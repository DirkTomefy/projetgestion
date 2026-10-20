package s5.itu.projetgestion.service.rh;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;


@Service
public class OffrePdfService {

    public static final String TEMPLATE = "offres/modele-offre";

    private final ITemplateEngine templateEngine;

    public OffrePdfService(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderHtml(Map<String, Object> variables) {
        Context ctx = new Context();
        ctx.setVariables(variables);
        return templateEngine.process(TEMPLATE, ctx);
    }

    public byte[] renderPdf(Map<String, Object> variables) {
        String html = renderHtml(variables);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Échec de la génération du PDF de l'offre", e);
        }
    }
}
