package s5.itu.projetgestion.service.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import s5.itu.projetgestion.config.UploadProperties;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.contrat.ContratDto;
import s5.itu.projetgestion.dto.contrat.EtatContratDto;
import s5.itu.projetgestion.dto.contrat.RenouvellementRequest;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.contrat.EtatContrat;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.repository.commun.TypeDocumentRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.contrat.EtatContratRepository;
import s5.itu.projetgestion.repository.contrat.TypeContratRepository;
import s5.itu.projetgestion.service.commun.FileStorageService;
import s5.itu.projetgestion.service.onboarding.OnboardingService;

@Service
public class ContratService {

    private final ContratRepository contratRepo;
    private final EtatContratRepository etatRepo;
    private final OnboardingService onboardingService;
    private final TypeDocumentRepository typeDocumentRepo ;
    private final FileStorageService fileStorageService ;
    private  final TypeContratRepository typeRepo ;
    private final UploadProperties uploadProperties;


    public ContratService(ContratRepository contratRepo, EtatContratRepository etatRepo,
                          OnboardingService onboardingService , TypeDocumentRepository typeDocumentRepository, FileStorageService fileStorageService , TypeContratRepository typeRepo,
                          UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
        this.contratRepo = contratRepo;
        this.etatRepo = etatRepo;
        this.onboardingService = onboardingService;
        this.typeDocumentRepo = typeDocumentRepository ;
        this.fileStorageService = fileStorageService ;
        this.typeRepo = typeRepo ;
    }

    // ---------- Lecture ----------

    @Transactional()
    public List<ContratDto> listerParEmploye(Integer idEmploye) {
        return contratRepo.findByAffectationEmployeIdOrderByDateDebutDesc(idEmploye).stream()
            .map(c -> versDto(c, false)).toList();
    }

    @Transactional()
    public ContratDto fiche(Integer id) {
        return versDto(trouver(id), true);
    }

    private static final String TYPE_SIGNE = "CONTRAT_SIGNE";

    // ---------- Changements d'état ----------

    @Transactional
    public ContratDto valider(Integer id) {
        Contrat c = trouver(id);
        exigerEtat(c, "CREE", "Seul un contrat CREE peut être validé.");
        changerEtat(c, "VALIDE");
        return versDto(c, true);
    }

    @Transactional
    public ContratDto signer(Integer id) {
        Contrat c = trouver(id);
        exigerEtat(c, "VALIDE", "Un contrat doit être VALIDE avant d'être signé.");
        // le PDF généré à l'embauche ne suffit pas : il faut la copie signée déposée par la RH
        if (c.getDocument() == null || !TYPE_SIGNE.equals(c.getDocument().getTypeDocument().getLibelle()))
            throw new IllegalArgumentException("Dépose d'abord le contrat signé (PDF) avant de signer.");
        changerEtat(c, "SIGNE");
        onboardingService.demarrer(c.getId());   // même transaction : si ça échoue, la signature est annulée
        return versDto(c, true);
    }

    @Transactional
    public ContratDto annuler(Integer id) {
        Contrat c = trouver(id);
        if (!"CREE".equals(c.getStatutActuel()) && !"VALIDE".equals(c.getStatutActuel()))
            throw new IllegalArgumentException("Seul un contrat CREE ou VALIDE peut être annulé.");
        changerEtat(c, "ANNULE");
        return versDto(c, true);
    }


    /* signer PDF */
    @Transactional
    public ContratDto deposerSigne(Integer id, MultipartFile fichier) {
        Contrat c = trouver(id);
        if (!"VALIDE".equals(c.getStatutActuel()))
            throw new IllegalArgumentException(
                "Le contrat signé ne peut être déposé que pour un contrat VALIDE. État actuel : "
                + c.getStatutActuel() + ".");
        if (fichier == null || fichier.isEmpty())
            throw new IllegalArgumentException("Le fichier PDF est obligatoire.");
        String nom = fichier.getOriginalFilename();
        if (nom == null || !nom.toLowerCase().endsWith(".pdf"))
            throw new IllegalArgumentException("Le fichier doit être un PDF.");

   
        Integer idType = typeDocumentRepo.findByLibelle(TYPE_SIGNE)
            .orElseThrow(() -> new IllegalStateException("Type de document CONTRAT_SIGNE manquant."))
            .getId();
        c.setDocument(fileStorageService.saveUpload(fichier, idType));
        contratRepo.save(c);
        return versDto(c, true);
    }

    // ---------- Outils ----------

    private void changerEtat(Contrat c, String nouvelEtat) {
        c.setStatutActuel(nouvelEtat);
        contratRepo.save(c);
        EtatContrat e = new EtatContrat();
        e.setContrat(c);
        e.setStatut(nouvelEtat);
        etatRepo.save(e);                         // l'historique n'est jamais écrasé
    }

    private void exigerEtat(Contrat c, String attendu, String message) {
        if (!attendu.equals(c.getStatutActuel()))
            throw new IllegalArgumentException(message + " État actuel : " + c.getStatutActuel() + ".");
    }

    private Contrat trouver(Integer id) {
        return contratRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable."));
    }

