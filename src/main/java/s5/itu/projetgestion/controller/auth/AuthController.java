package s5.itu.projetgestion.controller.auth;

import org.springframework.web.bind.annotation.PutMapping;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.auth.InscriptionRequest;
import s5.itu.projetgestion.dto.auth.MotDePasseRequest;
import s5.itu.projetgestion.dto.auth.UtilisateurDto;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.service.auth.UtilisateurService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.AuthException;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.service.auth.AuthService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UtilisateurService utilisateurService, UserRepository userRepository) {
        this.authService = authService;
        this.utilisateurService = utilisateurService;
        this.userRepository = userRepository;
    }

    // -----------------------------------------------------------------
    // Inscription, profil, mot de passe
    // -----------------------------------------------------------------

    /** Inscription publique : compte EN_ATTENTE jusqu'à sa validation par un administrateur. */
    @PostMapping(value = "/inscription", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> inscription(@RequestBody InscriptionRequest req) {
        UtilisateurDto u = utilisateurService.inscrire(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("login", u.login(),
                "message", "Inscription enregistrée. Vous pourrez vous connecter dès qu'un administrateur aura validé votre compte."));
    }

    @GetMapping("/profil")
    public UtilisateurDto profil(HttpSession session) {
        return utilisateurService.profil(Acces.connecte(session).getId());
    }

    public record ProfilRequest(String nomComplet, String email) { }

    @PutMapping(value = "/profil", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UtilisateurDto modifierProfil(@RequestBody ProfilRequest req, HttpSession session) {
        SessionUser su = Acces.connecte(session);
        UtilisateurDto u = utilisateurService.modifierProfil(su.getId(), req.nomComplet(), req.email());
        userRepository.findById(su.getId()).ifPresent(user -> session.setAttribute(SessionKeys.CURRENT_USER, SessionUser.from(user)));
        return u;
    }

    @PostMapping(value = "/mot-de-passe", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> motDePasse(@RequestBody MotDePasseRequest req, HttpSession session) {
        utilisateurService.changerMotDePasse(Acces.connecte(session).getId(), req);
        return Map.of("message", "Mot de passe modifié.");
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

        } catch (IllegalArgumentException | AuthException ex) {   // mauvais login ou mot de passe : 401, jamais 500
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