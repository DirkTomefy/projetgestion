package s5.itu.projetgestion.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import s5.itu.projetgestion.dto.SessionUser;

@ControllerAdvice
public class GlobalModelAttributes {
    //? Permet d'utiliser ${currentUser} dans toutes les vues Thymeleaf.
    @ModelAttribute("currentUser")
    public SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
    }
}