    private ContratDto versDto(Contrat c, boolean avecHistorique) {
        Employe emp = c.getAffectation().getEmploye();
        List<EtatContratDto> histo = avecHistorique
            ? etatRepo.findByContratIdOrderByIdAsc(c.getId()).stream()
                .map(e -> new EtatContratDto(e.getStatut(), e.getDate())).toList()
            : List.of();
        return new ContratDto(
            c.getId(), c.getReference(),
            emp.getNom() + (emp.getPrenoms() == null ? "" : " " + emp.getPrenoms()),
            emp.getMatricule(), c.getAffectation().getPoste().getLibelle(),
            c.getType().getLibelle(), c.getDateDebut(), c.getDateFinPrevue(),
            c.getDateFinEffective(), c.getStatutActuel(), c.getMotifFin(),
            c.getContratPrecedent() == null ? null : c.getContratPrecedent().getReference(),
            c.getDocument() != null, histo,
            c.getDocument() != null && TYPE_SIGNE.equals(c.getDocument().getTypeDocument().getLibelle()),
            c.getDocument() == null ? null : uploadProperties.getUrlPrefix() + c.getDocument().getLien());
    }

    /** Types de contrat (CE, CDD, CDI) pour le renouvellement. */
    public List<OptionDto> types() {
        return typeRepo.findAll().stream().map(t -> new OptionDto(t.getId(), t.getLibelle())).toList();
    }

    @Transactional
    public ContratDto renouveler(Integer id, RenouvellementRequest req) {
        Contrat ancien = trouver(id);
        exigerEtat(ancien, "SIGNE", "Seul un contrat SIGNE peut être renouvelé ou transformé.");

        TypeContrat ancienType = ancien.getType();
        boolean memeType = req.idTypeNouveau() == null
            || req.idTypeNouveau().equals(ancienType.getId());
        TypeContrat nouveauType = memeType ? ancienType
            : typeRepo.findById(req.idTypeNouveau())
                .orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable."));

        if ("CDI".equals(ancienType.getLibelle()))
            throw new IllegalArgumentException("Un CDI ne peut être ni renouvelé ni transformé.");
        if (!memeType && "CE".equals(nouveauType.getLibelle()))
            throw new IllegalArgumentException("On ne peut pas revenir à un contrat d'essai.");

        LocalDate debut = ancien.getDateFinPrevue() != null
            ? ancien.getDateFinPrevue().plusDays(1) : LocalDate.now();
        LocalDate fin = req.dateFinPrevue();

        if (nouveauType.getDureeMax() == null) {
            fin = null;                                   // CDI : pas de fin
        } else {
            if (fin == null) throw new IllegalArgumentException("La date de fin prévue est obligatoire.");
            if (!fin.isAfter(debut)) throw new IllegalArgumentException("La date de fin doit suivre le début.");
        }

        // Chaîne des contrats précédents du même type que le nouveau
        List<Contrat> chaine = new ArrayList<>();
        for (Contrat c = ancien; c != null; c = c.getContratPrecedent()) chaine.add(c);
        List<Contrat> memeTypeChaine = chaine.stream()
            .filter(c -> c.getType().getId().equals(nouveauType.getId())).toList();

        if (memeType && nouveauType.getRenouvellementMax() != null
                && memeTypeChaine.size() > nouveauType.getRenouvellementMax())
            throw new IllegalArgumentException("Nombre maximum de renouvellements atteint pour "
                + nouveauType.getLibelle() + " (" + nouveauType.getRenouvellementMax() + ").");

        if (nouveauType.getDureeMax() != null) {
            long moisNouveau = ChronoUnit.MONTHS.between(debut, fin.plusDays(1));
            if ("CE".equals(nouveauType.getLibelle())) {
                if (moisNouveau > nouveauType.getDureeMax())
                    throw new IllegalArgumentException("Un CE dure au plus " + nouveauType.getDureeMax() + " mois.");
            } else {
                long cumul = moisNouveau + memeTypeChaine.stream()
                    .filter(c -> c.getDateFinPrevue() != null)
                    .mapToLong(c -> ChronoUnit.MONTHS.between(c.getDateDebut(), c.getDateFinPrevue().plusDays(1)))
                    .sum();
                if (cumul > nouveauType.getDureeMax())
                    throw new IllegalArgumentException("Durée cumulée de " + cumul + " mois : maximum "
                        + nouveauType.getDureeMax() + " mois pour un " + nouveauType.getLibelle() + ".");
            }
        }

        // Terminer l'ancien contrat (historique conservé)
        ancien.setDateFinEffective(debut.minusDays(1));
        ancien.setMotifFin(memeType ? "Renouvellement" : "Changement de type : "
            + ancienType.getLibelle() + " vers " + nouveauType.getLibelle());
        changerEtat(ancien, "TERMINE");

        // Nouveau contrat
        Contrat nouveau = new Contrat();
        nouveau.setReference(genererReference());
        nouveau.setAffectation(ancien.getAffectation());
        nouveau.setType(nouveauType);
        nouveau.setDateDebut(debut);
        nouveau.setDateFinPrevue(fin);
        nouveau.setContratPrecedent(ancien);
        nouveau.setStatutActuel("CREE");
        nouveau = contratRepo.save(nouveau);
        EtatContrat e = new EtatContrat();
        e.setContrat(nouveau);
        e.setStatut("CREE");
        etatRepo.save(e);
        return versDto(nouveau, true);
    }

    @Transactional()
    public List<ContratDto> echeances(int jours) {
        LocalDate aujourdhui = LocalDate.now();
        return contratRepo.findByStatutActuelAndDateFinPrevueBetweenOrderByDateFinPrevueAsc(
                "SIGNE", aujourdhui, aujourdhui.plusDays(jours)).stream()
            .map(c -> versDto(c, false)).toList();
    }

    private String genererReference() {
        String prefix = "CTR-" + LocalDate.now().getYear() + "-";
        long n = contratRepo.countByReferenceStartingWith(prefix) + 1;
        String ref = prefix + String.format("%04d", n);
        while (contratRepo.existsByReference(ref)) { n++; ref = prefix + String.format("%04d", n); }
        return ref;
    }
}