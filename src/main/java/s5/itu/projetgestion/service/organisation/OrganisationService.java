package s5.itu.projetgestion.service.organisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.organisation.EtapeValidationDto;
import s5.itu.projetgestion.dto.organisation.EtapeValidationRequest;
import s5.itu.projetgestion.dto.organisation.PosteDetailDto;
import s5.itu.projetgestion.dto.organisation.PosteRequest;
import s5.itu.projetgestion.dto.organisation.UniteDto;
import s5.itu.projetgestion.dto.organisation.UniteRequest;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.organisation.UniteOrga;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.besoin.WorkflowValidationRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.EchelonMetierRepository;
import s5.itu.projetgestion.repository.organisation.MetierRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;
import s5.itu.projetgestion.repository.reporting.IndicateurRepository;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;

/** Bloc 0 : unités, organigramme, postes, circuit de validation des besoins. */
@Service
@Transactional
public class OrganisationService {

    /** Ordre hiérarchique des types d'unité : une unité ne peut pas dépendre d'une unité de rang inférieur. */
    private static final List<String> TYPES = List.of("DIRECTION", "DEPARTEMENT", "SERVICE", "SECTION");
    private static final Set<String> ETATS_BESOIN_ACTIFS = Set.of("EN_ATTENTE", "OUVERT");

    private final UniteOrgaRepository uniteRepo;
    private final PosteRepository posteRepo;
    private final MetierRepository metierRepo;
    private final EchelonMetierRepository echelonRepo;
    private final AffectationRepository affectationRepo;
    private final BesoinPosteRepository besoinPosteRepo;
    private final WorkflowValidationRepository workflowRepo;
    private final IndicateurRepository indicateurs;
    private final GrilleSalarialeService grilleService;

    public OrganisationService(UniteOrgaRepository uniteRepo, PosteRepository posteRepo, MetierRepository metierRepo,
                               EchelonMetierRepository echelonRepo, AffectationRepository affectationRepo,
                               BesoinPosteRepository besoinPosteRepo, WorkflowValidationRepository workflowRepo,
                               IndicateurRepository indicateurs, GrilleSalarialeService grilleService) {
        this.uniteRepo = uniteRepo;
        this.posteRepo = posteRepo;
        this.metierRepo = metierRepo;
        this.echelonRepo = echelonRepo;
        this.affectationRepo = affectationRepo;
        this.besoinPosteRepo = besoinPosteRepo;
        this.workflowRepo = workflowRepo;
        this.indicateurs = indicateurs;
        this.grilleService = grilleService;
    }

    // ================================================================== unités

    @Transactional(readOnly = true)
    public List<UniteDto> unites() {
        Map<Integer, IndicateurRepository.LigneUnite> stats = statsUnites();
        return uniteRepo.findAllAvecRelations().stream().map(u -> toDto(u, stats.get(u.getId()), List.of())).toList();
    }

    /** Organigramme : arbre des unités (racines = unités sans unité mère). */
    @Transactional(readOnly = true)
    public List<UniteDto> organigramme() {
        Map<Integer, IndicateurRepository.LigneUnite> stats = statsUnites();
        List<UniteOrga> toutes = uniteRepo.findAllAvecRelations();
        Map<Integer, List<UniteOrga>> enfants = new LinkedHashMap<>();
        for (UniteOrga u : toutes) {
            Integer mere = u.getUniteMere() == null ? null : u.getUniteMere().getId();
            enfants.computeIfAbsent(mere, k -> new ArrayList<>()).add(u);
        }
        return construire(null, enfants, stats);
    }

    private List<UniteDto> construire(Integer idMere, Map<Integer, List<UniteOrga>> enfants,
                                      Map<Integer, IndicateurRepository.LigneUnite> stats) {
        return enfants.getOrDefault(idMere, List.of()).stream()
                .map(u -> toDto(u, stats.get(u.getId()), construire(u.getId(), enfants, stats))).toList();
    }

