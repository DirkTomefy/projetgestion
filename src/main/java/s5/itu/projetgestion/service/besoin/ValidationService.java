package s5.itu.projetgestion.service.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.err.validation.AutorisationValidationException;
import s5.itu.projetgestion.err.validation.BesoinInvalideException;
import s5.itu.projetgestion.err.validation.UtilisateurInvalideException;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.besoin.Validation;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.recrutement.Delegation;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.besoin.ValidationRepository;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;
import s5.itu.projetgestion.service.auth.UserService;
import s5.itu.projetgestion.service.finance.BudgetService;
import s5.itu.projetgestion.service.notification.NotificationService;

/**
 * Circuit de validation des besoins (workflow_validation) :
 * chaque étape est validée par l'occupant du poste valideur (ou son délégué) ;
 * une étape « BUDGET » est validée d'office si le coût annuel estimé tient dans le budget de l'unité ;
 * à la dernière étape, le besoin passe OUVERT, ses sièges sont à pourvoir et les postes créés par le besoin deviennent ACTIF ;
 * un refus annule le besoin (commentaire obligatoire) et refuse les postes qu'il proposait.
 */
@Service
public class ValidationService {

    public static final String LOGIN_SYSTEME = "systeme";

    private final ValidationRepository validationRepository;
    private final WorkflowValidationService workflowValidationService;
    private final UserService userService;
    private final BesoinRepository besoinRepository;
    private final DelegationRepository delegationRepository;
    private final BesoinPosteRepository besoinPosteRepository;
    private final UserRepository userRepository;
    private final HistoBesoinService histo;
    private final NotificationService notificationService;
    private final BudgetService budgetService;

    public ValidationService(ValidationRepository validationRepository,
            WorkflowValidationService workflowValidationService, UserService userService,
            BesoinRepository besoinRepository, DelegationRepository delegationRepository,
            BesoinPosteRepository besoinPosteRepository, UserRepository userRepository,
            HistoBesoinService histo, NotificationService notificationService, BudgetService budgetService) {
        this.validationRepository = validationRepository;
        this.workflowValidationService = workflowValidationService;
        this.userService = userService;
        this.besoinRepository = besoinRepository;
        this.delegationRepository = delegationRepository;
        this.besoinPosteRepository = besoinPosteRepository;
        this.userRepository = userRepository;
        this.histo = histo;
        this.notificationService = notificationService;
        this.budgetService = budgetService;
    }

    // ------------------------------------------------------------------ décisions

    @Transactional
    public void validerBesoin(Besoin b, User u) throws Exception {
        validerBesoin(b, u, null);
    }

    @Transactional
    public void validerBesoin(Besoin b, User u, String commentaire) throws Exception {
        Autorisation autorisation = verifierAutorisation(b, u);
        String texte = joindre(commentaireDelegation(autorisation.delegation()), commentaire);
        enregistrerValidation(b, u, autorisation.etape(), texte, HistoBesoinService.VALIDATION);
        avancerAutomatiquement(b);
    }

    @Transactional
    public void refuserBesoin(Besoin b, User u) throws Exception {
        refuserBesoin(b, u, null);
    }

    @Transactional
    public void refuserBesoin(Besoin b, User u, String commentaire) throws Exception {
        Autorisation autorisation = verifierAutorisation(b, u);
        if (commentaire == null || commentaire.isBlank())
            throw new BesoinInvalideException("Le motif du refus est obligatoire.");

        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("REFUSE");
        validation.setUser(u);
        validation.setWorkflowValidation(autorisation.etape());
        validation.setCommentaire(joindre(commentaireDelegation(autorisation.delegation()), commentaire));
        validationRepository.save(validation);

        String avant = b.getEtatActuel();
        b.setEtatActuel("ANNULE");
        besoinRepository.save(b);
        for (BesoinPoste bp : besoinPosteRepository.findByBesoinId(b.getId())) {
            bp.setEtat("ANNULE");
            if ("PROPOSE".equals(bp.getPoste().getEtat())) bp.getPoste().setEtat("REFUSE");   // poste proposé par ce besoin
        }
        histo.tracer(b, avant, "ANNULE", HistoBesoinService.REFUS, u,
                "Refusé à l'étape « " + autorisation.etape().getPoste().getLibelle() + " » : " + commentaire.trim());
        notificationService.notifier(b.getUser().getId(), "BESOIN_REFUSE",
                "Votre besoin " + b.getReference() + " a été refusé : " + commentaire.trim(), "/besoins/" + b.getId());
    }

