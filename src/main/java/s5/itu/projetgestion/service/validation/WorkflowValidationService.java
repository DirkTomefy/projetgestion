package s5.itu.projetgestion.service.validation;

import org.springframework.stereotype.Service;

import java.util.Optional;

import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.validation.Validation;
import s5.itu.projetgestion.model.validation.WorkflowValidation;
import s5.itu.projetgestion.repository.validation.ValidationRepository;
import s5.itu.projetgestion.repository.validation.WorkflowValidationRepository;

@Service 
public class WorkflowValidationService {

    private final WorkflowValidationRepository workflowValidationRepository;
    private final ValidationRepository validationRepository;

    public WorkflowValidationService(WorkflowValidationRepository workflowValidationRepository, ValidationRepository validationRepository) {
        this.workflowValidationRepository = workflowValidationRepository;
        this.validationRepository = validationRepository;
    }

    public WorkflowValidation getEtapeActuelleBesoin(Besoin b) throws Exception{
        Optional<Validation> derniere = validationRepository.findFirstByBesoinOrderByDateDesc(b);
        int ordre = derniere
                .map(validation -> validation.getWorkflowValidation().getOrdre() + 1)
                .orElse(1);

        return workflowValidationRepository.findByOrdre(ordre)
                .orElseThrow(() -> new Exception("Aucune étape de validation disponible pour ce besoin"));
    }

    public boolean estDerniereEtape(WorkflowValidation etape) {
        return workflowValidationRepository.findByOrdre(etape.getOrdre() + 1).isEmpty();

    }


}
