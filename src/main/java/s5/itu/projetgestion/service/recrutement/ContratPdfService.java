package s5.itu.projetgestion.service.recrutement;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.contrat.ContratDetail;
import s5.itu.projetgestion.model.contrat.ContratRemuneration;
import s5.itu.projetgestion.model.commun.Document;
import s5.itu.projetgestion.model.commun.TypeDocument;
import s5.itu.projetgestion.repository.contrat.ContratDetailRepository;
import s5.itu.projetgestion.repository.contrat.ContratRemunerationRepository;
import s5.itu.projetgestion.repository.commun.TypeDocumentRepository;
import s5.itu.projetgestion.service.commun.ByteArrayMultipartFile;
import s5.itu.projetgestion.service.commun.FileStorageService;

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
	private static final String RAISON_SOCIALE = "Gestion RH";
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
		variables.put("raisonSociale", RAISON_SOCIALE);
		variables.put("typeCode", contrat.getType() != null ? contrat.getType().getLibelle() : "");
		variables.put("renouvMax", contrat.getType() != null ? contrat.getType().getRenouvellementMax() : null);
		variables.put("precedent", contrat.getContratPrecedent() != null ? contrat.getContratPrecedent().getReference() : null);
		var poste = contrat.getAffectation().getPoste();
		variables.put("metier", poste.getMetier() != null ? poste.getMetier().getLibelle() : null);
		variables.put("superieur", poste.getSuperieurDirect() != null ? poste.getSuperieurDirect().getLibelle() : null);

		// conditions convenues : horaire et lieu ont leur article, le reste est listé ; salaire, date et type sont déjà dans le contrat
		String horaire = null, lieu = null;
		List<String> autres = new java.util.ArrayList<>();
		for (ContratDetail d : details) {
			String code = d.getPointCondition().getLibelle();
			String val = d.getValeur() == null ? "" : d.getValeur().trim();
			if (val.isEmpty()) continue;
			switch (code) {
				case "HORAIRE" -> horaire = val;
				case "LIEU" -> lieu = val;
				case "CONDITIONS" -> autres.add(val);
				default -> { }   // SALAIRE, DATE_DEBUT, TYPE_CONTRAT
			}
		}
		variables.put("horaire", horaire);
		variables.put("lieu", lieu);
		variables.put("autres", autres);
		variables.put("salaire", remunerations.isEmpty() ? "-" : montant(remunerations.get(0).getSalaire()));
		return variables;
	}

	/** Montant en ariary, séparateur de milliers espace : « 1 200 000 Ar ». */
	static String montant(Double valeur) {
		if (valeur == null) return "-";
		java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols(java.util.Locale.FRANCE);
		sym.setGroupingSeparator(' ');
		return new java.text.DecimalFormat("#,##0", sym).format(valeur) + " Ar";
	}

	private String formatDate(LocalDate date) {
		return date == null ? "-" : DATE_FORMAT.format(date);
	}
}
