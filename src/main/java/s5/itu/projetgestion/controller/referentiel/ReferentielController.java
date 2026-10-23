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

    /**
     * Référentiels d'organisation (métiers, catégories, échelons, types de contrat) : modifiés par le DG.
     * Référentiels du recrutement (motifs, types de document, types d'information du CV) : RH, DG ou administrateur.
     */
    public static final java.util.Set<String> ORGANISATION = java.util.Set.of("categories", "echelons", "metiers", "types-contrat");

    private static void ecriture(String ref, HttpSession session) {
        if (ORGANISATION.contains(ref)) Acces.exiger(session, Acces.DG);
        else Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
    }

    /** Référentiels que l'utilisateur connecté peut modifier. */
    @GetMapping("/modifiables")
    public List<String> modifiables(HttpSession session) {
        var su = Acces.connecte(session);
        return ReferentielService.REFERENTIELS.keySet().stream().sorted()
                .filter(r -> ORGANISATION.contains(r) ? su.hasRole(Acces.DG)
                        : su.hasRole(Acces.RH) || su.hasRole(Acces.DG) || su.hasRole(Acces.ADMIN))
                .toList();
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
        ecriture(ref, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(ref, req));
    }

    @PutMapping("/{ref}/{id}")
    public ReferentielDto modifier(@PathVariable String ref, @PathVariable Integer id, @RequestBody ReferentielRequest req, HttpSession session) {
        ecriture(ref, session);
        return service.modifier(ref, id, req);
    }

    @DeleteMapping("/{ref}/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String ref, @PathVariable Integer id, HttpSession session) {
        ecriture(ref, session);
        service.supprimer(ref, id);
        return ResponseEntity.noContent().build();
    }
}
