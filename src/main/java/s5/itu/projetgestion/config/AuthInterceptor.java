package s5.itu.projetgestion.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionKeys.CURRENT_USER) == null) {
            String chemin = request.getRequestURI().substring(request.getContextPath().length());
            if (chemin.startsWith("/api/") || chemin.startsWith("/uploads/")) {
                // API et fichiers (CV, contrats) : 401 que le front sait traiter, jamais le fichier
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":401,\"message\":\"Vous n'êtes pas connecté\"}");
                return false;
            }
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return false;
        }
        return true;
    }
}