package s5.itu.projetgestion.controller.referentiel;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.referentiel.ReferentielDto;
import s5.itu.projetgestion.dto.referentiel.ReferentielRequest;
import s5.itu.projetgestion.service.referentiel.ReferentielService;

/** Référentiels : lecture pour tout utilisateur connecté, modification par l'ADMIN ou la RH. */
@RestController
@RequestMapping("/api/referentiels")
public class ReferentielController {

    private final ReferentielService service;

    public ReferentielController(ReferentielService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, String> referentiels(HttpSession session) {
        Acces.connecte(session);
        return ReferentielService.REFERENTIELS;
    }

    @GetMapping("/{ref}")
    public List<ReferentielDto> lister(@PathVariable String ref, HttpSession session) {
        Acces.connecte(session);
        return service.lister(ref);
    }

    @PostMapping("/{ref}")
    public ResponseEntity<ReferentielDto> creer(@PathVariable String ref, @RequestBody ReferentielRequest req, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN, Acces.RH);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(ref, req));
    }

    @PutMapping("/{ref}/{id}")
    public ReferentielDto modifier(@PathVariable String ref, @PathVariable Integer id, @RequestBody ReferentielRequest req, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN, Acces.RH);
        return service.modifier(ref, id, req);
    }

    @DeleteMapping("/{ref}/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String ref, @PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN, Acces.RH);
        service.supprimer(ref, id);
        return ResponseEntity.noContent().build();
    }
}
