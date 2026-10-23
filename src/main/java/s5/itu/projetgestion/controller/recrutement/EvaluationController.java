package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.dto.recrutement.PlanificationRequest;
import s5.itu.projetgestion.dto.recrutement.PosteOptionDto;
import s5.itu.projetgestion.dto.recrutement.ResultatRequest;
import s5.itu.projetgestion.dto.recrutement.SelectionDto;
import s5.itu.projetgestion.service.recrutement.EvaluationService;

import java.util.List;

@RestController
public class EvaluationController {

    private final EvaluationService service;

    public EvaluationController(EvaluationService service) {
        this.service = service;
    }

    @GetMapping("/api/rh/selection/postes")
    public List<PosteOptionDto> postes(HttpSession session) {
        SessionRh.rh(session);
        return service.postesResponsables();
    }

    /** Postes au nom desquels l'utilisateur connecté peut saisir un résultat (occupés ou délégués). */
    @GetMapping("/api/selection/mes-postes")
    public List<Integer> mesPostes(HttpSession session) {
        return service.mesPostes(SessionRh.connecte(session));
    }

    @PutMapping("/api/rh/evaluations/{id}/planification")
    public SelectionDto planifier(@PathVariable Integer id, @Valid @RequestBody PlanificationRequest req,
                                  HttpSession session) {
        SessionRh.rh(session);
        return service.planifier(id, req);
    }

    @PostMapping("/api/selection/evaluations/{id}/resultat")
    public SelectionDto resultat(@PathVariable Integer id, @Valid @RequestBody ResultatRequest req,
                                 HttpSession session) {
        return service.saisirResultat(id, req, SessionRh.connecte(session));
    }
}
