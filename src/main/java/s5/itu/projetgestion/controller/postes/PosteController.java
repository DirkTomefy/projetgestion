package s5.itu.projetgestion.controller.postes;

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

import s5.itu.projetgestion.dto.postes.EtatRequest;
import s5.itu.projetgestion.dto.postes.PosteDto;
import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.PosteDemandeRequest;
import s5.itu.projetgestion.service.postes.PosteService;

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

    @GetMapping("/api/rh/metiers")
    public List<OptionDto> metiers() { return posteService.listerMetiers(); }

    @GetMapping("/api/rh/categories")
    public List<OptionDto> categories() { return posteService.listerCategories(); }

    @GetMapping("/api/rh/echelons")
    public List<OptionDto> echelons() { return posteService.listerEchelons(); }

    @GetMapping("/api/rh/unites")
    public List<OptionDto> unites() { return posteService.listerUnites(); }

    @GetMapping("/api/rh/postes/superieurs")
    public List<OptionDto> superieurs() { return posteService.listerSuperieursPossibles(); }

    @org.springframework.web.bind.annotation.PostMapping("/api/rh/postes/demande")
    public ResponseEntity<PosteDto> demander(@RequestBody PosteDemandeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(posteService.creerDemande(request));
    }
}