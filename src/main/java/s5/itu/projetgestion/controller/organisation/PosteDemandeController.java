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

    private final s5.itu.projetgestion.service.organisation.PerimetreService perimetre;

    public PosteDemandeController(PosteService posteService, s5.itu.projetgestion.service.organisation.PerimetreService perimetre) {
        this.posteService = posteService;
        this.perimetre = perimetre;
    }

    @GetMapping("/metiers")
    public List<OptionDto> metiers() { return posteService.listerMetiers(); }

    @GetMapping("/categories")
    public List<OptionDto> categories() { return posteService.listerCategories(); }

    @GetMapping("/echelons")
    public List<OptionDto> echelons() { return posteService.listerEchelons(); }

    @GetMapping("/unites")
    public List<OptionDto> unites() { return posteService.listerUnites(); }

    /** Supérieurs possibles : postes actifs de l'unité donnée et des unités dont elle dépend (tous sans unité). */
    @GetMapping("/postes/superieurs")
    public List<OptionDto> superieurs(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer idUnite) {
        return posteService.listerSuperieursPossibles(idUnite);
    }

    /** Demande de poste hors besoin (Manager, RH, DG), dans le périmètre du demandeur. */
    @PostMapping("/postes/demande")
    public ResponseEntity<PosteDto> demander(@RequestBody PosteDemandeRequest request, jakarta.servlet.http.HttpSession session) {
        var su = s5.itu.projetgestion.config.Acces.exiger(session, s5.itu.projetgestion.config.Acces.MANAGER,
                s5.itu.projetgestion.config.Acces.RH, s5.itu.projetgestion.config.Acces.DG);
        perimetre.verifierUnite(su, request.idUniteOrga());
        return ResponseEntity.status(HttpStatus.CREATED).body(posteService.demanderPoste(request, su.getNomComplet() != null ? su.getNomComplet() : su.getLogin(), su.hasRole("DG")));
    }
}