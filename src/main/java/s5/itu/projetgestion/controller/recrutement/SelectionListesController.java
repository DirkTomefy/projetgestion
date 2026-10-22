package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import s5.itu.projetgestion.dto.recrutement.CandidatureRetenueDto;
import s5.itu.projetgestion.dto.recrutement.MesEvaluationsDto;
import s5.itu.projetgestion.service.recrutement.EvaluationService;
import s5.itu.projetgestion.service.recrutement.SelectionService;

import java.util.List;

/** P5 : listes issues de la sélection. */
@RestController
public class SelectionListesController {

    private final SelectionService selectionService;
    private final EvaluationService evaluationService;

    public SelectionListesController(SelectionService selectionService, EvaluationService evaluationService) {
        this.selectionService = selectionService;
        this.evaluationService = evaluationService;
    }

    /** Interface P5 → P4 : candidatures RETENUE sans proposition en cours. */
    @GetMapping("/api/rh/candidatures-retenues")
    public List<CandidatureRetenueDto> retenues(HttpSession session) {
        SessionRh.rh(session);
        return selectionService.retenuesSansProposition();
    }

    /** Espace évaluateur : tests et entretiens à venir pour le poste de l'utilisateur connecté. */
    @GetMapping("/api/selection/mes-evaluations")
    public MesEvaluationsDto mesEvaluations(HttpSession session) {
        return evaluationService.mesEvaluations(SessionRh.connecte(session));
    }
}