    public UniteDto enregistrerUnite(Integer id, UniteRequest req) {
        if (req == null) throw new IllegalArgumentException("Formulaire manquant.");
        UniteOrga u = id == null ? new UniteOrga() : trouverUnite(id);
        String code = obligatoire(req.code(), "Le code est obligatoire.").toUpperCase(Locale.ROOT);
        if (uniteRepo.existsByCodeIgnoreCaseAndIdNot(code, u.getId() == null ? -1 : u.getId()))
            throw new IllegalArgumentException("Le code " + code + " est déjà utilisé.");
        String type = obligatoire(req.type(), "Le type est obligatoire.").toUpperCase(Locale.ROOT);
        if (!TYPES.contains(type)) throw new IllegalArgumentException("Type d'unité inconnu : " + type);
        UniteOrga mere = null;
        if (req.idUniteMere() != null) {
            mere = trouverUnite(req.idUniteMere());
            if (u.getId() != null) {   // pas de cycle : la nouvelle mère ne doit pas être l'unité elle-même ni une de ses filles
                for (UniteOrga x = mere; x != null; x = x.getUniteMere())
                    if (x.getId().equals(u.getId())) throw new IllegalArgumentException("Une unité ne peut pas dépendre d'elle-même ou d'une de ses sous-unités.");
            }
            if (TYPES.indexOf(type) < TYPES.indexOf(mere.getType()))
                throw new IllegalArgumentException("Une unité de type " + type + " ne peut pas dépendre d'une unité de type " + mere.getType() + ".");
        } else if (!"DIRECTION".equals(type)) {
            throw new IllegalArgumentException("Seule une direction peut être une unité racine : choisissez l'unité mère.");
        }
        Poste responsable = null;
        if (req.idPosteResponsable() != null) {
            responsable = posteRepo.findById(req.idPosteResponsable()).orElseThrow(() -> new IllegalArgumentException("Poste introuvable."));
            if (u.getId() == null || !responsable.getUniteOrga().getId().equals(u.getId()))
                throw new IllegalArgumentException("Le poste responsable doit appartenir à l'unité.");
        }
        u.setCode(code);
        u.setType(type);
        u.setLibelle(obligatoire(req.libelle(), "Le libellé est obligatoire."));
        u.setUniteMere(mere);
        u.setPosteResponsable(responsable);
        uniteRepo.save(u);
        return toDto(u, statsUnites().get(u.getId()), List.of());
    }

    public void supprimerUnite(Integer id) {
        UniteOrga u = trouverUnite(id);
        if (uniteRepo.countByUniteMereId(id) > 0) throw new IllegalStateException("Cette unité a des sous-unités : supprimez-les ou rattachez-les ailleurs d'abord.");
        if (posteRepo.countByUniteOrgaId(id) > 0) throw new IllegalStateException("Cette unité a des postes : elle ne peut pas être supprimée.");
        try {
            uniteRepo.delete(u);
            uniteRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Cette unité est utilisée (besoins, budgets) : elle ne peut pas être supprimée.");
        }
    }

    // ================================================================== postes

