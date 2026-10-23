package s5.itu.projetgestion.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.ProjetgestionException;

/**
 * Filet de sécurité pour toutes les API.
 * Priorité la plus basse : les gestionnaires propres à chaque partie (recrutement, embauche, contrat)
 * passent avant lui ; il ne traite que ce qu'ils ne traitent pas.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<Map<String, Object>> accesRefuse(AccesRefuseException e) {
        return reponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Règle métier non respectée (données invalides, action impossible). */
    @ExceptionHandler({ProjetgestionException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> regle(RuntimeException e) {
        return reponse(e instanceof IllegalStateException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statut(ResponseStatusException e) {
        return reponse(e.getStatusCode(), String.valueOf(e.getReason()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> requeteInvalide(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide : " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> champs.putIfAbsent(f.getField(), f.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("message", "Certains champs sont invalides");
        body.put("champs", champs);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> introuvable(NoResourceFoundException e) {
        return reponse(HttpStatus.NOT_FOUND, "Adresse introuvable : /" + e.getResourcePath());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> methode(HttpRequestMethodNotSupportedException e) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> autre(Exception e) {
        log.error("Erreur serveur", e);   // la trace complète apparaît dans la console
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur est survenue. Réessayez plus tard.");
    }

    private static ResponseEntity<Map<String, Object>> reponse(HttpStatusCode statut, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", statut.value());
        body.put("message", message);
        return ResponseEntity.status(statut).body(body);
    }
}
