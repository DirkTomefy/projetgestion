package s5.itu.projetgestion.controller.embauche;

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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import s5.itu.projetgestion.dto.commun.ErreurApi;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.err.PropositionException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Erreurs des API de proposition (RH et page publique), au même format que les autres API : ErreurApi. */
@Order(Ordered.HIGHEST_PRECEDENCE)   // avant GlobalExceptionHandler
@RestControllerAdvice(basePackageClasses = EmbaucheErreurHandler.class)
public class EmbaucheErreurHandler {

    private static final Logger log = LoggerFactory.getLogger(EmbaucheErreurHandler.class);

    @ExceptionHandler(PropositionException.class)
    public ResponseEntity<ErreurApi> proposition(PropositionException e) {
        Map<String, String> champs = e.getChamp() == null ? Map.of() : Map.of(e.getChamp(), e.getMessage());
        return reponse(new ErreurApi(e.getStatus(), e.getMessage(), champs));
    }

    @ExceptionHandler(AccesRefuseException.class)
    public ResponseEntity<ErreurApi> accesRefuse(AccesRefuseException e) {
        return reponse(ErreurApi.generale(403, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> champs.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return reponse(new ErreurApi(400, "Certains champs sont invalides", champs));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> illisible(HttpMessageNotReadableException e) {
        return reponse(ErreurApi.generale(400, "Données illisibles (dates au format AAAA-MM-JJ, montants en nombres)"));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErreurApi> parametre(Exception e) {
        return reponse(ErreurApi.generale(400, "Paramètre manquant ou invalide"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage());
        if (detail.contains("ux_proposition_siege_en_cours")) {
            return reponse(ErreurApi.generale(409, "Une proposition est déjà en cours ou acceptée sur ce poste"));
        }
        if (detail.contains("ux_proposition_candidature_en_cours")) {
            return reponse(ErreurApi.generale(409, "Cette candidature a déjà une proposition en cours ou acceptée"));
        }
        log.error("Contrainte de base violée (proposition d'embauche)", e);
        return reponse(ErreurApi.generale(409, "L'opération n'a pas pu être enregistrée"));
    }

    @ExceptionHandler(ProjetgestionException.class)
    public ResponseEntity<ErreurApi> metier(ProjetgestionException e) {
        return reponse(ErreurApi.generale(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> inattendue(Exception e) {
        log.error("Erreur inattendue (proposition d'embauche)", e);
        return reponse(ErreurApi.generale(500, "Une erreur est survenue. Réessayez plus tard."));
    }

    private static ResponseEntity<ErreurApi> reponse(ErreurApi erreur) {
        return ResponseEntity.status(HttpStatus.valueOf(erreur.status())).body(erreur);
    }
}
