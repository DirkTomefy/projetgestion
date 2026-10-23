package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.dto.recrutement.CommentaireRequest;
import s5.itu.projetgestion.dto.recrutement.SelectionDto;
import s5.itu.projetgestion.service.recrutement.SelectionService;

@RestController
@RequestMapping("/api/rh/candidatures/{id}")
public class SelectionController {

    private final SelectionService service;

    public SelectionController(SelectionService service) {
        this.service = service;
    }

    /** Évaluations, historique et actions possibles. */
    @GetMapping("/selection")
    public SelectionDto dossier(@PathVariable Integer id, HttpSession session) {
        SessionRh.rh(session);
        return service.dossier(id);
    }

    @PostMapping("/preselection")
    public SelectionDto preselectionner(@PathVariable Integer id, @Valid @RequestBody CommentaireRequest req,
                                        HttpSession session) {
        Integer idUser = SessionRh.rh(session).getId();
        return service.preselectionner(id, idUser, req.commentaire());
    }

    @PostMapping("/refus")
    public SelectionDto refuser(@PathVariable Integer id, @Valid @RequestBody CommentaireRequest req,
                                HttpSession session) {
        Integer idUser = SessionRh.rh(session).getId();
        return service.refuser(id, idUser, req.commentaire());
    }
}
