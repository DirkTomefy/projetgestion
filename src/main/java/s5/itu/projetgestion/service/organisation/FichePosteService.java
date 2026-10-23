package s5.itu.projetgestion.service.organisation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.organisation.FichePosteDto;
import s5.itu.projetgestion.dto.organisation.FichePosteRequest;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.FichePoste;
import s5.itu.projetgestion.model.organisation.Metier;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.FichePosteRepository;
import s5.itu.projetgestion.repository.organisation.MetierRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.service.commun.PdfService;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Fiches de poste. Le modèle est attaché au métier et réutilisé par tous ses postes ; un poste peut avoir sa propre
 * fiche, qui prime. Lecture : RH, DG, ADMIN (tout), manager (son périmètre), employé (le poste qu'il occupe).
 * Écriture : RH.
 */
@Service
public class FichePosteService {

    public static final String TEMPLATE = "fiches/fiche-poste";
    private static final int MAX = 4000;
    private static final DateTimeFormatter FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FichePosteRepository repo;
    private final PosteRepository posteRepo;
    private final MetierRepository metierRepo;
    private final AffectationRepository affectationRepo;
    private final PerimetreService perimetre;
    private final PdfService pdf;
    private final GrilleSalarialeService grilleService;

    public FichePosteService(FichePosteRepository repo, PosteRepository posteRepo, MetierRepository metierRepo,
                             AffectationRepository affectationRepo, PerimetreService perimetre, PdfService pdf,
                             GrilleSalarialeService grilleService) {
        this.repo = repo;
        this.posteRepo = posteRepo;
        this.metierRepo = metierRepo;
        this.affectationRepo = affectationRepo;
        this.perimetre = perimetre;
        this.pdf = pdf;
        this.grilleService = grilleService;
    }

    /* ---------- catalogue et modèles ---------- */

    @Transactional(readOnly = true)
    public List<FichePosteRepository.LigneCatalogue> catalogue() {
        return repo.catalogue();
    }

    /** Texte standard proposé quand on crée une fiche (aucune fiche encore enregistrée). */
    public FichePosteRequest modeleStandard() {
        return new FichePosteRequest(
                "Décrire en deux ou trois phrases la raison d'être du poste et sa contribution aux objectifs de l'unité.",
                "Activité principale 1\nActivité principale 2\nActivité principale 3\nRendre compte à son supérieur direct",
                "Diplôme ou niveau d'études requis",
                "Expérience attendue (durée, domaine)",
                "Compétence technique\nQualité relationnelle\nSens de l'organisation",
                "Temps plein, 40 heures par semaine\nLieu de travail : à préciser");
    }

    @Transactional(readOnly = true)
    public FichePosteDto duMetier(Integer idMetier) {
        Metier m = metierRepo.findById(idMetier).orElseThrow(() -> new ProjetgestionException("Métier introuvable : " + idMetier));
        return repo.findByIdMetier(idMetier).map(f -> versDto(f, m, null, false))
                .orElseGet(() -> {
                    FichePosteRequest s = modeleStandard();
                    return new FichePosteDto(null, "STANDARD", m.getId(), m.getLibelle(), null, null, s.mission(), s.activites(),
                            s.formation(), s.experience(), s.competences(), s.conditions(), 0, null, false);
                });
    }

    @Transactional
    public FichePosteDto enregistrerMetier(Integer idMetier, FichePosteRequest req, SessionUser su) {
        Metier m = metierRepo.findById(idMetier).orElseThrow(() -> new ProjetgestionException("Métier introuvable : " + idMetier));
        FichePoste f = repo.findByIdMetier(idMetier).orElseGet(() -> {
            FichePoste n = new FichePoste();
            n.setIdMetier(idMetier);
            n.setVersion(0);
            return n;
        });
        appliquer(f, req, su);
        return versDto(repo.save(f), m, null, false);
    }

    @Transactional
    public FichePosteDto enregistrerPoste(Integer idPoste, FichePosteRequest req, SessionUser su) {
        Poste p = poste(idPoste);
        FichePoste f = repo.findByIdPoste(idPoste).orElseGet(() -> {
            FichePoste n = new FichePoste();
            n.setIdPoste(idPoste);
            n.setVersion(0);
            return n;
        });
        appliquer(f, req, su);
        return versDto(repo.save(f), p.getMetier(), p, false);
    }

    /** Retire la fiche propre au poste : il reprend le modèle de son métier. */
    @Transactional
    public void supprimerPoste(Integer idPoste) {
        FichePoste f = repo.findByIdPoste(idPoste).orElseThrow(() -> new ProjetgestionException("Ce poste n'a pas de fiche propre"));
        if (offreLiee(f)) throw new ProjetgestionException("Cette fiche est rattachée à une offre : modifiez-la plutôt que de la supprimer");
        repo.delete(f);
    }

    private boolean offreLiee(FichePoste f) {
        return repo.offresLiees(f.getId()) > 0;
    }

    private void appliquer(FichePoste f, FichePosteRequest r, SessionUser su) {
        String mission = nettoyer(r.mission(), "La mission");
        if (mission == null) throw new ProjetgestionException("La mission du poste est obligatoire");
        f.setMission(mission);
        f.setActivites(nettoyer(r.activites(), "Les activités"));
        f.setFormation(nettoyer(r.formation(), "La formation"));
        f.setExperience(nettoyer(r.experience(), "L'expérience"));
        f.setCompetences(nettoyer(r.competences(), "Les compétences"));
        f.setConditions(nettoyer(r.conditions(), "Les conditions"));
        f.setVersion(f.getVersion() + 1);
        f.setDateMaj(java.time.LocalDateTime.now());
        f.setIdAuteur(su == null ? null : su.getId());
    }

    private static String nettoyer(String s, String nom) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.length() > MAX) throw new ProjetgestionException(nom + " ne doit pas dépasser " + MAX + " caractères");
        return t;
    }

    /* ---------- lecture pour un poste ---------- */

    /** Fiche applicable à un poste (la sienne, sinon celle du métier) ; null si aucune n'existe. */
    @Transactional(readOnly = true)
    public FichePosteDto dePoste(Integer idPoste, SessionUser su) {
        Poste p = poste(idPoste);
        verifierLecture(su, p);
        return applicableDto(p);
    }

    /** Fiche du poste que l'employé connecté occupe ; null s'il n'occupe aucun poste ou si aucune fiche n'existe. */
    @Transactional(readOnly = true)
    public FichePosteDto maFiche(SessionUser su) {
        Poste p = posteOccupe(su);
        return p == null ? null : applicableDto(p);
    }

    @Transactional(readOnly = true)
    public byte[] pdfDePoste(Integer idPoste, SessionUser su) {
        Poste p = poste(idPoste);
        verifierLecture(su, p);
        return pdf.generer(TEMPLATE, variables(p, applicable(p), titulaire(su, p)));
    }

    @Transactional(readOnly = true)
    public byte[] pdfMaFiche(SessionUser su) {
        Poste p = posteOccupe(su);
        if (p == null) throw new ProjetgestionException("Vous n'occupez aucun poste actuellement");
        return pdf.generer(TEMPLATE, variables(p, applicable(p), titulaire(su, p)));
    }

    /** Texte repris dans l'offre d'emploi : [id de la fiche, description des missions, profil]. Vide si pas de fiche. */
    @Transactional(readOnly = true)
    public Optional<TexteOffre> texteOffre(Integer idPoste) {
        if (idPoste == null) return Optional.empty();
        return repo.applicable(idPoste).map(f -> {
            StringBuilder d = new StringBuilder(f.getMission());
            if (f.getActivites() != null) d.append("\n\nActivités principales :\n").append(puces(f.getActivites()));
            StringBuilder pr = new StringBuilder();
            if (f.getFormation() != null) pr.append("Formation : ").append(f.getFormation()).append('\n');
            if (f.getExperience() != null) pr.append("Expérience : ").append(f.getExperience()).append('\n');
            if (f.getCompetences() != null) pr.append("Compétences :\n").append(puces(f.getCompetences()));
            return new TexteOffre(f.getId(), d.toString().trim(), pr.toString().trim());
        });
    }

    public record TexteOffre(Integer idFiche, String description, String profil) { }

    private static String puces(String lignes) {
        StringBuilder b = new StringBuilder();
        for (String l : lignes.split("\\R")) if (!l.isBlank()) b.append("- ").append(l.trim()).append('\n');
        return b.toString();
    }

    /* ---------- interne ---------- */

    private Poste poste(Integer id) {
        return posteRepo.findById(id).orElseThrow(() -> new ProjetgestionException("Poste introuvable : " + id));
    }

    private Optional<FichePoste> applicable(Poste p) {
        return repo.applicable(p.getId());
    }

    private FichePosteDto applicableDto(Poste p) {
        return applicable(p).map(f -> versDto(f, p.getMetier(), p, f.getIdPoste() == null)).orElse(null);
    }

    private Poste posteOccupe(SessionUser su) {
        if (su == null || su.getIdEmploye() == null) return null;
        List<Affectation> l = affectationRepo.findEnCoursParEmploye(su.getIdEmploye());
        return l.isEmpty() ? null : l.get(0).getPoste();
    }

    private String titulaire(SessionUser su, Poste p) {
        Poste mien = posteOccupe(su);
        return mien != null && mien.getId().equals(p.getId()) ? su.getNomComplet() : null;
    }

    private void verifierLecture(SessionUser su, Poste p) {
        if (su == null) throw new AccesRefuseException("Vous devez être connecté");
        if (su.hasRole("RH") || su.hasRole("DG") || su.hasRole("ADMIN")) return;
        Poste mien = posteOccupe(su);
        if (mien != null && mien.getId().equals(p.getId())) return;
        if (su.hasRole("Manager") && perimetre.unitesDemandables(su).contains(p.getUniteOrga().getId())) return;
        throw new AccesRefuseException("Vous ne pouvez consulter que la fiche de votre poste");
    }

    private FichePosteDto versDto(FichePoste f, Metier m, Poste p, boolean herite) {
        return new FichePosteDto(f.getId(), f.getIdPoste() != null ? "POSTE" : "METIER",
                m == null ? null : m.getId(), m == null ? null : m.getLibelle(),
                p == null ? null : p.getId(), p == null ? null : p.getLibelle(),
                f.getMission(), f.getActivites(), f.getFormation(), f.getExperience(), f.getCompetences(),
                f.getConditions(), f.getVersion(), f.getDateMaj(), herite);
    }

    private Map<String, Object> variables(Poste p, Optional<FichePoste> opt, String titulaire) {
        FichePoste f = opt.orElseThrow(() -> new ProjetgestionException("Aucune fiche de poste n'existe encore pour " + p.getLibelle()));
        Map<String, Object> v = new HashMap<>();
        v.put("code", p.getCode());
        v.put("poste", p.getLibelle());
        v.put("unite", p.getUniteOrga().getLibelle());
        v.put("uniteType", null);
        v.put("metier", p.getMetier().getLibelle());
        v.put("categorie", p.getMetier().getCategorie() == null ? null : p.getMetier().getCategorie().getLibelle());
        v.put("echelon", p.getEchelon() == null ? null : p.getEchelon().getLibelle());
        v.put("superieur", p.getSuperieurDirect() == null ? null : p.getSuperieurDirect().getLibelle());
        v.put("salaire", fourchette(p));
        v.put("mission", f.getMission());
        v.put("activites", lignes(f.getActivites()));
        v.put("formation", f.getFormation());
        v.put("experience", f.getExperience());
        v.put("competences", lignes(f.getCompetences()));
        v.put("conditions", lignes(f.getConditions()));
        v.put("version", f.getVersion());
        v.put("dateMaj", f.getDateMaj().format(FR));
        v.put("titulaire", titulaire);
        return v;
    }

    private String fourchette(Poste p) {
        return grilleService.fourchette(p.getId(), LocalDate.now()).map(fo -> {
            DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.FRANCE);
            s.setGroupingSeparator(' ');
            DecimalFormat df = new DecimalFormat("#,##0", s);
            return df.format(fo.min()) + " – " + df.format(fo.max()) + " Ar / mois";
        }).orElse(null);
    }

    private static List<String> lignes(String s) {
        if (s == null) return List.of();
        return Arrays.stream(s.split("\\R")).map(String::trim).filter(x -> !x.isEmpty()).toList();
    }
}
