package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.dto.recrutement.NiveauSelectionDto;
import s5.itu.projetgestion.dto.recrutement.NiveauSelectionRequest;
import s5.itu.projetgestion.dto.recrutement.NiveauxSelectionDto;
import s5.itu.projetgestion.service.recrutement.NiveauSelectionService;

@RestController
@RequestMapping("/api/rh")
public class NiveauSelectionController {

    private final NiveauSelectionService service;

    public NiveauSelectionController(NiveauSelectionService service) {
        this.service = service;
    }

    /** Niveaux communs, ou niveaux qui s'appliquent à une offre (?idOffre=). */
    @GetMapping("/niveaux-selection")
    public NiveauxSelectionDto lister(@RequestParam(required = false) Integer idOffre, HttpSession session) {
        SessionRh.rh(session);
        return service.lister(idOffre);
    }

    @PostMapping("/niveaux-selection")
    public NiveauSelectionDto creer(@Valid @RequestBody NiveauSelectionRequest req, HttpSession session) {
        SessionRh.rh(session);
        return service.creer(req);
    }

    @PutMapping("/niveaux-selection/{id}")
    public NiveauSelectionDto modifier(@PathVariable Integer id, @Valid @RequestBody NiveauSelectionRequest req,
                                       HttpSession session) {
        SessionRh.rh(session);
        return service.modifier(id, req);
    }

    @DeleteMapping("/niveaux-selection/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id, HttpSession session) {
        SessionRh.rh(session);
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** L'offre reçoit une copie des niveaux communs, modifiable ensuite. */
    @PostMapping("/offres/{idOffre}/niveaux-selection")
    public NiveauxSelectionDto personnaliser(@PathVariable Integer idOffre, HttpSession session) {
        SessionRh.rh(session);
        return service.personnaliser(idOffre);
    }

    /** L'offre abandonne ses niveaux propres et reprend les niveaux communs. */
    @DeleteMapping("/offres/{idOffre}/niveaux-selection")
    public NiveauxSelectionDto reprendreCommuns(@PathVariable Integer idOffre, HttpSession session) {
        SessionRh.rh(session);
        return service.reprendreCommuns(idOffre);
    }
}
