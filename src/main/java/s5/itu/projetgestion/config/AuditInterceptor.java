package s5.itu.projetgestion.config;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.service.audit.AuditService;

/**
 * Journal d'audit automatique : chaque requête qui modifie quelque chose (POST, PUT, PATCH, DELETE)
 * sur l'API est tracée avec l'utilisateur, la date, l'adresse et le code de réponse.
 */
public class AuditInterceptor implements HandlerInterceptor {

    private static final String UTILISATEUR_AVANT = AuditInterceptor.class.getName() + ".utilisateur";

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // la déconnexion invalide la session : on mémorise l'utilisateur avant
        if (estModification(request)) request.setAttribute(UTILISATEUR_AVANT, utilisateur(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!estModification(request)) return;
        SessionUser u = utilisateur(request);
        if (u == null) u = (SessionUser) request.getAttribute(UTILISATEUR_AVANT);
        String chemin = request.getRequestURI().substring(request.getContextPath().length());
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        auditService.tracer(u == null ? null : u.getId(), u == null ? loginTente(request) : u.getLogin(),
                request.getMethod(), chemin, response.getStatus(), ip);
    }

    private static boolean estModification(HttpServletRequest request) {
        String m = request.getMethod();
        return "POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m) || "DELETE".equals(m);
    }

    private static SessionUser utilisateur(HttpServletRequest request) {
        try {
            HttpSession s = request.getSession(false);
            return s == null ? null : (SessionUser) s.getAttribute(SessionKeys.CURRENT_USER);
        } catch (IllegalStateException sessionInvalidee) {
            return null;
        }
    }

    private static String loginTente(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/auth/login") ? "(connexion refusée)" : null;
    }
}
