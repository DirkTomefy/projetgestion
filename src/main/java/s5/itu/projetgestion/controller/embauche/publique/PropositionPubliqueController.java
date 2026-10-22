package s5.itu.projetgestion.controller.embauche.publique;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.dto.embauche.MotifRequest;
import s5.itu.projetgestion.dto.embauche.PropositionPubliqueDto;
import s5.itu.projetgestion.dto.embauche.ReponseCandidatRequest;
import s5.itu.projetgestion.service.embauche.PropositionReponseService;

/**
 * Page publique de réponse du candidat (sans compte) : le lien personnel
 * {app.front.url}/proposition/{jeton} appelle ces routes.
 */
@RestController
@RequestMapping("/api/public/propositions/{jeton}")
public class PropositionPubliqueController {

    private final PropositionReponseService service;

    public PropositionPubliqueController(PropositionReponseService service) {
        this.service = service;
    }

    @GetMapping
    public PropositionPubliqueDto consulter(@PathVariable String jeton) {
        return service.consulter(jeton);
    }

    /** Accepter ou refuser chaque condition en attente. */
    @PostMapping("/reponse")
    public PropositionPubliqueDto repondre(@PathVariable String jeton, @Valid @RequestBody ReponseCandidatRequest req) {
        return service.repondre(jeton, req);
    }

    /** Décliner toute la proposition. */
    @PostMapping("/declin")
    public PropositionPubliqueDto decliner(@PathVariable String jeton, @Valid @RequestBody(required = false) MotifRequest req) {
        return service.decliner(jeton, req);
    }
}
