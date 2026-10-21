package s5.itu.projetgestion.controller.rh.publique;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import s5.itu.projetgestion.dto.rh.ErreurApi;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.service.rh.OffrePubliqueService;

@RestControllerAdvice(basePackageClasses = ApiPubliqueErreurHandler.class)
public class ApiPubliqueErreurHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiPubliqueErreurHandler.class);

    @ExceptionHandler(OffrePubliqueService.OffreIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(ProjetgestionException e) {
        return reponse(ErreurApi.generale(404, e.getMessage()));
    }

    @ExceptionHandler(ProjetgestionException.class)
    public ResponseEntity<ErreurApi> metier(ProjetgestionException e) {
        return reponse(ErreurApi.generale(400, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApi> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(ErreurApi.generale(400, "Paramètre invalide : " + e.getName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> inattendue(Exception e) {
        log.error("Erreur inattendue dans l'API publique", e);
        return reponse(ErreurApi.generale(500, "Une erreur est survenue. Réessayez plus tard."));
    }

    static ResponseEntity<ErreurApi> reponse(ErreurApi erreur) {
        return ResponseEntity.status(HttpStatus.valueOf(erreur.status())).body(erreur);
    }
}
