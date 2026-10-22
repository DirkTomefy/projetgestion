package s5.itu.projetgestion.controller.notification;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.notification.NotificationDto;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.service.notification.NotificationService;

import java.util.List;
import java.util.Map;

/** Cloche du menu : notifications de l'utilisateur connecté (tous rôles). */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationDto> liste(@RequestParam(defaultValue = "false") boolean nonLues,
                                       @RequestParam(defaultValue = "20") int limite,
                                       HttpSession session) {
        return service.lister(connecte(session).getId(), nonLues, limite);
    }

    /** Nombre de notifications non lues (pastille de la cloche). */
    @GetMapping("/compteur")
    public Map<String, Long> compteur(HttpSession session) {
        return Map.of("nonLues", service.compterNonLues(connecte(session).getId()));
    }

    @PostMapping("/{id}/lue")
    public NotificationDto marquerLue(@PathVariable Integer id, HttpSession session) {
        return service.marquerLue(id, connecte(session).getId());
    }

    @PostMapping("/tout-lire")
    public Map<String, Integer> toutLire(HttpSession session) {
        return Map.of("marquees", service.marquerToutLu(connecte(session).getId()));
    }

    private static SessionUser connecte(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (u == null) throw new AccesRefuseException("Vous devez être connecté");
        return u;
    }
}
