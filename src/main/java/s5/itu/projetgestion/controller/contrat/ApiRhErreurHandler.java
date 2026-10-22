package s5.itu.projetgestion.controller.contrat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import s5.itu.projetgestion.controller.besoin.BesoinController;
import s5.itu.projetgestion.dto.commun.ErreurApi;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.ProjetgestionException;

@Order(Ordered.HIGHEST_PRECEDENCE)   // avant GlobalExceptionHandler
@RestControllerAdvice(basePackageClasses = {ContratController.class, BesoinController.class})
public class ApiRhErreurHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiRhErreurHandler.class);

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ErreurApi> accesRefuse(AccesRefuseException e) {
        return reponse(ErreurApi.generale(403, e.getMessage()));
    }

    // Règles métier : états invalides, quantités, durées...
    @ExceptionHandler({ProjetgestionException.class, IllegalArgumentException.class,
            IllegalStateException.class})
    public ResponseEntity<ErreurApi> metier(RuntimeException e) {
        return reponse(ErreurApi.generale(400, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(err -> champs.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return reponse(new ErreurApi(400, "Certains champs sont invalides", champs));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErreurApi> validationParametres(HandlerMethodValidationException e) {
        return reponse(ErreurApi.generale(400, "Certains champs sont invalides"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErreurApi> partieManquante(MissingServletRequestPartException e) {
        return reponse(ErreurApi.generale(400,
            "Requête incomplète : partie « " + e.getRequestPartName() + " » manquante"));
    }

    /** Statut choisi par le contrôleur (403 pas votre étape, 404 introuvable…) : on le garde. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErreurApi> statut(ResponseStatusException e) {
        return reponse(ErreurApi.generale(e.getStatusCode().value(), e.getReason()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErreurApi> parametreManquant(MissingServletRequestParameterException e) {
        return reponse(ErreurApi.generale(400, "Paramètre manquant : " + e.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> illisible(HttpMessageNotReadableException e) {
        return reponse(ErreurApi.generale(400,
            "Données illisibles (format de date attendu : AAAA-MM-JJ)"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApi> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(ErreurApi.generale(400, "Paramètre invalide : " + e.getName()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        log.error("Contrainte de base violée", e);
        return reponse(ErreurApi.generale(409,
            "Opération refusée : doublon ou donnée liée à d'autres enregistrements"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> inattendue(Exception e) {
        log.error("Erreur inattendue dans l'API RH", e);
        return reponse(ErreurApi.generale(500, "Une erreur est survenue. Réessayez plus tard."));
    }

    static ResponseEntity<ErreurApi> reponse(ErreurApi erreur) {
        return ResponseEntity.status(HttpStatus.valueOf(erreur.status())).body(erreur);
    }
}