package s5.itu.projetgestion.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.SessionUser;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.service.AuthService;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Formulaire de connexion */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (session.getAttribute(SessionKeys.CURRENT_USER) != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    /** Traitement du formulaire */
    @PostMapping("/login")
    public String doLogin(@RequestParam String login,
                          @RequestParam String motDePasse,
                          HttpSession session,
                          Model model) {
        try {
            User user = authService.authenticate(login, motDePasse);
            SessionUser su = SessionUser.from(user);
            session.setAttribute(SessionKeys.CURRENT_USER, su);
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("login", login);
            return "auth/login";
        }
    }

    /** Déconnexion */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping(value = "/session", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> currentSession(HttpSession session) {

        SessionUser su = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", su != null);
        body.put("sessionId", session.getId());
        body.put("creationTime", session.getCreationTime());
        body.put("lastAccessedTime", session.getLastAccessedTime());
        body.put("maxInactiveInterval", session.getMaxInactiveInterval());
        body.put("new", session.isNew());

        if (su != null) {
            body.put("user", su);
        }

        return su == null
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
                : ResponseEntity.ok(body);
    }
}