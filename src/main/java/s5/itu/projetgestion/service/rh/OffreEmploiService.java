package s5.itu.projetgestion.service.rh;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.SessionUser;
import s5.itu.projetgestion.dto.rh.BesoinOffreInfo;
import s5.itu.projetgestion.dto.rh.OffreEmploiForm;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.rh.Document;
import s5.itu.projetgestion.model.rh.OffreEmploi;
import s5.itu.projetgestion.model.rh.TypeContrat;
import s5.itu.projetgestion.model.rh.TypeDocument;
import s5.itu.projetgestion.repository.rh.OffreEmploiRepository;
import s5.itu.projetgestion.repository.rh.TypeContratRepository;
import s5.itu.projetgestion.repository.rh.TypeDocumentRepository;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Service
public class OffreEmploiService {

    public static final String ROLE_RH = "RH";
    public static final String TYPE_DOCUMENT_OFFRE = "OFFRE";

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OffreEmploiRepository offreRepo;
    private final TypeContratRepository typeContratRepo;
    private final TypeDocumentRepository typeDocumentRepo;
    private final BesoinOffreLookupService besoinLookup;
    private final OffrePdfService pdfService;
    private final FileStorageService fileStorageService;

    public OffreEmploiService(OffreEmploiRepository offreRepo,
                              TypeContratRepository typeContratRepo,
                              TypeDocumentRepository typeDocumentRepo,
                              BesoinOffreLookupService besoinLookup,
                              OffrePdfService pdfService,
                              FileStorageService fileStorageService) {
        this.offreRepo = offreRepo;
        this.typeContratRepo = typeContratRepo;
        this.typeDocumentRepo = typeDocumentRepo;
        this.besoinLookup = besoinLookup;
        this.pdfService = pdfService;
        this.fileStorageService = fileStorageService;
    }


    public List<OffreEmploi> findAll() {
        return offreRepo.findAllByOrderByIdDesc();
    }

    public OffreEmploi getById(Integer id) {
        return offreRepo.findById(id)
                .orElseThrow(() -> new ProjetgestionException("Offre introuvable : " + id));
    }

    public List<TypeContrat> typesContrat() {
        return typeContratRepo.findAll();
    }

    public BesoinOffreInfo getBesoinOuvert(Integer idBesoin) {
        BesoinOffreInfo b = besoinLookup.findById(idBesoin)
                .orElseThrow(() -> new ProjetgestionException("Besoin introuvable : " + idBesoin));
        if (!BesoinOffreLookupService.ETATS_RECRUTEMENT.contains(b.etatActuel())) {
            throw new ProjetgestionException("Le recrutement n'est pas ouvert pour le besoin "
                    + b.reference() + " (état : " + b.etatActuel() + ")");
        }
        return b;
    }

    public OffreEmploiForm nouveauFormulaire(Integer idBesoin) {
        BesoinOffreInfo b = getBesoinOuvert(idBesoin);
        OffreEmploiForm f = new OffreEmploiForm();
        f.setIdBesoin(b.id());
        f.setDatePublication(LocalDate.now());
        f.setDateLimiteCandidature(LocalDate.now().plusDays(30));
        f.setReference(proposerReference(b));
        return f;
    }

    private String proposerReference(BesoinOffreInfo b) {
        String base = "OFF-" + LocalDate.now().getYear() + "-" + String.format("%04d", b.id());
        String ref = base;
        int i = 2;
        while (offreRepo.existsByRef(ref)) {
            ref = base + "-" + i++;
        }
        return ref;
    }


