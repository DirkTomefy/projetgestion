package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.recrutement.BesoinOffreInfo;
import s5.itu.projetgestion.dto.recrutement.OffreEmploiForm;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.service.recrutement.BesoinOffreLookupService;
import s5.itu.projetgestion.service.commun.FileStorageService;
import s5.itu.projetgestion.service.recrutement.OffreEmploiService;
import s5.itu.projetgestion.service.recrutement.OffrePdfService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/rh/offres")
public class OffreEmploiApiController {

    public record OffreDto(Integer id, String ref, Integer idBesoin, String besoinRef, String poste,
                           String typeContrat, LocalDate datePublication, LocalDate dateLimiteCandidature,
                           String etat, String pdfUrl) { }

    private final OffreEmploiService offreService;
    private final BesoinOffreLookupService besoinLookup;
    private final OffrePdfService pdfService;
    private final FileStorageService fileStorageService;

    public OffreEmploiApiController(OffreEmploiService offreService,
                                    BesoinOffreLookupService besoinLookup,
                                    OffrePdfService pdfService,
                                    FileStorageService fileStorageService) {
        this.offreService = offreService;
        this.besoinLookup = besoinLookup;
        this.pdfService = pdfService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<OffreDto> liste(HttpSession session) {
        verifierRh(session);
        return offreService.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/besoins")
    public List<BesoinOffreInfo> besoinsOuverts(HttpSession session) {
        verifierRh(session);
        return besoinLookup.findRecrutementOuvert();
    }

    @GetMapping("/nouvelle")
    public Map<String, Object> nouvelle(@RequestParam Integer idBesoin, HttpSession session) {
        verifierRh(session);
        return Map.of(
                "form", offreService.nouveauFormulaire(idBesoin),
                "besoin", offreService.getBesoinOuvert(idBesoin),
                "typesContrat", offreService.typesContrat());
    }

    @PostMapping
    public ResponseEntity<OffreDto> creer(@RequestBody OffreEmploiForm form, HttpSession session) {
        verifierRh(session);
        OffreEmploi o = offreService.creer(form, currentUser(session));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(o));
    }

    @PostMapping(value = "/apercu", produces = MediaType.TEXT_HTML_VALUE)
    public String apercu(@RequestBody OffreEmploiForm form, HttpSession session) {
        verifierRh(session);
        BesoinOffreInfo b = offreService.getBesoinOuvert(form.getIdBesoin());
        TypeContrat tc = form.getIdTypeContrat() == null ? null :
                offreService.typesContrat().stream()
                        .filter(t -> t.getId().equals(form.getIdTypeContrat())).findFirst().orElse(null);
        return pdfService.renderHtml(offreService.variablesModele(b, form.getReference(),
                form.getDatePublication(), form.getDateLimiteCandidature(), tc,
                form.getDescription(), form.getProfil()));
    }

    @PostMapping("/{id}/publier")
    public OffreDto publier(@PathVariable Integer id, HttpSession session) {
        verifierRh(session);
        return toDto(offreService.publier(id, currentUser(session)));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> pdf(@PathVariable Integer id) {
        OffreEmploi o = offreService.getById(id);
        if (o.getDocument() == null) return ResponseEntity.notFound().build();
        Resource r = fileStorageService.loadAsResource(o.getDocument().getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"offre_" + o.getRef() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(r);
    }


    @ExceptionHandler({ProjetgestionException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> erreur(RuntimeException e) {
        HttpStatus status = e instanceof AccesRefuse ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("error", String.valueOf(e.getMessage())));
    }

    private static class AccesRefuse extends ProjetgestionException {
        AccesRefuse() { super("Action réservée au rôle RH"); }
    }


    private OffreDto toDto(OffreEmploi o) {
        BesoinOffreInfo b = besoinLookup.findById(o.getIdBesoin()).orElse(null);
        return new OffreDto(o.getId(), o.getRef(), o.getIdBesoin(),
                b != null ? b.reference() : null,
                b != null ? b.poste() : null,
                o.getTypeContrat() != null ? o.getTypeContrat().getLibelle() : null,
                o.getDatePublication(), o.getDateLimiteCandidature(), o.getEtat(),
                o.getDocument() != null ? "/api/rh/offres/" + o.getId() + "/pdf" : null);
    }

    private static SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
    }

    private static void verifierRh(HttpSession session) {
        SessionUser u = currentUser(session);
        if (u == null || !u.hasRole(OffreEmploiService.ROLE_RH)) throw new AccesRefuse();
    }
}