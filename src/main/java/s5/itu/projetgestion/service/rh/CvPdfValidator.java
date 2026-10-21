package s5.itu.projetgestion.service.rh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import s5.itu.projetgestion.err.CandidatureException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@Component
public class CvPdfValidator {

    public static final String CHAMP = "cv";
    private static final byte[] SIGNATURE_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final long tailleMaxOctets;

    public CvPdfValidator(@Value("${app.candidature.cv-max-bytes:5242880}") long tailleMaxOctets) {
        this.tailleMaxOctets = tailleMaxOctets;
    }

    public void verifier(MultipartFile cv) {
        if (cv == null || cv.isEmpty()) {
            throw new CandidatureException(400, "Le CV au format PDF est obligatoire", CHAMP);
        }
        if (cv.getSize() > tailleMaxOctets) {
            throw new CandidatureException(413,
                    "Le CV ne doit pas dépasser " + (tailleMaxOctets / (1024 * 1024)) + " Mo", CHAMP);
        }
        String nom = cv.getOriginalFilename();
        if (nom == null || !nom.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new CandidatureException(415, "Le CV doit être un fichier .pdf", CHAMP);
        }
        if (!commenceParSignaturePdf(cv)) {
            throw new CandidatureException(415, "Le fichier envoyé n'est pas un PDF valide", CHAMP);
        }
    }

    private static boolean commenceParSignaturePdf(MultipartFile cv) {
        try (InputStream in = cv.getInputStream()) {
            byte[] debut = in.readNBytes(SIGNATURE_PDF.length);
            return Arrays.equals(debut, SIGNATURE_PDF);
        } catch (IOException e) {
            throw new CandidatureException(400, "Lecture du CV impossible", CHAMP);
        }
    }
}
