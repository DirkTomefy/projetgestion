package s5.itu.projetgestion.service.besoin;

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
import s5.itu.projetgestion.model.besoin.Validation;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.finance.BudgetUnite;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.recrutement.Delegation;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.finance.BudgetUniteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;
import s5.itu.projetgestion.repository.besoin.ValidationRepository;
import s5.itu.projetgestion.service.auth.UserService;

@Service
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final WorkflowValidationService workflowValidationService;
    private final BudgetUniteRepository budgetUniteRepository;
    private final UserService userService;
    private final BesoinRepository besoinRepository;
    private final UniteOrgaRepository uniteOrgaRepository;
    private final DelegationRepository delegationRepository;

    public ValidationService(ValidationRepository validationRepository,
            WorkflowValidationService workflowValidationService, BudgetUniteRepository budgetUniteRepository,
            UserService userService, BesoinRepository besoinRepository, UniteOrgaRepository uniteOrgaRepository,
        DelegationRepository delegationRepository) {
        this.validationRepository = validationRepository;
        this.workflowValidationService = workflowValidationService;
        this.budgetUniteRepository = budgetUniteRepository;
        this.userService = userService;
        this.besoinRepository = besoinRepository;
        this.uniteOrgaRepository = uniteOrgaRepository;
        this.delegationRepository = delegationRepository;
    }

    @Transactional
    public void refuserBesoin(Besoin b, User u) throws Exception {
        Autorisation autorisation = verifierAutorisation(b, u);

        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("REFUSE");
        validation.setUser(u);
        validation.setWorkflowValidation(autorisation.etape());
        validation.setCommentaire(commentaireDelegation(autorisation.delegation()));
        validationRepository.save(validation);

        b.setEtatActuel("ANNULE");
        besoinRepository.save(b);
    }

    @Transactional
    public void validerBesoin(Besoin b, User u) throws Exception {
        Autorisation autorisation = verifierAutorisation(b, u);

        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("VALIDE");
        validation.setUser(u);
        validation.setWorkflowValidation(autorisation.etape());
        validation.setCommentaire(commentaireDelegation(autorisation.delegation()));

        validationRepository.save(validation);

        boolean derniereEtape = workflowValidationService.estDerniereEtape(autorisation.etape());
        b.setEtatActuel(derniereEtape ? "OUVERT" : "EN_ATTENTE");
        besoinRepository.save(b);
    }

    private Autorisation verifierAutorisation(Besoin b, User u) throws Exception {
        if (b == null || b.getId() == null) {
            throw new BesoinInvalideException("Besoin invalide");
        }

        if (u == null || u.getId() == null) {
            throw new UtilisateurInvalideException("Utilisateur invalide");
        }
        List<WorkflowValidation> etapesActuelle = workflowValidationService.getEtapeActuelleBesoin(b);
        if (etapesActuelle.isEmpty()) {
            throw new AutorisationValidationException(
                    "Aucune étape de validation disponible pour ce besoin");
        }
        Poste posteUtilisateur = userService.getPosteByUser(u);
        for (WorkflowValidation etape : etapesActuelle) {
            if (etape.getPoste().getId().equals(posteUtilisateur.getId())) {
                return new Autorisation(etape, null);
            }
        }

        LocalDate aujourdHui = LocalDate.now();
        List<Delegation> delegations = delegationRepository
                .findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(u, aujourdHui, aujourdHui);
        for (Delegation delegation : delegations) {
            Poste posteDelegant = userService.getPosteByUser(delegation.getUserDelegant());
            for (WorkflowValidation etape : etapesActuelle) {
                if (etape.getPoste().getId().equals(posteDelegant.getId())) {
                    return new Autorisation(etape, delegation);
                }
            }
        }
        throw new AutorisationValidationException("Ce n'est pas à vous de valider cette demande");
    }

    private String commentaireDelegation(Delegation delegation) {
        if (delegation == null) {
            return null;
        }
        String nomDelegant = delegation.getUserDelegant().getLogin();
        return "Par délégation de " + (nomDelegant == null ? delegation.getUserDelegant().getId() : nomDelegant);
    }

    private record Autorisation(WorkflowValidation etape, Delegation delegation) {}

    @Transactional(readOnly = true)
    public boolean validerBesoinFinance(Besoin b, User u) throws Exception {
        if (b == null || b.getBudgetEstime() == null
                || b.getEffectif() == null || b.getEffectif() <= 0) {
            throw new BesoinInvalideException(
                    "Montant ou effectif du besoin invalide");
        }

        if (u == null || u.getId() == null) {
            throw new UtilisateurInvalideException("Utilisateur invalide");
        }

        Poste posteUtilisateur = userService.getPosteByUser(u);

        if (posteUtilisateur == null || posteUtilisateur.getUniteOrga() == null) {
            throw new AutorisationValidationException(
                    "L'utilisateur n'est affecté à aucun poste valide");
        }

        BudgetUnite bu = budgetUniteRepository
                .findByUniteOrga(posteUtilisateur.getUniteOrga());

        if (bu == null || bu.getMontant() == null) {
            return false;
        }

        double coutAnnuel = b.getBudgetEstime() * b.getEffectif() * 12;

        return coutAnnuel <= bu.getMontant();
    }

}
