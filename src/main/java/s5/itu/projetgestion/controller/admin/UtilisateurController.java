package s5.itu.projetgestion.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.auth.UtilisateurDto;
import s5.itu.projetgestion.dto.auth.UtilisateurRequest;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.service.auth.UtilisateurService;

/** Administration des comptes (rôle ADMIN). */
@RestController
@RequestMapping("/api/admin/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping
    public List<UtilisateurDto> lister(@RequestParam(required = false) String statut,
                                       @RequestParam(required = false) String q, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN);
        return service.lister(statut, q);
    }

    @GetMapping("/{id}")
    public UtilisateurDto fiche(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN);
        return service.fiche(id);
    }

    @GetMapping("/roles")
    public List<OptionDto> roles(HttpSession session) {
        Acces.exiger(session, Acces.ADMIN);
        return service.roles();
    }

    @PostMapping
    public ResponseEntity<UtilisateurDto> creer(@RequestBody UtilisateurRequest req, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req));
    }

    @PutMapping("/{id}")
    public UtilisateurDto modifier(@PathVariable Integer id, @RequestBody UtilisateurRequest req, HttpSession session) {
        return service.modifier(id, req, Acces.exiger(session, Acces.ADMIN).getId());
    }

    @PostMapping("/{id}/activer")
    public UtilisateurDto activer(@PathVariable Integer id, HttpSession session) {
        return service.activer(id, Acces.exiger(session, Acces.ADMIN).getId());
    }

    @PostMapping("/{id}/desactiver")
    public UtilisateurDto desactiver(@PathVariable Integer id, HttpSession session) {
        return service.desactiver(id, Acces.exiger(session, Acces.ADMIN).getId());
    }

    @PostMapping("/{id}/reinitialiser")
    public Map<String, String> reinitialiser(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.ADMIN);
        return Map.of("motDePasseProvisoire", service.reinitialiserMotDePasse(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id, HttpSession session) {
        service.supprimer(id, Acces.exiger(session, Acces.ADMIN).getId());
        return ResponseEntity.noContent().build();
    }
}
