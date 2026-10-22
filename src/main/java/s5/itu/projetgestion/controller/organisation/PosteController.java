package s5.itu.projetgestion.controller.organisation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import s5.itu.projetgestion.dto.organisation.EtatRequest;
import s5.itu.projetgestion.dto.organisation.PosteDto;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.organisation.PosteDemandeRequest;
import s5.itu.projetgestion.service.organisation.PosteService;

@RestController
@RequestMapping("/api/postes")
public class PosteController {

    private final PosteService posteService;

    public PosteController(PosteService posteService) {
        this.posteService = posteService;
    }

    // GET /api/postes?etat=PROPOSE
    @GetMapping
    public List<PosteDto> lister(@RequestParam(defaultValue = "PROPOSE") String etat) {
        return posteService.listerParEtat(etat);
    }

    // PUT /api/postes/{id}/etat   { "etat": "ACTIF" | "REFUSE" }
    @PutMapping("/{id}/etat")
    public PosteDto changerEtat(@PathVariable Integer id, @RequestBody EtatRequest request) {
        return posteService.traiterProposition(id, request.etat());
    }

}