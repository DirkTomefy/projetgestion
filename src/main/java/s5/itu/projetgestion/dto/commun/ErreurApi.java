package s5.itu.projetgestion.dto.commun;

import java.util.Map;

public record ErreurApi(int status, String message, Map<String, String> champs) {

    public static ErreurApi generale(int status, String message) {
        return new ErreurApi(status, message, Map.of());
    }
}
