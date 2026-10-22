package s5.itu.projetgestion.err.validation;


public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
