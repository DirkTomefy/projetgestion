package s5.itu.projetgestion.service.besoin;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.Validation;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.repository.besoin.ValidationRepository;
import s5.itu.projetgestion.repository.besoin.WorkflowValidationRepository;

@Service
public class WorkflowValidationService {

    private final WorkflowValidationRepository workflowValidationRepository;
    private final ValidationRepository validationRepository;

    public WorkflowValidationService(WorkflowValidationRepository workflowValidationRepository,
            ValidationRepository validationRepository) {
        this.workflowValidationRepository = workflowValidationRepository;
        this.validationRepository = validationRepository;
    }

    public List<WorkflowValidation> getEtapeActuelleBesoin(Besoin b) throws Exception {
        // L'étape attendue suit la dernière étape VALIDÉE. La ligne « Demande soumise » enregistrée à la
        // création porte l'étape 1 avec la décision EN_ATTENTE : la compter faisait sauter la validation RH.
        int ordre = validationRepository.findFirstByBesoinAndDecisionOrderByDateDescIdDesc(b, "VALIDE")
                .map(validation -> validation.getWorkflowValidation().getOrdre() + 1)
                .orElse(1);

        return workflowValidationRepository.findByOrdre(ordre);
    }

    public boolean estDerniereEtape(WorkflowValidation etape) {
        return workflowValidationRepository.findByOrdre(etape.getOrdre() + 1).isEmpty();

    }

}
