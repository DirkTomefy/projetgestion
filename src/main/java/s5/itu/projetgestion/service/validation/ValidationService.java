package s5.itu.projetgestion.service.validation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.finance.BudgetUnite;
import s5.itu.projetgestion.model.validation.Validation;
import s5.itu.projetgestion.model.validation.WorkflowValidation;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.finance.BudgetUniteRepository;
import s5.itu.projetgestion.repository.postes.UniteOrgaRepository;
import s5.itu.projetgestion.repository.validation.ValidationRepository;
import s5.itu.projetgestion.service.UserService;

@Service 
public class ValidationService {
    
    private static final int RH_UO = 3;
    private static final int FI_UO = 2;



    private final ValidationRepository validationRepository;
    private final WorkflowValidationService workflowValidationService;
    private final BudgetUniteRepository budgetUniteRepository;
    private final UserService userService;
    private final BesoinRepository besoinRepository;
    private final UniteOrgaRepository uniteOrgaRepository;
    
    public ValidationService(ValidationRepository validationRepository,
            WorkflowValidationService workflowValidationService,BudgetUniteRepository budgetUniteRepository,
        UserService userService, BesoinRepository besoinRepository, UniteOrgaRepository uniteOrgaRepository) {
        this.validationRepository = validationRepository;
        this.workflowValidationService = workflowValidationService;
        this.budgetUniteRepository = budgetUniteRepository;
        this.userService = userService;
        this.besoinRepository = besoinRepository;
        this.uniteOrgaRepository = uniteOrgaRepository;
    }

    @Transactional 
    public void refuserBesoin(Besoin b, User u) throws Exception{
        WorkflowValidation etapeActuelle = verifierAutorisation(b, u);

        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("REFUSE");
        validation.setUser(u);
        validation.setWorkflowValidation(etapeActuelle);
        validationRepository.save(validation);

        b.setEtatActuel("ANNULE");
        besoinRepository.save(b);
    }


    @Transactional 
    public void validerBesoin(Besoin b, User u) throws Exception {
        WorkflowValidation etapeActuelle = verifierAutorisation(b, u);

        Validation validation = new Validation();
        validation.setBesoin(b);
        validation.setDate(LocalDateTime.now());
        validation.setDecision("VALIDE");
        validation.setUser(u);
        validation.setWorkflowValidation(etapeActuelle);

        validationRepository.save(validation);

        boolean derniereEtape = workflowValidationService.estDerniereEtape(etapeActuelle);
        b.setEtatActuel(derniereEtape ? "OUVERT" : "EN_ATTENTE");
        besoinRepository.save(b);
    }

    private WorkflowValidation verifierAutorisation(Besoin b, User u) throws Exception {
        if (b == null || b.getId() == null) {
            throw new Exception("Besoin invalide");
        }
        if (u == null || u.getId() == null) {
            throw new Exception("Utilisateur invalide");
        }

        WorkflowValidation etapeActuelle = workflowValidationService.getEtapeActuelleBesoin(b);
        var posteUtilisateur = userService.getPosteByUser(u);
        if (etapeActuelle.getPoste() == null || posteUtilisateur == null
                || !etapeActuelle.getPoste().getId().equals(posteUtilisateur.getId())) {
            throw new Exception("Ce n'est pas à vous de valider cette demande");
        }
        return etapeActuelle;
    }

    @Transactional(readOnly = true)
    public boolean validerBesoinFinance(Besoin b, User u) throws Exception{
        if (b == null || b.getBudgetEstime() == null || b.getEffectif() == null || b.getEffectif() <= 0) {
            throw new Exception("Montant ou effectif du besoin invalide");
        }
        if (u == null) {
            throw new Exception("Utilisateur invalide");
        }

        var posteUtilisateur = userService.getPosteByUser(u);
        BudgetUnite bu = budgetUniteRepository.findByUniteOrga(posteUtilisateur.getUniteOrga());
        if (bu == null || bu.getMontant() == null) {
            return false;
        }

        double coutAnnuel = b.getBudgetEstime() * b.getEffectif() * 12;
        return coutAnnuel <= bu.getMontant();
    }
    





}
