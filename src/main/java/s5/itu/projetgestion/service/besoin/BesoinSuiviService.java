package s5.itu.projetgestion.service.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.besoin.BesoinFicheDto;
import s5.itu.projetgestion.dto.besoin.BesoinLigneDto;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.besoin.Validation;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.recrutement.Candidat;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.besoin.HistoBesoinRepository;
import s5.itu.projetgestion.repository.besoin.ValidationRepository;
import s5.itu.projetgestion.repository.besoin.WorkflowValidationRepository;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;

/** Consultation des besoins (liste filtrée, fiche complète) et annulation par le demandeur ou la RH. */
@Service
public class BesoinSuiviService {

    private final BesoinRepository besoinRepo;
    private final BesoinPosteRepository besoinPosteRepo;
    private final ValidationRepository validationRepo;
    private final WorkflowValidationRepository workflowRepo;
    private final HistoBesoinRepository histoRepo;
    private final OffreEmploiRepository offreRepo;
    private final UserRepository userRepo;
    private final ValidationService validationService;
    private final HistoBesoinService histo;
    private final GrilleSalarialeService grilleService;
    private final s5.itu.projetgestion.repository.organisation.PosteRepository posteRepo;

    public BesoinSuiviService(BesoinRepository besoinRepo, BesoinPosteRepository besoinPosteRepo,
                              ValidationRepository validationRepo, WorkflowValidationRepository workflowRepo,
                              HistoBesoinRepository histoRepo, OffreEmploiRepository offreRepo, UserRepository userRepo,
                              ValidationService validationService, HistoBesoinService histo,
                              GrilleSalarialeService grilleService,
                              s5.itu.projetgestion.repository.organisation.PosteRepository posteRepo) {
        this.posteRepo = posteRepo;
        this.besoinRepo = besoinRepo;
        this.besoinPosteRepo = besoinPosteRepo;
        this.validationRepo = validationRepo;
        this.workflowRepo = workflowRepo;
        this.histoRepo = histoRepo;
        this.offreRepo = offreRepo;
        this.userRepo = userRepo;
        this.validationService = validationService;
        this.histo = histo;
        this.grilleService = grilleService;
    }