    @Transactional(readOnly = true)
    public List<PosteDetailDto> postes(String etat, String occupation, Integer idUnite, String q) {
        return posteRepo.rechercher(norm(etat), norm(occupation), idUnite == null ? 0 : idUnite, q == null ? "" : q.trim().toLowerCase(Locale.ROOT))
                .stream().map(p -> detail(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public PosteDetailDto poste(Integer id) {
        return detail(posteRepo.findAvecRelations(id).orElseThrow(() -> introuvable("Poste", id)), true);
    }

    /** Création directe d'un poste ACTIF (ADMIN / RH) ; une demande de poste par un manager passe par PosteService.creerDemande. */
    public PosteDetailDto creerPoste(PosteRequest req) {
        Poste p = new Poste();
        appliquer(p, req);
        p.setCode(genererCode());
        p.setEtat("ACTIF");
        p.setStatutOccupation("LIBRE");
        posteRepo.save(p);
        return detail(p, true);
    }

    public PosteDetailDto modifierPoste(Integer id, PosteRequest req) {
        Poste p = posteRepo.findById(id).orElseThrow(() -> introuvable("Poste", id));
        if ("SUPPRIME".equals(p.getEtat()) || "REFUSE".equals(p.getEtat()))
            throw new IllegalStateException("Un poste " + p.getEtat() + " ne peut plus être modifié.");
        if (!p.getUniteOrga().getId().equals(req.idUniteOrga()) && "OCCUPE".equals(p.getStatutOccupation()))
            throw new IllegalStateException("Un poste occupé ne peut pas changer d'unité : passez par une mutation de l'employé.");
        appliquer(p, req);
        return detail(p, true);
    }

    /** Suppression d'un poste actif : état SUPPRIME (historique conservé). Le poste doit être libre et sans recrutement en cours. */
    public PosteDetailDto supprimerPoste(Integer id) {
        Poste p = posteRepo.findById(id).orElseThrow(() -> introuvable("Poste", id));
        if (!"ACTIF".equals(p.getEtat())) throw new IllegalStateException("Seul un poste ACTIF peut être supprimé.");
        if ("OCCUPE".equals(p.getStatutOccupation())) throw new IllegalStateException("Ce poste est occupé : il ne peut pas être supprimé.");
        if (besoinPosteRepo.existsByPosteIdAndEtatIn(id, ETATS_BESOIN_ACTIFS))
            throw new IllegalStateException("Un besoin en cours porte sur ce poste : il ne peut pas être supprimé.");
        if (posteRepo.countBySuperieurDirectIdAndEtatNot(id, "SUPPRIME") > 0)
            throw new IllegalStateException("D'autres postes ont ce poste pour supérieur direct : rattachez-les ailleurs d'abord.");
        if (workflowRepo.findAll().stream().anyMatch(w -> w.getPoste().getId().equals(id)))
            throw new IllegalStateException("Ce poste fait partie du circuit de validation des besoins.");
        p.setEtat("SUPPRIME");
        return detail(p, true);
    }

    private void appliquer(Poste p, PosteRequest req) {
        if (req == null) throw new IllegalArgumentException("Formulaire manquant.");
        p.setLibelle(obligatoire(req.libelle(), "Le libellé du poste est obligatoire."));
        if (req.idMetier() == null || req.idEchelon() == null || req.idUniteOrga() == null)
            throw new IllegalArgumentException("Le métier, l'échelon et l'unité sont obligatoires.");
        p.setMetier(metierRepo.findById(req.idMetier()).orElseThrow(() -> new IllegalArgumentException("Métier introuvable.")));
        p.setEchelon(echelonRepo.findById(req.idEchelon()).orElseThrow(() -> new IllegalArgumentException("Échelon introuvable.")));
        p.setUniteOrga(trouverUnite(req.idUniteOrga()));
        Poste sup = null;
        if (req.idSuperieurDirect() != null) {
            sup = posteRepo.findById(req.idSuperieurDirect()).orElseThrow(() -> new IllegalArgumentException("Supérieur direct introuvable."));
            if (!"ACTIF".equals(sup.getEtat())) throw new IllegalArgumentException("Le supérieur direct doit être un poste ACTIF.");
            boolean dansLaChaine = false;   // l'unité du supérieur : celle du poste ou une unité dont elle dépend
            for (UniteOrga u = p.getUniteOrga(); u != null && !dansLaChaine; u = u.getUniteMere())
                dansLaChaine = u.getId().equals(sup.getUniteOrga().getId());
            if (!dansLaChaine)
                throw new IllegalArgumentException("Le supérieur direct doit appartenir à l'unité du poste ou à une unité dont elle dépend.");
            for (Poste x = sup; x != null; x = x.getSuperieurDirect())
                if (p.getId() != null && x.getId().equals(p.getId()))
                    throw new IllegalArgumentException("Un poste ne peut pas être son propre supérieur (directement ou non).");
        }
        p.setSuperieurDirect(sup);
    }

    private PosteDetailDto detail(Poste p, boolean complet) {
        Affectation a = affectationRepo.findEnCoursParPoste(p.getId()).stream().findFirst().orElse(null);
        Employe e = a == null ? null : a.getEmploye();
        BigDecimal min = null, max = null;
        List<OptionDto> subordonnes = List.of();
        String besoinRef = null;
        Integer idBesoin = null;
        if (complet) {
            var f = grilleService.fourchette(p.getId(), LocalDate.now()).orElse(null);
            if (f != null) { min = f.min(); max = f.max(); }
            subordonnes = posteRepo.findBySuperieurDirectIdAndEtatNot(p.getId(), "SUPPRIME").stream()
                    .map(s -> new OptionDto(s.getId(), s.getLibelle())).toList();
        }
        if (complet || "PROPOSE".equals(p.getEtat())) {
            var bp = besoinPosteRepo.findDernierParPoste(p.getId()).orElse(null);
            if (bp != null) { besoinRef = bp.getBesoin().getReference(); idBesoin = bp.getBesoin().getId(); }
        }
        Poste sup = p.getSuperieurDirect();
        return new PosteDetailDto(p.getId(), p.getCode(), p.getLibelle(), p.getEtat(), p.getStatutOccupation(),
                p.getMetier().getId(), p.getMetier().getLibelle(), p.getMetier().getCategorie().getId(), p.getMetier().getCategorie().getLibelle(),
                p.getEchelon().getId(), p.getEchelon().getLibelle(), p.getUniteOrga().getId(), p.getUniteOrga().getLibelle(),
                sup == null ? null : sup.getId(), sup == null ? null : sup.getLibelle(),
                e == null ? null : e.getId(), e == null ? null : e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()),
                e == null ? null : e.getMatricule(), a == null ? null : a.getDateDebut(),
                min, max, subordonnes, besoinRef, idBesoin);
    }

    private String genererCode() {
        long n = posteRepo.count() + 1;
        String code = String.format("POST-%03d", n);
        while (posteRepo.existsByCode(code)) code = String.format("POST-%03d", ++n);
        return code;
    }

    // ================================================================== circuit de validation

    @Transactional(readOnly = true)
    public List<EtapeValidationDto> circuit() {
        return workflowRepo.findAllByOrderByOrdreAscIdAsc().stream().map(this::toDto).toList();
    }

    public EtapeValidationDto enregistrerEtape(Integer id, EtapeValidationRequest req) {
        if (req == null || req.ordre() == null || req.idPoste() == null) throw new IllegalArgumentException("L'ordre et le poste valideur sont obligatoires.");
        if (req.ordre() < 1) throw new IllegalArgumentException("L'ordre commence à 1.");
        if (req.controle() != null && !req.controle().isBlank() && !"BUDGET".equals(req.controle()))
            throw new IllegalArgumentException("Contrôle inconnu : " + req.controle());
        WorkflowValidation w = id == null ? new WorkflowValidation() : workflowRepo.findById(id).orElseThrow(() -> introuvable("Étape", id));
        Poste p = posteRepo.findById(req.idPoste()).orElseThrow(() -> new IllegalArgumentException("Poste introuvable."));
        if (!"ACTIF".equals(p.getEtat())) throw new IllegalArgumentException("Le poste valideur doit être ACTIF.");
        if (workflowRepo.existsByPosteIdAndOrdreAndIdNot(p.getId(), req.ordre(), w.getId() == null ? -1 : w.getId()))
            throw new IllegalArgumentException("Ce poste valide déjà à cette étape.");
        w.setOrdre(req.ordre());
        w.setPoste(p);
        w.setControle(req.controle() == null || req.controle().isBlank() ? null : req.controle());
        workflowRepo.save(w);
        controlerContinuite();
        return toDto(w);
    }

    public void supprimerEtape(Integer id) {
        WorkflowValidation w = workflowRepo.findById(id).orElseThrow(() -> introuvable("Étape", id));
        try {
            workflowRepo.delete(w);
            workflowRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Des décisions ont déjà été prises à cette étape : modifiez-la plutôt que de la supprimer.");
        }
        controlerContinuite();
    }

    /** Les ordres doivent se suivre (1, 2, 3...) : une étape manquante bloquerait tous les besoins. */
    private void controlerContinuite() {
        workflowRepo.flush();
        Set<Integer> ordres = new HashSet<>();
        workflowRepo.findAll().forEach(w -> ordres.add(w.getOrdre()));
        for (int i = 1; i <= ordres.size(); i++)
            if (!ordres.contains(i)) throw new IllegalArgumentException("Les étapes doivent se suivre sans trou (1, 2, 3...) : l'étape " + i + " manque.");
    }

    private EtapeValidationDto toDto(WorkflowValidation w) {
        Poste p = w.getPoste();
        Affectation a = affectationRepo.findEnCoursParPoste(p.getId()).stream().findFirst().orElse(null);
        return new EtapeValidationDto(w.getId(), w.getOrdre(), p.getId(), p.getLibelle(), p.getUniteOrga().getLibelle(),
                a == null ? null : a.getEmploye().getNom() + (a.getEmploye().getPrenoms() == null ? "" : " " + a.getEmploye().getPrenoms()),
                w.getControle());
    }

    // ================================================================== outils

    private Map<Integer, IndicateurRepository.LigneUnite> statsUnites() {
        return indicateurs.parUnite(LocalDate.now().getYear()).stream()
                .collect(Collectors.toMap(IndicateurRepository.LigneUnite::getIdUnite, Function.identity()));
    }

    private UniteDto toDto(UniteOrga u, IndicateurRepository.LigneUnite s, List<UniteDto> enfants) {
        Poste resp = u.getPosteResponsable();
        String responsable = null;
        if (resp != null) {
            Affectation a = affectationRepo.findEnCoursParPoste(resp.getId()).stream().findFirst().orElse(null);
            if (a != null) responsable = a.getEmploye().getNom() + (a.getEmploye().getPrenoms() == null ? "" : " " + a.getEmploye().getPrenoms());
        }
        return new UniteDto(u.getId(), u.getCode(), u.getType(), u.getLibelle(),
                u.getUniteMere() == null ? null : u.getUniteMere().getId(), u.getUniteMere() == null ? null : u.getUniteMere().getLibelle(),
                resp == null ? null : resp.getId(), resp == null ? null : resp.getLibelle(), responsable,
                s == null ? 0 : s.getPostes(), s == null ? 0 : s.getOccupes(), s == null ? 0 : s.getVacants(),
                s == null ? 0 : s.getEffectif(), s == null ? null : s.getBudget(), enfants);
    }

    private UniteOrga trouverUnite(Integer id) {
        return uniteRepo.findById(id).orElseThrow(() -> introuvable("Unité", id));
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }

    private static String obligatoire(String v, String message) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(message);
        return v.trim();
    }

    private static ResponseStatusException introuvable(String quoi, Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, quoi + " introuvable : " + id);
    }
}
