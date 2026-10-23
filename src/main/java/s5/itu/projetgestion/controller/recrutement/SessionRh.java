package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.service.recrutement.OffreEmploiService;

final class SessionRh {

    private SessionRh() { }

    static SessionUser connecte(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (u == null) {
            throw new AccesRefuseException("Vous devez être connecté");
        }
        return u;
    }

    static SessionUser rh(HttpSession session) {
        SessionUser u = connecte(session);
        if (!u.hasRole(OffreEmploiService.ROLE_RH)) {
            throw new AccesRefuseException("Accès réservé au rôle RH");
        }
        return u;
    }
}