    /**
     * vue : "tous" (défaut), "mes-demandes" (créés par l'utilisateur), "a-valider" (attendant sa décision).
     * etat : EN_ATTENTE, OUVERT, POURVU, ANNULE ou vide ; q : référence, unité, poste.
     */
    @Transactional(readOnly = true)
    public List<BesoinLigneDto> lister(SessionUser su, String vue, String etat, String q) {
        User u = userRepo.findById(su.getId()).orElseThrow();
        String texte = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<BesoinLigneDto> res = new ArrayList<>();
        for (Besoin b : besoinRepo.findAllAvecRelations()) {
            if (etat != null && !etat.isBlank() && !etat.equalsIgnoreCase(b.getEtatActuel())) continue;
            if ("mes-demandes".equals(vue) && !b.getUser().getId().equals(u.getId())) continue;
            boolean aMoi = "EN_ATTENTE".equals(b.getEtatActuel()) && validationService.peutDecider(b, u);
            if ("a-valider".equals(vue) && !aMoi) continue;
            List<BesoinPoste> lignes = besoinPosteRepo.findByBesoinId(b.getId());
            String postes = resumePostes(lignes);
            if (!texte.isEmpty() && !(b.getReference() + " " + b.getUniteOrga().getLibelle() + " " + postes)
                    .toLowerCase(Locale.ROOT).contains(texte)) continue;
            String etape = validationService.etapesCourantes(b).stream().map(w -> w.getPoste().getLibelle())
                    .collect(Collectors.joining(" ou "));
            res.add(new BesoinLigneDto(b.getId(), b.getReference(), b.getTypeBesoin(), b.getUniteOrga().getId(),
                    b.getUniteOrga().getLibelle(), postes, b.getEffectif(), b.getDateSouhaitee(), b.getEtatActuel(),
                    nom(b.getUser()), dateDemande(b), etape.isEmpty() ? null : etape, aMoi,
                    lignes.stream().anyMatch(l -> "PROPOSE".equals(l.getPoste().getEtat())),
                    b.getBudgetEstime() == null ? null : BigDecimal.valueOf(b.getBudgetEstime())));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public BesoinFicheDto fiche(Integer id, SessionUser su) {
        Besoin b = trouver(id);
        User u = userRepo.findById(su.getId()).orElseThrow();
        LocalDate jour = b.getDateSouhaitee() != null ? b.getDateSouhaitee() : LocalDate.now();

        List<BesoinFicheDto.Ligne> lignes = besoinPosteRepo.findByBesoinId(id).stream().map(l -> {
            var f = grilleService.fourchette(l.getPoste().getId(), jour).orElse(null);
            Employe r = l.getEmployeRemplace();
            Candidat c = l.getCandidatureRetenue() == null ? null : l.getCandidatureRetenue().getCandidat();
            return new BesoinFicheDto.Ligne(l.getId(), l.getPoste().getId(), l.getPoste().getCode(), l.getPoste().getLibelle(),
                    l.getPoste().getEtat(), l.getPoste().getMetier().getLibelle(), l.getPoste().getEchelon().getLibelle(),
                    l.getEtat(), r == null ? null : r.getNom() + (r.getPrenoms() == null ? "" : " " + r.getPrenoms()),
                    c == null ? null : c.getNom() + (c.getPrenom() == null ? "" : " " + c.getPrenom()),
                    f == null ? null : f.min(), f == null ? null : f.max());
        }).toList();

        // circuit : chaque ordre d'étape, avec la décision prise (ou attendue)
        List<Validation> decisions = validationRepo.findParBesoin(id).stream()
                .filter(v -> !"EN_ATTENTE".equals(v.getDecision())).toList();
        Set<Integer> courants = validationService.etapesCourantes(b).stream().map(WorkflowValidation::getOrdre).collect(Collectors.toSet());
        Map<Integer, List<WorkflowValidation>> parOrdre = new LinkedHashMap<>();
        workflowRepo.findAllByOrderByOrdreAscIdAsc().forEach(w -> parOrdre.computeIfAbsent(w.getOrdre(), k -> new ArrayList<>()).add(w));
        boolean refuse = false;
        List<BesoinFicheDto.Etape> circuit = new ArrayList<>();
        for (var e : parOrdre.entrySet()) {
            Validation v = decisions.stream().filter(d -> d.getWorkflowValidation().getOrdre().equals(e.getKey()))
                    .reduce((a, x) -> x).orElse(null);
            String postes = e.getValue().stream().map(w -> w.getPoste().getLibelle()).collect(Collectors.joining(" ou "));
            boolean budget = e.getValue().stream().anyMatch(w -> "BUDGET".equals(w.getControle()));
            String statut;
            if (v != null) {
                statut = "REFUSE".equals(v.getDecision()) ? "REFUSE" : "FAIT";
                if ("REFUSE".equals(v.getDecision())) refuse = true;
            } else if (refuse || "ANNULE".equals(b.getEtatActuel())) {
                statut = "NON_ATTEINTE";
            } else {
                statut = courants.contains(e.getKey()) ? "EN_COURS" : "A_VENIR";
            }
            circuit.add(new BesoinFicheDto.Etape(e.getKey(), postes, statut, v == null ? null : nom(v.getUser()),
                    v == null ? null : v.getDate(), v == null ? null : v.getCommentaire(), budget));
        }

        var cb = validationService.controleBudget(b);
        List<BesoinFicheDto.Evenement> historique = histoRepo.findParBesoin(id).stream()
                .map(h -> new BesoinFicheDto.Evenement(h.getDateAction(), h.getAction(), h.getEtatAvant(), h.getEtatApres(),
                        h.getUser() == null ? "Application" : nom(h.getUser()), h.getCommentaire()))
                .toList();
        List<BesoinFicheDto.Offre> offres = offreRepo.findByIdBesoinOrderByIdDesc(id).stream()
                .map(o -> new BesoinFicheDto.Offre(o.getId(), o.getRef(), o.getEtat(), o.getDatePublication(), o.getDateLimiteCandidature(),
                        o.getIdPoste() == null ? null : posteRepo.findById(o.getIdPoste()).map(p -> p.getLibelle()).orElse(null)))
                .toList();

        boolean decider = "EN_ATTENTE".equals(b.getEtatActuel()) && validationService.peutDecider(b, u);
        boolean annuler = peutAnnuler(b, su, offres);
        // une offre par poste : postes qui ont encore des places ouvertes et pas d'offre en cours
        List<Integer> postesSansOffre = !"OUVERT".equals(b.getEtatActuel()) ? List.of()
                : besoinPosteRepo.findByBesoinId(id).stream()
                    .filter(l -> !"POURVU".equals(l.getEtat()) && !"ANNULE".equals(l.getEtat()))
                    .map(l -> l.getPoste().getId()).distinct()
                    .filter(idPoste -> !offreRepo.existeOffreEnCours(id, idPoste)).toList();
        boolean creerOffre = su.hasRole("RH") && !postesSansOffre.isEmpty();
        return new BesoinFicheDto(b.getId(), b.getReference(), b.getTypeBesoin(), b.getEtatActuel(),
                b.getUniteOrga().getId(), b.getUniteOrga().getLibelle(), b.getMotif().getId(), b.getMotif().getLibelle(),
                b.getPrecisionMotif(), b.getDateSouhaitee(), b.getEffectif(), nom(b.getUser()), dateDemande(b),
                lignes, circuit, new BesoinFicheDto.Budget(cb.exercice(), cb.coutAnnuel(), cb.budget(),
                        cb.masseSalariale(), cb.autresBesoins(), cb.disponible(), cb.dansLeBudget()),
                historique, offres, new BesoinFicheDto.Actions(decider, decider, annuler, creerOffre, postesSansOffre));
    }

    /** Annulation par le demandeur (ou la RH) tant qu'aucun siège n'est pourvu et qu'aucune offre n'est publiée. */
    @Transactional
    public BesoinFicheDto annuler(Integer id, SessionUser su, String motif) {
        Besoin b = trouver(id);
        List<BesoinFicheDto.Offre> offres = offreRepo.findByIdBesoinOrderByIdDesc(id).stream()
                .map(o -> new BesoinFicheDto.Offre(o.getId(), o.getRef(), o.getEtat(), o.getDatePublication(), o.getDateLimiteCandidature(),
                        o.getIdPoste() == null ? null : posteRepo.findById(o.getIdPoste()).map(p -> p.getLibelle()).orElse(null)))
                .toList();
        if (!peutAnnuler(b, su, offres)) {
            if (!b.getUser().getId().equals(su.getId()) && !su.hasRole("RH"))
                throw new s5.itu.projetgestion.err.AccesRefuseException("Seul le demandeur ou la RH peut annuler ce besoin.");
            throw new IllegalStateException("Ce besoin ne peut plus être annulé (état " + b.getEtatActuel()
                    + ", offre publiée ou siège déjà pourvu).");
        }
        if (motif == null || motif.isBlank()) throw new IllegalArgumentException("Le motif de l'annulation est obligatoire.");
        String avant = b.getEtatActuel();
        b.setEtatActuel("ANNULE");
        for (BesoinPoste bp : besoinPosteRepo.findByBesoinId(id)) {
            bp.setEtat("ANNULE");
            if ("PROPOSE".equals(bp.getPoste().getEtat())) bp.getPoste().setEtat("REFUSE");
        }
        histo.tracer(b, avant, "ANNULE", HistoBesoinService.ANNULATION, userRepo.getReferenceById(su.getId()), motif);
        return fiche(id, su);
    }

    private boolean peutAnnuler(Besoin b, SessionUser su, List<BesoinFicheDto.Offre> offres) {
        boolean acteur = b.getUser().getId().equals(su.getId()) || su.hasRole("RH");
        if (!acteur) return false;
        if ("EN_ATTENTE".equals(b.getEtatActuel())) return true;
        return "OUVERT".equals(b.getEtatActuel())
                && offres.stream().noneMatch(o -> "PUBLIEE".equals(o.etat()))
                && besoinPosteRepo.findByBesoinId(b.getId()).stream().noneMatch(l -> "POURVU".equals(l.getEtat()));
    }

    private LocalDateTime dateDemande(Besoin b) {
        return validationRepo.findParBesoin(b.getId()).stream().findFirst().map(Validation::getDate).orElse(null);
    }

    private static String resumePostes(List<BesoinPoste> lignes) {
        Map<String, Long> n = lignes.stream().collect(Collectors.groupingBy(l -> l.getPoste().getLibelle(), LinkedHashMap::new, Collectors.counting()));
        return n.entrySet().stream().map(e -> e.getValue() > 1 ? e.getKey() + " ×" + e.getValue() : e.getKey())
                .collect(Collectors.joining(", "));
    }

    private static String nom(User u) {
        return u.getNomComplet() != null ? u.getNomComplet() + " (" + u.getLogin() + ")" : u.getLogin();
    }

    private Besoin trouver(Integer id) {
        return besoinRepo.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Besoin introuvable : " + id));
    }
}
