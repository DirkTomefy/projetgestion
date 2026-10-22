package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import s5.itu.projetgestion.service.commun.PdfService;

import java.util.Map;

/** PDF de l'offre d'emploi : s'appuie sur le service général PdfService. */
@Service
public class OffrePdfService {

    public static final String TEMPLATE = "offres/modele-offre";

    private final PdfService pdfService;

    public OffrePdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    public String renderHtml(Map<String, Object> variables) {
        return pdfService.html(TEMPLATE, variables);
    }

    public byte[] renderPdf(Map<String, Object> variables) {
        return pdfService.generer(TEMPLATE, variables);
    }
}
