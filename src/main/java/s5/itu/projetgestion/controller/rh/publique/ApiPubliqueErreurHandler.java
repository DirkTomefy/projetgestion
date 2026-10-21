package s5.itu.projetgestion.controller.rh.publique;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import s5.itu.projetgestion.dto.rh.ErreurApi;
import s5.itu.projetgestion.err.CandidatureException;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.service.rh.OffrePubliqueService;

/**
 * Erreurs de l'API publique renvoyées en JSON ({@link ErreurApi}).
 * Limité au package controller.rh.publique : ne touche pas aux pages des autres membres.
 */
@RestControllerAdvice(basePackageClasses = ApiPubliqueErreurHandler.class)
public class ApiPubliqueErreurHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiPubliqueErreurHandler.class);

    @ExceptionHandler(OffrePubliqueService.OffreIntrouvableException.class)
    public ResponseEntity<ErreurApi> introuvable(ProjetgestionException e) {
        return reponse(ErreurApi.generale(404, e.getMessage()));
    }

    /** Erreur métier d'une candidature, éventuellement liée à un champ du formulaire. */
    @ExceptionHandler(CandidatureException.class)
    public ResponseEntity<ErreurApi> candidature(CandidatureException e) {
        Map<String, String> champs = e.getChamp() == null ? Map.of() : Map.of(e.getChamp(), e.getMessage());
        return reponse(new ErreurApi(e.getStatus(), e.getMessage(), champs));
    }

    /** Champs du formulaire invalides : un message par champ (ex. "details[0].libelle"). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> champs.putIfAbsent(
                err.getField().replace(".periodeValide", ".dateFin"), err.getDefaultMessage()));
        return reponse(new ErreurApi(400, "Certains champs sont invalides", champs));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErreurApi> validationParametres(HandlerMethodValidationException e) {
        return reponse(ErreurApi.generale(400, "Certains champs sont invalides"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErreurApi> partieManquante(MissingServletRequestPartException e) {
        return reponse(ErreurApi.generale(400, "Requête incomplète : partie « " + e.getRequestPartName() + " » manquante"));
    }

    /** JSON mal formé ou date au mauvais format (attendu : AAAA-MM-JJ). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurApi> illisible(HttpMessageNotReadableException e) {
        return reponse(ErreurApi.generale(400, "Données du formulaire illisibles (format de date attendu : AAAA-MM-JJ)"));
    }

    /** Deux envois simultanés peuvent passer les contrôles et buter sur une contrainte unique de la base. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage());
        if (detail.contains("candidatures_id_candidat_id_offre_emploi_key")) {
            return reponse(ErreurApi.generale(409, "Vous avez déjà postulé à cette offre"));
        }
        if (detail.contains("ux_candidat_email")) {
            return reponse(ErreurApi.generale(409, "Une candidature avec cet email est déjà en cours d'envoi, réessayez dans un instant"));
        }
        log.error("Contrainte de base violée lors d'une candidature", e);
        return reponse(ErreurApi.generale(409, "La candidature n'a pas pu être enregistrée"));
    }

    /** Erreur métier : message destiné à l'utilisateur. */
    @ExceptionHandler(ProjetgestionException.class)
    public ResponseEntity<ErreurApi> metier(ProjetgestionException e) {
        return reponse(ErreurApi.generale(400, e.getMessage()));
    }

    /** Identifiant non numérique dans l'URL (ex. /offres/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurApi> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return reponse(ErreurApi.generale(400, "Paramètre invalide : " + e.getName()));
    }

    /** Toute autre erreur : on journalise le détail, on ne l'expose pas au public. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurApi> inattendue(Exception e) {
        log.error("Erreur inattendue dans l'API publique", e);
        return reponse(ErreurApi.generale(500, "Une erreur est survenue. Réessayez plus tard."));
    }

    static ResponseEntity<ErreurApi> reponse(ErreurApi erreur) {
        return ResponseEntity.status(HttpStatus.valueOf(erreur.status())).body(erreur);
    }
}
