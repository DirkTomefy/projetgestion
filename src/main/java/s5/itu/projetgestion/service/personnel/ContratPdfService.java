package s5.itu.projetgestion.service.personnel;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.contrat.ContratDetail;
import s5.itu.projetgestion.model.contrat.ContratRemuneration;
import s5.itu.projetgestion.model.rh.Document;
import s5.itu.projetgestion.model.rh.TypeDocument;
import s5.itu.projetgestion.repository.contrat.ContratDetailRepository;
import s5.itu.projetgestion.repository.contrat.ContratRemunerationRepository;
import s5.itu.projetgestion.repository.rh.TypeDocumentRepository;
import s5.itu.projetgestion.service.rh.ByteArrayMultipartFile;
import s5.itu.projetgestion.service.rh.FileStorageService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContratPdfService {

	private static final String TEMPLATE = "contrat/modele-contrat";
	private static final String TYPE_DOCUMENT_CONTRAT = "CONTRAT";
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ITemplateEngine templateEngine;
	private final FileStorageService fileStorageService;
	private final TypeDocumentRepository typeDocumentRepository;
	private final ContratDetailRepository contratDetailRepository;
	private final ContratRemunerationRepository contratRemunerationRepository;

	public ContratPdfService(ITemplateEngine templateEngine,
			FileStorageService fileStorageService,
			TypeDocumentRepository typeDocumentRepository,
			ContratDetailRepository contratDetailRepository,
			ContratRemunerationRepository contratRemunerationRepository) {
		this.templateEngine = templateEngine;
		this.fileStorageService = fileStorageService;
		this.typeDocumentRepository = typeDocumentRepository;
		this.contratDetailRepository = contratDetailRepository;
		this.contratRemunerationRepository = contratRemunerationRepository;
	}

	public String renderHtml(Contrat contrat) {
		Context context = new Context();
		context.setVariables(variables(contrat));
		return templateEngine.process(TEMPLATE, context);
	}

	public byte[] renderPdf(Contrat contrat) {
		String html = renderHtml(contrat);
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withHtmlContent(html, null);
			builder.toStream(output);
			builder.run();
			return output.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Échec de la génération du PDF du contrat", e);
		}
	}

	public Document generateAndStore(Contrat contrat) {
		TypeDocument typeDocument = typeDocumentRepository.findByLibelle(TYPE_DOCUMENT_CONTRAT)
				.orElseGet(() -> {
					TypeDocument type = new TypeDocument();
					type.setLibelle(TYPE_DOCUMENT_CONTRAT);
					return typeDocumentRepository.save(type);
				});

		byte[] pdf = renderPdf(contrat);
		ByteArrayMultipartFile file = new ByteArrayMultipartFile(
				pdf, "contrat_" + contrat.getReference() + ".pdf", "application/pdf");
		return fileStorageService.saveUpload(file, typeDocument.getId());
	}

	private Map<String, Object> variables(Contrat contrat) {
		List<ContratDetail> details = contratDetailRepository.findByContratId(contrat.getId());
		List<ContratRemuneration> remunerations = contratRemunerationRepository
				.findByContratIdOrderByDateEffetDesc(contrat.getId());

		Map<String, Object> variables = new HashMap<>();
		variables.put("contrat", contrat);
		variables.put("details", details);
		variables.put("remunerations", remunerations);
		variables.put("dateDebut", formatDate(contrat.getDateDebut()));
		variables.put("dateFinPrevue", formatDate(contrat.getDateFinPrevue()));
		variables.put("typeContrat", contrat.getType() != null ? contrat.getType().getLibelle() : "-");
		variables.put("dureeMax", contrat.getType() != null ? contrat.getType().getDureeMax() : null);
		variables.put("employe", contrat.getAffectation().getEmploye());
		variables.put("poste", contrat.getAffectation().getPoste());
		variables.put("unite", contrat.getAffectation().getPoste().getUniteOrga());
		variables.put("reference", contrat.getReference());
		return variables;
	}

	private String formatDate(LocalDate date) {
		return date == null ? "-" : DATE_FORMAT.format(date);
	}
}
