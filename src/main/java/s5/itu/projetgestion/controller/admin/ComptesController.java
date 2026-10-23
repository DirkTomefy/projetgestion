package s5.itu.projetgestion.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.service.auth.UtilisateurService;

/** Liste des comptes actifs pour les listes de choix (tout utilisateur connecté). */
@RestController
public class ComptesController {

    private final UtilisateurService service;

    public ComptesController(UtilisateurService service) {
        this.service = service;
    }

    @GetMapping("/api/utilisateurs/actifs")
    public List<OptionDto> actifs(HttpSession session) {
        Acces.connecte(session);
        return service.actifs();
    }
}
