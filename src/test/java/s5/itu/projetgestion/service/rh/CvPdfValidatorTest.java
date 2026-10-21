package s5.itu.projetgestion.service.rh;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import s5.itu.projetgestion.err.CandidatureException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CvPdfValidatorTest {

    private final CvPdfValidator validator = new CvPdfValidator(1024);

    private static MockMultipartFile fichier(String nom, String contenu) {
        return new MockMultipartFile("cv", nom, "application/pdf", contenu.getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void accepteUnVraiPdf() {
        assertDoesNotThrow(() -> validator.verifier(fichier("mon-cv.PDF", "%PDF-1.7 contenu")));
    }

    @Test
    void refuseUnCvAbsent() {
        CandidatureException e = assertThrows(CandidatureException.class, () -> validator.verifier(null));
        assertEquals(400, e.getStatus());
        assertEquals("cv", e.getChamp());
    }

    @Test
    void refuseUneMauvaiseExtension() {
        CandidatureException e = assertThrows(CandidatureException.class,
                () -> validator.verifier(fichier("cv.docx", "%PDF-1.7")));
        assertEquals(415, e.getStatus());
    }

    @Test
    void refuseUnFauxPdfRenomme() {
        CandidatureException e = assertThrows(CandidatureException.class,
                () -> validator.verifier(fichier("cv.pdf", "MZ executable deguise")));
        assertEquals(415, e.getStatus());
    }

    @Test
    void refuseUnFichierTropGros() {
        CandidatureException e = assertThrows(CandidatureException.class,
                () -> validator.verifier(fichier("cv.pdf", "%PDF-" + "x".repeat(2000))));
        assertEquals(413, e.getStatus());
    }
}
