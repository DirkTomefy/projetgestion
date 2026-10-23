package s5.itu.projetgestion.service.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

/**
 * Hachage des mots de passe (PBKDF2-HMAC-SHA256, sel aléatoire), sans dépendance supplémentaire.
 * Format stocké : pbkdf2$&lt;itérations&gt;$&lt;sel base64&gt;$&lt;empreinte base64&gt;.
 * Les mots de passe encore en clair (données de test) sont acceptés puis re-hachés à la connexion.
 */
@Service
public class PasswordService {

    private static final String PREFIXE = "pbkdf2$";
    private static final int ITERATIONS = 120_000;
    private static final int LONGUEUR_CLE = 256;
    private static final SecureRandom ALEA = new SecureRandom();

    public String hacher(String motDePasse) {
        byte[] sel = new byte[16];
        ALEA.nextBytes(sel);
        byte[] empreinte = pbkdf2(motDePasse.toCharArray(), sel, ITERATIONS);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIXE + ITERATIONS + "$" + b64.encodeToString(sel) + "$" + b64.encodeToString(empreinte);
    }

    public boolean verifier(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null) return false;
        if (!estHache(stocke)) {   // ancien format (clair)
            return MessageDigest.isEqual(motDePasse.getBytes(), stocke.getBytes());
        }
        String[] parts = stocke.split("\\$");
        if (parts.length != 4) return false;
        int iterations = Integer.parseInt(parts[1]);
        byte[] sel = Base64.getDecoder().decode(parts[2]);
        byte[] attendu = Base64.getDecoder().decode(parts[3]);
        return MessageDigest.isEqual(attendu, pbkdf2(motDePasse.toCharArray(), sel, iterations));
    }

    public boolean estHache(String stocke) {
        return stocke != null && stocke.startsWith(PREFIXE);
    }

    /** Règle minimale : 8 caractères dont au moins une lettre et un chiffre. */
    public void controlerRobustesse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 8
                || !motDePasse.matches(".*[A-Za-z].*") || !motDePasse.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères, dont une lettre et un chiffre.");
        }
    }

    private static byte[] pbkdf2(char[] mdp, byte[] sel, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(mdp, sel, iterations, LONGUEUR_CLE);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Hachage du mot de passe impossible", e);
        }
    }
}
