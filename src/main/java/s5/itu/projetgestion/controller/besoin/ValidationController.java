package s5.itu.projetgestion.controller.besoin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.validation.AutorisationValidationException;
import s5.itu.projetgestion.err.validation.BesoinInvalideException;
import s5.itu.projetgestion.err.validation.UtilisateurInvalideException;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.service.auth.UserService;
import s5.itu.projetgestion.service.besoin.BesoinService;
import s5.itu.projetgestion.service.besoin.ValidationService;

@RestController
public class ValidationController {

    private final ValidationService validationService;
    private final UserService userService;
    private final BesoinService besoinService;

    public ValidationController(
            ValidationService validationService,
            UserService userService,
            BesoinService besoinService) {
        this.validationService = validationService;
        this.userService = userService;
        this.besoinService = besoinService;
    }

    @PostMapping("/api/besoin/valider")
    public ResponseEntity<String> validerBesoin(
            @RequestParam Integer idBesoin,
            @RequestParam(required = false) String commentaire,
            HttpSession session) {

        User utilisateur = recupererUtilisateur(session);
        Besoin besoin = new Besoin();

        try {
            besoin = recupererBesoin(idBesoin);
            validationService.validerBesoin(besoin, utilisateur, commentaire);
            return ResponseEntity.ok("Besoin validé avec succès.");

        } catch (BesoinInvalideException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (UtilisateurInvalideException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        } catch (AutorisationValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage(), e);
        }
    }

    @PostMapping("/api/besoin/refuser")
    public void refuserBesoin(
            @RequestParam Integer idBesoin,
            @RequestParam(required = false) String commentaire,
            HttpSession session) {

        User utilisateur = recupererUtilisateur(session);
        Besoin besoin = new Besoin();

        try {
            besoin = recupererBesoin(idBesoin);
            validationService.refuserBesoin(besoin, utilisateur, commentaire);
        } catch (BesoinInvalideException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (UtilisateurInvalideException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, e.getMessage(), e);
        } catch (AutorisationValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du refus du besoin", e);
        }
    }

    private User recupererUtilisateur(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);

        if (sessionUser == null || sessionUser.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Utilisateur non connecté");
        }

        User utilisateur = userService.getById(sessionUser.getId());

        if (utilisateur == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Utilisateur introuvable");
        }

        return utilisateur;
    }

    private Besoin recupererBesoin(Integer idBesoin) throws Exception {
        if (idBesoin == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Identifiant du besoin obligatoire");
        }

        try {
            Besoin besoin = besoinService.getById(idBesoin);

            if (besoin == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Besoin introuvable");
            }

            return besoin;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Besoin introuvable", e);
        }
    }
}
