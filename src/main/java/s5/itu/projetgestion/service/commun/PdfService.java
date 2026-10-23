package s5.itu.projetgestion.service.commun;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Génération de PDF à partir d'un modèle Thymeleaf (templates/…) : version générale de OffrePdfService.
 * Interface P4 → P3 : PdfService.generer("contrat/modele-contrat", variables) pour le contrat.
 * Le modèle doit produire du XHTML bien formé (balises fermées), comme offres/modele-offre.html.
 */
@Service
public class PdfService {

    private final ITemplateEngine templateEngine;

    public PdfService(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /** HTML du modèle (utile pour un aperçu dans le navigateur). */
    public String html(String modele, Map<String, Object> variables) {
        Context ctx = new Context();
        if (variables != null) ctx.setVariables(variables);
        return templateEngine.process(modele, ctx);
    }

    /** PDF du modèle. */
    public byte[] generer(String modele, Map<String, Object> variables) {
        String html = html(modele, variables);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Échec de la génération du PDF (" + modele + ")", e);
        }
    }
}
