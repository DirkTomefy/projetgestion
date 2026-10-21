package s5.itu.projetgestion.service.rh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import s5.itu.projetgestion.config.UploadProperties;
import s5.itu.projetgestion.dto.rh.CandidatureDeposeeResponse;
import s5.itu.projetgestion.dto.rh.CandidatureRequest;
import s5.itu.projetgestion.dto.rh.DetailCandidatRequest;
import s5.itu.projetgestion.err.CandidatureException;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.personnel.Candidat;
import s5.itu.projetgestion.model.personnel.CandidatDetail;
import s5.itu.projetgestion.model.embauche.Candidature;
import s5.itu.projetgestion.model.rh.Document;
import s5.itu.projetgestion.model.embauche.DocumentCandidat;
import s5.itu.projetgestion.model.rh.TypeDocument;
import s5.itu.projetgestion.model.personnel.TypeInformation;
import s5.itu.projetgestion.repository.personnel.CandidatDetailRepository;
import s5.itu.projetgestion.repository.personnel.CandidatRepository;
import s5.itu.projetgestion.repository.embauche.CandidatureRepository;
import s5.itu.projetgestion.repository.embauche.DocumentCandidatRepository;
import s5.itu.projetgestion.repository.rh.TypeDocumentRepository;
import s5.itu.projetgestion.repository.personnel.TypeInformationRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CandidaturePubliqueService {

    private static final Logger log = LoggerFactory.getLogger(CandidaturePubliqueService.class);
    public static final String TYPE_DOCUMENT_CV = "CV";

    private final OffrePubliqueService offres;
    private final CandidatRepository candidatRepo;
    private final CandidatDetailRepository detailRepo;
    private final TypeInformationRepository typeInformationRepo;
    private final CandidatureRepository candidatureRepo;
    private final DocumentCandidatRepository documentCandidatRepo;
    private final TypeDocumentRepository typeDocumentRepo;
    private final FileStorageService fileStorageService;
    private final UploadProperties uploadProperties;
    private final CvPdfValidator cvValidator;

    public CandidaturePubliqueService(OffrePubliqueService offres,
            CandidatRepository candidatRepo,
            CandidatDetailRepository detailRepo,
            TypeInformationRepository typeInformationRepo,
            CandidatureRepository candidatureRepo,
            DocumentCandidatRepository documentCandidatRepo,
            TypeDocumentRepository typeDocumentRepo,
            FileStorageService fileStorageService,
            UploadProperties uploadProperties,
            CvPdfValidator cvValidator) {
        this.offres = offres;
        this.candidatRepo = candidatRepo;
        this.detailRepo = detailRepo;
        this.typeInformationRepo = typeInformationRepo;
        this.candidatureRepo = candidatureRepo;
        this.documentCandidatRepo = documentCandidatRepo;
        this.typeDocumentRepo = typeDocumentRepo;
        this.fileStorageService = fileStorageService;
        this.uploadProperties = uploadProperties;
        this.cvValidator = cvValidator;
    }

    @Transactional
    public CandidatureDeposeeResponse deposer(CandidatureRequest demande, MultipartFile cv) {
        cvValidator.verifier(cv);
        if (!offres.estOuverte(demande.idOffreEmploi())) {
            throw new CandidatureException(409, "Cette offre n'est plus ouverte aux candidatures");
        }
        Map<String, TypeInformation> typesParCode = typeInformationRepo.findAll().stream()
                .collect(Collectors.toMap(TypeInformation::getLibelle, Function.identity()));
        verifierTypes(demande.detailsOuVide(), typesParCode);
        TypeDocument typeCv = typeDocumentRepo.findByLibelle(TYPE_DOCUMENT_CV)
                .orElseThrow(() -> new ProjetgestionException(
                        "Type de document 'CV' absent : exécuter data_test_document.sql"));

        String email = demande.email().trim().toLowerCase(Locale.ROOT);
        Optional<Candidat> dejaConnu = candidatRepo.findFirstByEmailIgnoreCase(email);
        if (dejaConnu.isPresent() && candidatureRepo.existsByCandidat_IdAndIdOffreEmploi(
                dejaConnu.get().getId(), demande.idOffreEmploi())) {
            throw new CandidatureException(409, "Vous avez déjà postulé à cette offre");
        }

        Document document = fileStorageService.saveUpload(cv, typeCv.getId());
        supprimerFichierSiAnnulation(document.getLien());

        Candidat candidat = dejaConnu.orElseGet(() -> creerCandidat(demande, email));

        ajouterDetails(candidat, demande.detailsOuVide(), typesParCode);

        Candidature candidature = new Candidature();
        candidature.setCandidat(candidat);
        candidature.setIdOffreEmploi(demande.idOffreEmploi());
        candidature.setDateDepot(LocalDate.now());
        candidature.setEtat(Candidature.ETAT_DEPOSEE);
        candidature = candidatureRepo.save(candidature);

        DocumentCandidat lien = new DocumentCandidat();
        lien.setCandidature(candidature);
        lien.setDocument(document);
        documentCandidatRepo.save(lien);

        String reference = offres.trouverOffre(demande.idOffreEmploi()).reference();
        return new CandidatureDeposeeResponse(candidature.getId(), reference, candidature.getDateDepot(),
                "Votre candidature a bien été enregistrée.");
    }

    private Candidat creerCandidat(CandidatureRequest demande, String email) {
        Candidat c = new Candidat();
        c.setNom(demande.nom().trim());
        c.setPrenom(nettoyer(demande.prenom()));
        c.setDateNaissance(demande.dateNaissance());
        c.setAdresse(nettoyer(demande.adresse()));
        c.setNumero(demande.telephone().trim());
        c.setEmail(email);
        return candidatRepo.save(c);
    }

    private static void verifierTypes(List<DetailCandidatRequest> lignes, Map<String, TypeInformation> typesParCode) {
        for (int i = 0; i < lignes.size(); i++) {
            if (!typesParCode.containsKey(lignes.get(i).codeTypeInformation())) {
                throw new CandidatureException(400, "Type d'information inconnu",
                        "details[" + i + "].codeTypeInformation");
            }
        }
    }

    private void ajouterDetails(Candidat candidat, List<DetailCandidatRequest> lignes,
            Map<String, TypeInformation> typesParCode) {
        List<CandidatDetail> connus = new ArrayList<>(detailRepo.findByCandidat_Id(candidat.getId()));
        for (DetailCandidatRequest ligne : lignes) {
            TypeInformation type = typesParCode.get(ligne.codeTypeInformation());
            String libelle = ligne.libelle().trim();
            boolean dejaConnue = connus.stream()
                    .anyMatch(d -> d.estEquivalente(type.getId(), libelle, ligne.dateDebut(), ligne.dateFin()));
            if (!dejaConnue) {
                CandidatDetail d = new CandidatDetail();
                d.setCandidat(candidat);
                d.setTypeInformation(type);
                d.setLibelle(libelle);
                d.setDateDebut(ligne.dateDebut());
                d.setDateFin(ligne.dateFin());
                connus.add(detailRepo.save(d));
            }
        }
    }

    private void supprimerFichierSiAnnulation(String lien) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    supprimerFichier(lien);
                }
            }
        });
    }

    private void supprimerFichier(String lien) {
        Path racine = Paths.get(uploadProperties.getDir()).toAbsolutePath().normalize();
        Path fichier = racine.resolve(lien).normalize();
        if (!fichier.startsWith(racine)) {
            return;
        }
        try {
            Files.deleteIfExists(fichier);
        } catch (IOException e) {
            log.warn("Fichier de CV orphelin non supprimé : {}", fichier, e);
        }
    }

    private static String nettoyer(String texte) {
        return texte == null || texte.isBlank() ? null : texte.trim();
    }
}
