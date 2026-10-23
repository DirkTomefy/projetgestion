package s5.itu.projetgestion.service.onboarding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.onboarding.ModeleOnboardingDto;
import s5.itu.projetgestion.dto.onboarding.ModeleOnboardingRequest;
import s5.itu.projetgestion.dto.onboarding.OnboardingDto;
import s5.itu.projetgestion.dto.onboarding.TacheDto;
import s5.itu.projetgestion.dto.onboarding.TacheRequest;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.onboarding.Onboarding;
import s5.itu.projetgestion.model.onboarding.OnboardingDetail;
import s5.itu.projetgestion.model.onboarding.WorkflowOnboarding;
import s5.itu.projetgestion.model.onboarding.WorkflowOnboardingDetail;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.onboarding.OnboardingDetailRepository;
import s5.itu.projetgestion.repository.onboarding.OnboardingRepository;
import s5.itu.projetgestion.repository.onboarding.WorkflowOnboardingDetailRepository;
import s5.itu.projetgestion.repository.onboarding.WorkflowOnboardingRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;
import s5.itu.projetgestion.service.notification.NotificationService;

/**
 * Onboarding (bloc 4) : modèles de tâches, démarrage automatique à la signature du contrat,
 * suivi des tâches par poste responsable, fin quand toutes les tâches obligatoires sont faites.
 */
