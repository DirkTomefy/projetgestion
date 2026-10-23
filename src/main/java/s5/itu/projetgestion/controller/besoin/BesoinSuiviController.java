package s5.itu.projetgestion.controller.besoin;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.besoin.BesoinFicheDto;
import s5.itu.projetgestion.dto.besoin.BesoinLigneDto;
import s5.itu.projetgestion.service.besoin.BesoinSuiviService;

/** Suivi des besoins (visible par tout utilisateur connecté). */
@RestController
@RequestMapping("/api/besoins")
public class BesoinSuiviController {

    private final BesoinSuiviService service;

    public BesoinSuiviController(BesoinSuiviService service) {
        this.service = service;
    }

    @GetMapping
    public List<BesoinLigneDto> lister(@RequestParam(defaultValue = "tous") String vue,
                                       @RequestParam(required = false) String etat,
                                       @RequestParam(required = false) String q, HttpSession session) {
        return service.lister(Acces.connecte(session), vue, etat, q);
    }

    @GetMapping("/{id}")
    public BesoinFicheDto fiche(@PathVariable Integer id, HttpSession session) {
        return service.fiche(id, Acces.connecte(session));
    }

    @PostMapping("/{id}/annuler")
    public BesoinFicheDto annuler(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        return service.annuler(id, Acces.connecte(session), body == null ? null : body.get("motif"));
    }
}
