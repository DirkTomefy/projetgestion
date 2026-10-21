package s5.itu.projetgestion.controller.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.service.auth.AuthService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -----------------------------------------------------------------
    // DTO de requête
    // -----------------------------------------------------------------

    public record LoginRequest(String login, String password) { }

    // -----------------------------------------------------------------
    // POST /auth/login
    // -----------------------------------------------------------------

    /**
     * Authentifie l'utilisateur à partir du body JSON :
     *   { "login": "...", "password": "..." }
     *
     * Succès  → 200 + SessionUser
     * Échec   → 401 + { "message": "..." }
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req,
                                                     HttpSession session) {
        try {
            User user = authService.authenticate(req.login(), req.password());
            SessionUser su = SessionUser.from(user);
            session.setAttribute(SessionKeys.CURRENT_USER, su);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("authenticated", true);
            body.put("user", su);
            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", ex.getMessage()));
        }
    }

    // -----------------------------------------------------------------
    // POST /auth/logout
    // -----------------------------------------------------------------

    /** Invalide la session → 200 { "ok": true } */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // -----------------------------------------------------------------
    // GET /auth/session
    // -----------------------------------------------------------------

    /**
     * Retourne l'état de la session courante.
     *
     * Connecté    → 200 { "authenticated": true, "user": {...}, "session": {...} }
     * Anonyme     → 401 { "authenticated": false, "session": {...} }
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> currentSession(HttpSession session) {

        SessionUser su = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);

        Map<String, Object> sessionInfo = new LinkedHashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("creationTime", session.getCreationTime());
        sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        sessionInfo.put("new", session.isNew());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", su != null);
        body.put("session", sessionInfo);
        if (su != null) {
            body.put("user", su);
        }

        return su == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
                : ResponseEntity.ok(body);
    }
}