package s5.itu.projetgestion.controller.rh;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.PosteDemandeRequest;
import s5.itu.projetgestion.dto.PosteDto;
import s5.itu.projetgestion.service.rh.PosteService;

@RestController
@RequestMapping("/api/rh")
@CrossOrigin   // pour le front Vue en développement ; à restreindre ensuite
public class PosteController {

    private final PosteService posteService;

    public PosteController(PosteService posteService) {
        this.posteService = posteService;
    }

    @GetMapping("/metiers")
    public List<OptionDto> metiers() { return posteService.listerMetiers(); }

    @GetMapping("/categories")
    public List<OptionDto> categories() { return posteService.listerCategories(); }

    @GetMapping("/echelons")
    public List<OptionDto> echelons() { return posteService.listerEchelons(); }

    @GetMapping("/unites")
    public List<OptionDto> unites() { return posteService.listerUnites(); }

    @GetMapping("/postes/superieurs")
    public List<OptionDto> superieurs() { return posteService.listerSuperieursPossibles(); }

    @PostMapping("/postes/demande")
    public ResponseEntity<PosteDto> demander(@RequestBody PosteDemandeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(posteService.creerDemande(req));
    }
}