    /**
     * Après la soumission ou une validation : valide d'office les étapes « BUDGET » quand le coût tient dans le budget,
     * puis prévient les valideurs de l'étape suivante.
     */
    @Transactional
    public void avancerAutomatiquement(Besoin b) throws Exception {
        while ("EN_ATTENTE".equals(b.getEtatActuel())) {
            List<WorkflowValidation> etapes = workflowValidationService.getEtapeActuelleBesoin(b);
            if (etapes.isEmpty()) return;
            WorkflowValidation budget = etapes.stream().filter(e -> "BUDGET".equals(e.getControle())).findFirst().orElse(null);
            ControleBudget cb = controleBudget(b);
            if (budget != null && cb.dansLeBudget()) {
                User systeme = userRepository.findByLogin(LOGIN_SYSTEME).orElse(null);
                if (systeme == null) break;
                enregistrerValidation(b, systeme, budget,
                        "Validation automatique : coût annuel estimé " + format(cb.coutAnnuel()) + " ≤ disponible " + format(cb.disponible())
                                + " (" + b.getUniteOrga().getLibelle() + ", " + cb.exercice() + " : budget " + format(cb.budget())
                                + ", masse salariale " + format(cb.masseSalariale()) + ", autres besoins " + format(cb.autresBesoins()) + ")",
                        HistoBesoinService.VALIDATION_AUTO);
                continue;
            }
            for (WorkflowValidation e : etapes) {
                String detail = "BUDGET".equals(e.getControle()) && cb.budget() != null
                        ? " (hors budget : " + format(cb.coutAnnuel()) + " pour un disponible de " + format(cb.disponible()) + ")" : "";
                notificationService.notifierPoste(e.getPoste().getId(), NotificationService.VALIDATION_EN_ATTENTE,
                        "Le besoin " + b.getReference() + " (" + b.getUniteOrga().getLibelle() + ") attend votre validation" + detail,
                        "/besoins/" + b.getId());
            }
            return;
        }
    }

    private void enregistrerValidation(Besoin b, User u, WorkflowValidation etape, String commentaire, String action) {
        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("VALIDE");
        validation.setUser(u);
        validation.setWorkflowValidation(etape);
        validation.setCommentaire(commentaire);
        validationRepository.save(validation);

        boolean derniereEtape = workflowValidationService.estDerniereEtape(etape);
        histo.tracer(b, b.getEtatActuel(), derniereEtape ? "OUVERT" : "EN_ATTENTE", action, u,
                "Étape " + etape.getOrdre() + " — " + etape.getPoste().getLibelle() + (commentaire == null ? "" : " : " + commentaire));
        if (!derniereEtape) return;

        b.setEtatActuel("OUVERT");
        besoinRepository.save(b);
        // tous les sièges deviennent à pourvoir ; les postes créés par ce besoin sont activés avec lui
        for (BesoinPoste bp : besoinPosteRepository.findByBesoinId(b.getId())) {
            bp.setEtat("OUVERT");
            if ("PROPOSE".equals(bp.getPoste().getEtat())) bp.getPoste().setEtat("ACTIF");
        }
        histo.tracer(b, "OUVERT", "OUVERT", HistoBesoinService.OUVERTURE, u, "Circuit de validation terminé : recrutement ouvert");
        notificationService.notifier(b.getUser().getId(), "BESOIN_VALIDE",
                "Votre besoin " + b.getReference() + " est validé : le recrutement est ouvert.", "/besoins/" + b.getId());
        notificationService.notifierRole("RH", "BESOIN_VALIDE",
                "Le besoin " + b.getReference() + " (" + b.getUniteOrga().getLibelle() + ") est validé : l'offre d'emploi peut être publiée.",
                "/offres/besoins");
    }

    // ------------------------------------------------------------------ consultation

