package s5.itu.projetgestion.err;

/** Erreur métier d'une proposition d'embauche, avec le code HTTP à renvoyer et le champ en cause. */
public class PropositionException extends ProjetgestionException {

    private final int status;
    private final String champ;

    public PropositionException(int status, String message) {
        this(status, message, null);
    }

    public PropositionException(int status, String message, String champ) {
        super(message);
        this.status = status;
        this.champ = champ;
    }

    public static PropositionException introuvable(String message) {
        return new PropositionException(404, message);
    }

    public static PropositionException conflit(String message) {
        return new PropositionException(409, message);
    }

    public static PropositionException invalide(String champ, String message) {
        return new PropositionException(400, message, champ);
    }

    public int getStatus() { return status; }
    public String getChamp() { return champ; }
}