    @Transactional
    public OffreEmploi creer(OffreEmploiForm form, SessionUser user) {
        verifierRh(user);
        BesoinOffreInfo besoin = getBesoinOuvert(form.getIdBesoin());

        String ref = form.getReference() == null ? "" : form.getReference().trim();
        if (ref.isEmpty()) throw new ProjetgestionException("La référence est obligatoire");
        if (ref.length() > 50) throw new ProjetgestionException("La référence ne doit pas dépasser 50 caractères");
        if (offreRepo.existsByRef(ref)) throw new ProjetgestionException("La référence " + ref + " existe déjà");

        LocalDate datePub = form.getDatePublication() != null ? form.getDatePublication() : LocalDate.now();
        LocalDate dateLimite = form.getDateLimiteCandidature();
        if (dateLimite == null) throw new ProjetgestionException("La date limite de candidature est obligatoire");
        if (dateLimite.isBefore(datePub))
            throw new ProjetgestionException("La date limite doit être postérieure ou égale à la date de publication");

        String etat = OffreEmploi.ETAT_BROUILLON.equals(form.getEtat())
                ? OffreEmploi.ETAT_BROUILLON : OffreEmploi.ETAT_PUBLIEE;

        if (offreRepo.existsByIdBesoinAndEtatIn(besoin.id(),
                List.of(OffreEmploi.ETAT_BROUILLON, OffreEmploi.ETAT_PUBLIEE))) {
            throw new ProjetgestionException("Une offre est déjà en cours pour le besoin " + besoin.reference());
        }

        TypeContrat typeContrat = null;   // facultatif
        if (form.getIdTypeContrat() != null) {
            typeContrat = typeContratRepo.findById(form.getIdTypeContrat())
                    .orElseThrow(() -> new ProjetgestionException("Type de contrat introuvable"));
        }

        Map<String, Object> vars = variablesModele(besoin, ref, datePub, dateLimite, typeContrat,
                form.getDescription(), form.getProfil());
        byte[] pdf = pdfService.renderPdf(vars);

        TypeDocument typeOffre = typeDocumentRepo.findByLibelle(TYPE_DOCUMENT_OFFRE)
                .orElseThrow(() -> new ProjetgestionException(
                        "Type de document 'OFFRE' absent : exécuter data_test_document.sql"));
        Document doc = fileStorageService.saveUpload(
                new ByteArrayMultipartFile(pdf, "offre_" + ref + ".pdf", "application/pdf"),
                typeOffre.getId());

        OffreEmploi o = new OffreEmploi();
        o.setIdBesoin(besoin.id());
        o.setRef(ref);
        o.setTypeContrat(typeContrat);
        o.setDatePublication(datePub);
        o.setDateLimiteCandidature(dateLimite);
        o.setDocument(doc);
        o.setEtat(etat);
        return offreRepo.save(o);
    }

    @Transactional
    public OffreEmploi publier(Integer idOffre, SessionUser user) {
        verifierRh(user);
        OffreEmploi o = getById(idOffre);
        if (!OffreEmploi.ETAT_BROUILLON.equals(o.getEtat()))
            throw new ProjetgestionException("Seule une offre en BROUILLON peut être publiée");
        if (o.getDateLimiteCandidature() != null && o.getDateLimiteCandidature().isBefore(LocalDate.now()))
            throw new ProjetgestionException("La date limite de candidature est déjà dépassée");
        o.setEtat(OffreEmploi.ETAT_PUBLIEE);
        return o;   // entité managée : mise à jour au commit
    }


    public Map<String, Object> variablesModele(BesoinOffreInfo b, String ref, LocalDate datePub,
                                               LocalDate dateLimite, TypeContrat typeContrat,
                                               String description, String profil) {
        Map<String, Object> v = new HashMap<>();
        v.put("ref", ref);
        v.put("poste", b.poste() != null ? b.poste() : "Poste à pourvoir");
        v.put("metier", b.metier());
        v.put("categorie", b.categorie());
        v.put("echelon", b.echelon());
        v.put("unite", b.unite());
        v.put("uniteType", b.uniteType());
        v.put("effectif", b.effectif());
        v.put("typeContrat", typeContrat != null ? typeContrat.getLibelle() : null);
        v.put("datePublication", datePub != null ? datePub.format(FR_DATE) : null);
        v.put("dateLimite", dateLimite != null ? dateLimite.format(FR_DATE) : null);
        v.put("datePriseFonction", b.dateSouhaitee() != null ? b.dateSouhaitee().format(FR_DATE) : null);
        v.put("salaire", fourchette(b.salaireMin(), b.salaireMax()));
        v.put("description", blankToNull(description));
        v.put("profil", blankToNull(profil));
        v.put("besoinRef", b.reference());
        return v;
    }

    private static String fourchette(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.FRANCE);
        s.setGroupingSeparator(' ');
        DecimalFormat f = new DecimalFormat("#,##0", s);
        if (min != null && max != null) return f.format(min) + " – " + f.format(max) + " Ar / mois";
        return f.format(min != null ? min : max) + " Ar / mois";
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static void verifierRh(SessionUser user) {
        if (user == null || !user.hasRole(ROLE_RH)) {
            throw new ProjetgestionException("Action réservée au rôle RH");
        }
    }
}