@Service
@Transactional
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);
    private static final Set<String> STATUTS_TACHE = Set.of("A_FAIRE", "FAIT", "NON_APPLICABLE");

    private final OnboardingRepository onboardingRepo;
    private final OnboardingDetailRepository detailRepo;
    private final WorkflowOnboardingRepository modeleRepo;
    private final WorkflowOnboardingDetailRepository modeleDetailRepo;
    private final ContratRepository contratRepo;
    private final PosteRepository posteRepo;
    private final AffectationRepository affectationRepo;
    private final UserRepository userRepo;
    private final DelegationRepository delegationRepo;
    private final NotificationService notificationService;

    public OnboardingService(OnboardingRepository onboardingRepo, OnboardingDetailRepository detailRepo,
                             WorkflowOnboardingRepository modeleRepo, WorkflowOnboardingDetailRepository modeleDetailRepo,
                             ContratRepository contratRepo, PosteRepository posteRepo, AffectationRepository affectationRepo,
                             UserRepository userRepo, DelegationRepository delegationRepo,
                             NotificationService notificationService) {
        this.onboardingRepo = onboardingRepo;
        this.detailRepo = detailRepo;
        this.modeleRepo = modeleRepo;
        this.modeleDetailRepo = modeleDetailRepo;
        this.contratRepo = contratRepo;
        this.posteRepo = posteRepo;
        this.affectationRepo = affectationRepo;
        this.userRepo = userRepo;
        this.delegationRepo = delegationRepo;
        this.notificationService = notificationService;
    }

    // ================================================================== démarrage (appelé par ContratService.signer)

    /**
     * Démarre l'onboarding d'un contrat signé avec le premier modèle actif : une tâche par tâche du modèle,
     * échéance = début du contrat + délai. Sans effet pour un renouvellement (l'employé est déjà intégré)
     * ou si l'onboarding existe déjà.
     */
    public void demarrer(Integer idContrat) {
        Contrat c = contratRepo.findById(idContrat).orElseThrow(() -> new IllegalArgumentException("Contrat introuvable : " + idContrat));
        if (c.getContratPrecedent() != null || onboardingRepo.existsByContratId(idContrat)) return;
        WorkflowOnboarding modele = modeleRepo.findByStatutOrderByIdAsc("ACTIF").stream().findFirst().orElse(null);
        if (modele == null) {
            log.warn("Aucun modèle d'onboarding actif : onboarding non démarré pour le contrat {}", idContrat);
            return;
        }
        LocalDate debut = c.getDateDebut() != null ? c.getDateDebut() : LocalDate.now();
        Onboarding o = new Onboarding();
        o.setContrat(c);
        o.setWorkflow(modele);
        o.setStatut("EN_COURS");
        o.setDateDebut(debut);
        onboardingRepo.save(o);

        Poste posteEmploye = c.getAffectation().getPoste();
        Employe e = c.getAffectation().getEmploye();
        String nom = e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms());
        Set<Integer> postesNotifies = new HashSet<>();
        for (WorkflowOnboardingDetail t : modele.getTaches()) {
            OnboardingDetail d = new OnboardingDetail();
            d.setOnboarding(o);
            d.setModele(t);
            d.setOrdre(t.getOrdre());
            d.setTypeTache(t.getTypeTache());
            d.setLibelle(t.getLibelle() != null ? t.getLibelle() : t.getTypeTache());
            d.setPosteResponsable(t.getPosteResponsable() != null ? t.getPosteResponsable() : posteEmploye.getSuperieurDirect());
            d.setStatut("A_FAIRE");
            d.setDateEcheance(debut.plusDays(t.getDelaiJours() == null ? 0 : t.getDelaiJours()));
            detailRepo.save(d);
            o.getTaches().add(d);
            if (d.getPosteResponsable() != null && postesNotifies.add(d.getPosteResponsable().getId())) {
                notificationService.notifierPoste(d.getPosteResponsable().getId(), NotificationService.ONBOARDING_TACHE,
                        "Onboarding de " + nom + " (" + posteEmploye.getLibelle() + ") : des tâches vous sont confiées",
                        "/onboarding/mes-taches");
            }
        }
        notificationService.notifierRole("RH", NotificationService.ONBOARDING_TACHE,
                "Onboarding démarré pour " + nom + " (" + posteEmploye.getLibelle() + ")", "/onboarding/" + o.getId());
    }

    // ================================================================== consultation

    @Transactional(readOnly = true)
    public List<OnboardingDto> lister(String statut) {
        String s = statut == null ? "" : statut.trim().toUpperCase(Locale.ROOT);
        return onboardingRepo.rechercher(s).stream().map(o -> toDto(o, false, null)).toList();
    }

    @Transactional(readOnly = true)
    public OnboardingDto fiche(Integer id, SessionUser su) {
        return toDto(trouver(id), true, su);
    }

    @Transactional(readOnly = true)
    public List<OnboardingDto> parEmploye(Integer idEmploye) {
        return onboardingRepo.findParEmploye(idEmploye).stream().map(o -> toDto(o, true, null)).toList();
    }

    /** Tâches à faire confiées aux postes de l'utilisateur (et à ceux de qui lui a délégué). */
    @Transactional(readOnly = true)
    public List<TacheDto> mesTaches(SessionUser su) {
        Set<Integer> postes = postesDe(su.getId());
        if (postes.isEmpty()) return List.of();
        return detailRepo.aFairePourPostes(postes).stream().map(d -> toDto(d, su, true)).toList();
    }

    // ================================================================== avancement

    public OnboardingDto marquer(Integer idTache, TacheRequest req, SessionUser su) {
        OnboardingDetail d = detailRepo.findById(idTache)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tâche introuvable : " + idTache));
        if (!peutModifier(d, su)) throw new AccesRefuseException("Cette tâche est confiée à un autre poste.");
        String statut = req == null || req.statut() == null ? "" : req.statut().trim().toUpperCase(Locale.ROOT);
        if (!STATUTS_TACHE.contains(statut)) throw new IllegalArgumentException("Statut de tâche inconnu : " + statut);
        if ("NON_APPLICABLE".equals(statut) && d.isObligatoire())
            throw new IllegalArgumentException("Une tâche obligatoire ne peut pas être marquée « non applicable ».");
        d.setStatut(statut);
        d.setCommentaire(req.commentaire() == null || req.commentaire().isBlank() ? null : req.commentaire().trim());
        if ("A_FAIRE".equals(statut)) {
            d.setRealisateur(null);
            d.setDateRealisation(null);
        } else {
            d.setRealisateur(userRepo.getReferenceById(su.getId()));
            d.setDateRealisation(LocalDate.now());
        }
        Onboarding o = d.getOnboarding();
        mettreAJourStatut(o);
        return toDto(o, true, su);
    }

    /** Tâche « BANQUE » faite automatiquement quand la RH saisit les coordonnées bancaires de l'employé. */
    public void tacheRealiseeAutomatiquement(Integer idEmploye, String typeTache, Integer idUser, String commentaire) {
        for (OnboardingDetail d : detailRepo.aFaireParEmployeEtType(idEmploye, typeTache)) {
            d.setStatut("FAIT");
            d.setRealisateur(idUser == null ? null : userRepo.getReferenceById(idUser));
            d.setDateRealisation(LocalDate.now());
            d.setCommentaire(commentaire);
            mettreAJourStatut(d.getOnboarding());
        }
    }

    private void mettreAJourStatut(Onboarding o) {
        detailRepo.flush();
        List<OnboardingDetail> taches = detailRepo.findParOnboarding(o.getId());
        boolean fini = taches.stream().filter(OnboardingDetail::isObligatoire).allMatch(t -> "FAIT".equals(t.getStatut()));
        if (fini && "EN_COURS".equals(o.getStatut())) {
            o.setStatut("TERMINE");
            o.setDateFin(LocalDate.now());
            Employe e = o.getContrat().getAffectation().getEmploye();
            String nom = e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms());
            notificationService.notifierRole("RH", "ONBOARDING_TERMINE", "Onboarding terminé pour " + nom, "/onboarding/" + o.getId());
            Poste sup = o.getContrat().getAffectation().getPoste().getSuperieurDirect();
            if (sup != null) notificationService.notifierPoste(sup.getId(), "ONBOARDING_TERMINE",
                    "Onboarding terminé pour " + nom + " : bienvenue dans l'équipe !", "/personnel/" + e.getId());
        } else if (!fini && "TERMINE".equals(o.getStatut())) {
            o.setStatut("EN_COURS");
            o.setDateFin(null);
        }
    }

    private boolean peutModifier(OnboardingDetail d, SessionUser su) {
        if (su.hasRole("RH") || su.hasRole("ADMIN")) return true;
        return d.getPosteResponsable() != null && postesDe(su.getId()).contains(d.getPosteResponsable().getId());
    }

    /** Postes occupés par l'utilisateur, plus ceux de qui lui délègue aujourd'hui. */
    private Set<Integer> postesDe(Integer idUser) {
        Set<Integer> postes = new HashSet<>();
        var u = userRepo.findById(idUser).orElse(null);
        if (u == null) return postes;
        ajouterPostes(postes, u.getIdEmploye());
        LocalDate j = LocalDate.now();
        delegationRepo.findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(u, j, j)
                .forEach(dl -> ajouterPostes(postes, dl.getUserDelegant().getIdEmploye()));
        return postes;
    }

    private void ajouterPostes(Set<Integer> postes, Integer idEmploye) {
        if (idEmploye == null) return;
        affectationRepo.findEnCoursParEmploye(idEmploye).forEach(a -> postes.add(a.getPoste().getId()));
    }

    // ================================================================== modèles

    @Transactional(readOnly = true)
    public List<ModeleOnboardingDto> modeles() {
        return modeleRepo.findAllByOrderByIdAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ModeleOnboardingDto modele(Integer id) {
        return toDto(trouverModele(id));
    }

    public ModeleOnboardingDto enregistrerModele(Integer id, ModeleOnboardingRequest req) {
        if (req == null || req.libelle() == null || req.libelle().isBlank()) throw new IllegalArgumentException("Le libellé du modèle est obligatoire.");
        if (req.taches() == null || req.taches().isEmpty()) throw new IllegalArgumentException("Un modèle doit contenir au moins une tâche.");
        String statut = req.statut() == null || req.statut().isBlank() ? "ACTIF" : req.statut().trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ACTIF", "INACTIF").contains(statut)) throw new IllegalArgumentException("Statut inconnu : " + statut);
        WorkflowOnboarding m = id == null ? new WorkflowOnboarding() : trouverModele(id);
        m.setLibelle(req.libelle().trim());
        m.setStatut(statut);
        modeleRepo.save(m);

        // tâches retirées : supprimées si jamais utilisées
        Set<Integer> gardees = new HashSet<>();
        req.taches().stream().filter(t -> t.id() != null).forEach(t -> gardees.add(t.id()));
        for (WorkflowOnboardingDetail t : new ArrayList<>(m.getTaches())) {
            if (gardees.contains(t.getId())) continue;
            if (detailRepo.countByModeleId(t.getId()) > 0)
                throw new IllegalStateException("La tâche « " + libelle(t) + " » a déjà servi : elle ne peut pas être retirée du modèle.");
            m.getTaches().remove(t);
            modeleDetailRepo.delete(t);
        }
        modeleDetailRepo.flush();
        // décalage temporaire des ordres pour respecter l'unicité (modèle, ordre) pendant la renumérotation
        for (WorkflowOnboardingDetail t : m.getTaches()) t.setOrdre(t.getOrdre() + 1000);
        modeleDetailRepo.flush();
        int ordre = 1;
        for (ModeleOnboardingRequest.Tache r : req.taches()) {
            if (r.libelle() == null || r.libelle().isBlank()) throw new IllegalArgumentException("Chaque tâche doit avoir un libellé.");
            if (r.delaiJours() != null && r.delaiJours() < 0) throw new IllegalArgumentException("Le délai ne peut pas être négatif.");
            WorkflowOnboardingDetail t = r.id() == null ? new WorkflowOnboardingDetail()
                    : m.getTaches().stream().filter(x -> x.getId().equals(r.id())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Tâche inconnue dans ce modèle : " + r.id()));
            t.setWorkflow(m);
            t.setOrdre(ordre++);
            t.setLibelle(r.libelle().trim());
            t.setTypeTache(r.typeTache() == null || r.typeTache().isBlank() ? code(r.libelle()) : r.typeTache().trim().toUpperCase(Locale.ROOT));
            t.setPosteResponsable(r.idPosteResponsable() == null ? null
                    : posteRepo.findById(r.idPosteResponsable()).orElseThrow(() -> new IllegalArgumentException("Poste introuvable.")));
            t.setDelaiJours(r.delaiJours() == null ? 0 : r.delaiJours());
            t.setObligatoire(r.obligatoire() == null || r.obligatoire());
            modeleDetailRepo.save(t);
            if (r.id() == null) m.getTaches().add(t);
        }
        modeleDetailRepo.flush();
        m.getTaches().sort((a, b) -> a.getOrdre() - b.getOrdre());
        return toDto(m);
    }

    // ================================================================== outils

    private ModeleOnboardingDto toDto(WorkflowOnboarding m) {
        return new ModeleOnboardingDto(m.getId(), m.getLibelle(), m.getStatut(), onboardingRepo.countByWorkflowId(m.getId()),
                m.getTaches().stream().map(t -> new ModeleOnboardingDto.Tache(t.getId(), t.getOrdre(), t.getTypeTache(), libelle(t),
                        t.getPosteResponsable() == null ? null : t.getPosteResponsable().getId(),
                        t.getPosteResponsable() == null ? "Supérieur direct du nouvel employé" : t.getPosteResponsable().getLibelle(),
                        t.getDelaiJours(), t.isObligatoire())).toList());
    }

    private OnboardingDto toDto(Onboarding o, boolean avecTaches, SessionUser su) {
        Affectation a = o.getContrat().getAffectation();
        Employe e = a.getEmploye();
        List<OnboardingDetail> taches = detailRepo.findParOnboarding(o.getId());
        LocalDate j = LocalDate.now();
        int faites = (int) taches.stream().filter(t -> !"A_FAIRE".equals(t.getStatut())).count();
        int retard = (int) taches.stream().filter(t -> "A_FAIRE".equals(t.getStatut()) && t.getDateEcheance() != null && t.getDateEcheance().isBefore(j)).count();
        return new OnboardingDto(o.getId(), e.getId(), e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()), e.getMatricule(),
                a.getPoste().getLibelle(), a.getPoste().getUniteOrga().getLibelle(), o.getContrat().getId(), o.getContrat().getReference(),
                o.getWorkflow().getLibelle(), o.getStatut(), o.getDateDebut(), o.getDateFin(), faites, taches.size(), retard,
                avecTaches ? taches.stream().map(t -> toDto(t, su, false)).toList() : List.of());
    }

    private TacheDto toDto(OnboardingDetail d, SessionUser su, boolean avecEmploye) {
        Employe e = avecEmploye ? d.getOnboarding().getContrat().getAffectation().getEmploye() : null;
        boolean retard = "A_FAIRE".equals(d.getStatut()) && d.getDateEcheance() != null && d.getDateEcheance().isBefore(LocalDate.now());
        return new TacheDto(d.getId(), d.getOnboarding().getId(), d.getOrdre(), d.getTypeTache(), d.getLibelle(),
                d.getPosteResponsable() == null ? null : d.getPosteResponsable().getId(),
                d.getPosteResponsable() == null ? "—" : d.getPosteResponsable().getLibelle(),
                d.isObligatoire(), d.getStatut(), d.getDateEcheance(), retard,
                d.getRealisateur() == null ? null : d.getRealisateur().getLogin(), d.getDateRealisation(), d.getCommentaire(),
                e == null ? null : e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()),
                e == null ? null : e.getMatricule(),
                e == null ? null : d.getOnboarding().getContrat().getAffectation().getPoste().getLibelle(),
                su != null && peutModifier(d, su));
    }

    private static String libelle(WorkflowOnboardingDetail t) {
        return t.getLibelle() != null ? t.getLibelle() : t.getTypeTache();
    }

    private static String code(String libelle) {
        String c = java.text.Normalizer.normalize(libelle, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
        return c.length() > 100 ? c.substring(0, 100) : c;
    }

    private Onboarding trouver(Integer id) {
        return onboardingRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Onboarding introuvable : " + id));
    }

    private WorkflowOnboarding trouverModele(Integer id) {
        return modeleRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modèle introuvable : " + id));
    }
}
