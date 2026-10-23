package s5.itu.projetgestion.err;

public class CandidatureException extends ProjetgestionException {

    private final int status;
    private final String champ;

    public CandidatureException(int status, String message) {
        this(status, message, null);
    }

    public CandidatureException(int status, String message, String champ) {
        super(message);
        this.status = status;
        this.champ = champ;
    }

    public int getStatus() {
        return status;
    }

    public String getChamp() {
        return champ;
    }
}
