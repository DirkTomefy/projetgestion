package s5.itu.projetgestion.controller.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.SessionUser;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.service.UserService;
import s5.itu.projetgestion.service.besoin.BesoinService;
import s5.itu.projetgestion.service.validation.ValidationService;

@RestController
public class ValidationController {

    private final ValidationService validationService;
    private final UserService userService;
    private final BesoinService besoinService;

    public ValidationController(ValidationService validationService, UserService userService,
            BesoinService besoinService) {
        this.validationService = validationService;
        this.userService = userService;
        this.besoinService = besoinService;
    }

    @PostMapping("/api/besoin/valider")
    public void validerBesoin(@RequestParam Integer idBesoin, HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        User u = userService.getById(sessionUser.getId());
        Besoin b = new Besoin();
        try {
            b = this.besoinService.getById(idBesoin);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        try {
            this.validationService.validerBesoin(b, u);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());

        }
    }

    @PostMapping("/api/besoin/refuser")
    public void refuserBesoin(@RequestParam Integer idBesoin, HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        User u = userService.getById(sessionUser.getId());
        Besoin b = new Besoin();
        try {
            b = this.besoinService.getById(idBesoin);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

        try {
            this.validationService.refuserBesoin(b, u);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());

        }
    }

}
