package s5.itu.projetgestion.controller.organisation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.organisation.PosteDemandeRequest;
import s5.itu.projetgestion.dto.organisation.PosteDto;
import s5.itu.projetgestion.service.organisation.PosteService;

@RestController
@RequestMapping("/api/rh")
public class PosteDemandeController {

    private final PosteService posteService;

    public PosteDemandeController(PosteService posteService) {
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
    public ResponseEntity<PosteDto> demander(@RequestBody PosteDemandeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(posteService.creerDemande(request));
    }
}