    /** Étapes attendues pour ce besoin (vide s'il n'est plus en attente). */
    @Transactional(readOnly = true)
    public List<WorkflowValidation> etapesCourantes(Besoin b) {
        if (!"EN_ATTENTE".equals(b.getEtatActuel())) return List.of();
        try {
            return workflowValidationService.getEtapeActuelleBesoin(b);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** L'utilisateur peut-il valider ou refuser ce besoin maintenant (titulaire du poste valideur ou délégué) ? */
    @Transactional(readOnly = true)
    public boolean peutDecider(Besoin b, User u) {
        try {
            verifierAutorisation(b, u);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Coût annuel estimé du besoin comparé au disponible de l'unité pour l'exercice de la date souhaitée :
     * budget validé − masse salariale annuelle des contrats en cours − coût des autres besoins en cours.
     */
    @Transactional(readOnly = true)
    public ControleBudget controleBudget(Besoin b) {
        int exercice = (b.getDateSouhaitee() != null ? b.getDateSouhaitee() : LocalDate.now()).getYear();
        BigDecimal cout = b.getBudgetEstime() == null ? null : BigDecimal.valueOf(b.getBudgetEstime());
        boolean compte = "EN_ATTENTE".equals(b.getEtatActuel()) || "OUVERT".equals(b.getEtatActuel());   // déjà dans les besoins en cours
        BudgetService.Engagement eg = budgetService.engagement(b.getUniteOrga().getId(), exercice, compte ? cout : null);
        return new ControleBudget(exercice, cout, eg.budget(), eg.masseSalariale(), eg.autresBesoins(), eg.disponible(),
                cout != null && eg.disponible() != null && cout.compareTo(eg.disponible()) <= 0);
    }

    public record ControleBudget(int exercice, BigDecimal coutAnnuel, BigDecimal budget, BigDecimal masseSalariale,
                                 BigDecimal autresBesoins, BigDecimal disponible, boolean dansLeBudget) { }

    // ------------------------------------------------------------------ autorisation

    private Autorisation verifierAutorisation(Besoin b, User u) throws Exception {
        if (b == null || b.getId() == null) {
            throw new BesoinInvalideException("Besoin invalide");
        }
        if (u == null || u.getId() == null) {
            throw new UtilisateurInvalideException("Utilisateur invalide");
        }
        if (!"EN_ATTENTE".equals(b.getEtatActuel())) {
            throw new BesoinInvalideException("Ce besoin n'est plus en attente de validation (état " + b.getEtatActuel() + ")");
        }
        List<WorkflowValidation> etapesActuelle = workflowValidationService.getEtapeActuelleBesoin(b);
        if (etapesActuelle.isEmpty()) {
            throw new AutorisationValidationException("Aucune étape de validation disponible pour ce besoin");
        }
        Poste posteUtilisateur = posteOuNull(u);
        for (WorkflowValidation etape : etapesActuelle) {
            if (posteUtilisateur != null && etape.getPoste().getId().equals(posteUtilisateur.getId())) {
                return new Autorisation(etape, null);
            }
        }
        LocalDate aujourdHui = LocalDate.now();
        List<Delegation> delegations = delegationRepository
                .findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(u, aujourdHui, aujourdHui);
        for (Delegation delegation : delegations) {
            Poste posteDelegant = posteOuNull(delegation.getUserDelegant());
            for (WorkflowValidation etape : etapesActuelle) {
                if (posteDelegant != null && etape.getPoste().getId().equals(posteDelegant.getId())) {
                    return new Autorisation(etape, delegation);
                }
            }
        }
        throw new AutorisationValidationException("Ce n'est pas à vous de valider cette demande");
    }

    /** Poste occupé par l'utilisateur, ou null s'il n'est rattaché à aucun employé affecté. */
    private Poste posteOuNull(User u) {
        try {
            return userService.getPosteByUser(u);
        } catch (Exception e) {
            return null;
        }
    }

    private String commentaireDelegation(Delegation delegation) {
        if (delegation == null) {
            return null;
        }
        String nomDelegant = delegation.getUserDelegant().getLogin();
        return "Par délégation de " + (nomDelegant == null ? delegation.getUserDelegant().getId() : nomDelegant);
    }

    private static String joindre(String a, String b) {
        boolean va = a != null && !a.isBlank(), vb = b != null && !b.isBlank();
        if (va && vb) return a + " — " + b.trim();
        return va ? a : vb ? b.trim() : null;
    }

    private static String format(BigDecimal v) {
        return v == null ? "—" : String.format(java.util.Locale.FRANCE, "%,.0f Ar", v);
    }

    private record Autorisation(WorkflowValidation etape, Delegation delegation) {}
}
