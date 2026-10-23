package s5.itu.projetgestion.config;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.AccesRefuseException;

/**
 * Contrôle d'accès des API à partir de l'utilisateur en session.
 * Les rôles de l'application : ADMIN, RH, Manager, Finance, DG, Employe.
 */
public final class Acces {

    public static final String ADMIN = "ADMIN";
    public static final String RH = "RH";
    public static final String MANAGER = "Manager";
    public static final String FINANCE = "Finance";
    public static final String DG = "DG";

    private Acces() { }

    /** Utilisateur connecté, sinon 403. */
    public static SessionUser connecte(HttpSession session) {
        SessionUser u = session == null ? null : (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (u == null) throw new AccesRefuseException("Vous devez être connecté");
        return u;
    }

    /** Utilisateur connecté ayant au moins un des rôles, sinon 403. */
    public static SessionUser exiger(HttpSession session, String... roles) {
        SessionUser u = connecte(session);
        for (String r : roles) if (u.hasRole(r)) return u;
        throw new AccesRefuseException("Accès réservé aux rôles : " + String.join(", ", roles));
    }

    public static boolean aRole(SessionUser u, String... roles) {
        if (u == null) return false;
        for (String r : roles) if (u.hasRole(r)) return true;
        return false;
    }
}
