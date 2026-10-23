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
    private final s5.itu.projetgestion.repository.contrat.ContratRemunerationRepository remunerationRepo;
    private final s5.itu.projetgestion.service.finance.GrilleSalarialeService grilleService;


    public ContratService(ContratRepository contratRepo, EtatContratRepository etatRepo,
                          OnboardingService onboardingService , TypeDocumentRepository typeDocumentRepository, FileStorageService fileStorageService , TypeContratRepository typeRepo,
                          UploadProperties uploadProperties,
                          s5.itu.projetgestion.repository.contrat.ContratRemunerationRepository remunerationRepo,
                          s5.itu.projetgestion.service.finance.GrilleSalarialeService grilleService) {
        this.uploadProperties = uploadProperties;
        this.remunerationRepo = remunerationRepo;
        this.grilleService = grilleService;
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

    /**
     * Fin d'un contrat (départ de l'employé) : un contrat SIGNE passe TERMINE avec sa date de fin effective et son motif ;
     * un contrat pas encore signé (CREE, VALIDE) est ANNULE. L'historique des états est conservé.
     */
    @Transactional
    public void terminerPourDepart(Integer id, LocalDate dateFin, String motif) {
        Contrat c = trouver(id);
        switch (c.getStatutActuel()) {
            case "SIGNE" -> {
                c.setDateFinEffective(dateFin);
                c.setMotifFin(motif);
                changerEtat(c, "TERMINE");
            }
            case "CREE", "VALIDE" -> {
                c.setMotifFin(motif);
                changerEtat(c, "ANNULE");
            }
            default -> { }
        }
    }

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
            c.getDocument() == null ? null : uploadProperties.getUrlPrefix() + c.getDocument().getLien(),
            emp.getId(), c.getContratPrecedent() == null ? null : c.getContratPrecedent().getId());
    }

    /** Types de contrat (CE, CDD, CDI) pour le renouvellement. */
    public List<OptionDto> types() {
        return typeRepo.findAll().stream().map(t -> new OptionDto(t.getId(), t.getLibelle())).toList();
    }

    @Transactional
    public ContratDto renouveler(Integer id, RenouvellementRequest req) {
        return renouveler(id, req, null, null, false);
    }

    /**
     * Renouvellement / transformation. salaireImpose et dateEffet sont facultatifs (par défaut : salaire en vigueur,
     * lendemain de la fin prévue). En simulation, toutes les règles sont contrôlées mais rien n'est enregistré.
     */
    @Transactional
    public ContratDto renouveler(Integer id, RenouvellementRequest req, java.math.BigDecimal salaireImpose,
                                 LocalDate dateEffet, boolean simulation) {
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

        LocalDate debut = dateEffet != null ? dateEffet
            : ancien.getDateFinPrevue() != null ? ancien.getDateFinPrevue().plusDays(1) : LocalDate.now();
        if (!debut.isAfter(ancien.getDateDebut()))
            throw new IllegalArgumentException("Le nouveau contrat doit commencer après le début du contrat actuel.");
        LocalDate fin = req.dateFinPrevue();
        if (salaireImpose != null) verifierGrille(ancien, salaireImpose, debut);

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

        if (simulation) return null;

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
        // le salaire en vigueur passe au nouveau contrat (sinon la masse salariale le perdrait)
        if (salaireImpose != null) remunerer(nouveau, debut, salaireImpose);
        else {
            var salaire = remunerationRepo.findByContratIdOrderByDateEffetDesc(ancien.getId()).stream().findFirst().orElse(null);
            if (salaire != null) remunerer(nouveau, debut, java.math.BigDecimal.valueOf(salaire.getSalaire()));
        }
        return versDto(nouveau, true);
    }

    @Transactional()
    public List<ContratDto> echeances(int jours) {
        LocalDate aujourdhui = LocalDate.now();
        return contratRepo.findByStatutActuelAndDateFinPrevueBetweenOrderByDateFinPrevueAsc(
                "SIGNE", aujourdhui, aujourdhui.plusDays(jours)).stream()
            .map(c -> versDto(c, false)).toList();
    }

    /**
     * Reprise d'un employé déjà en poste (saisie de l'existant) : contrat directement SIGNE, sans onboarding,
     * avec le salaire mensuel, qui doit être dans la fourchette de la grille validée à la date de début.
     */
    @Transactional
    public Contrat creerReprise(s5.itu.projetgestion.model.organisation.Affectation affectation, Integer idType,
                                LocalDate finPrevue, java.math.BigDecimal salaire) {
        if (idType == null) throw new IllegalArgumentException("Le type de contrat est obligatoire.");
        TypeContrat type = typeRepo.findById(idType).orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable."));
        if (salaire == null || salaire.signum() <= 0) throw new IllegalArgumentException("Le salaire mensuel est obligatoire.");
        LocalDate debut = affectation.getDateDebut();
        grilleService.fourchette(affectation.getPoste().getId(), debut).ifPresent(f -> {
            if (!f.contient(salaire))
                throw new IllegalArgumentException(String.format(java.util.Locale.FRANCE,
                        "Le salaire doit être compris entre %,.0f et %,.0f Ar (grille : catégorie %s, échelon %s).",
                        f.min(), f.max(), f.categorie(), f.echelon()));
        });
        if (type.getDureeMax() == null) finPrevue = null;   // CDI
        else {
            if (finPrevue == null) finPrevue = debut.plusMonths(type.getDureeMax()).minusDays(1);
            if (!finPrevue.isAfter(debut)) throw new IllegalArgumentException("La date de fin doit suivre le début.");
        }
        Contrat c = new Contrat();
        c.setReference(genererReference());
        c.setAffectation(affectation);
        c.setType(type);
        c.setDateDebut(debut);
        c.setDateFinPrevue(finPrevue);
        c.setStatutActuel("CREE");
        c = contratRepo.save(c);
        for (String etat : List.of("CREE", "VALIDE", "SIGNE")) {
            c.setStatutActuel(etat);
            EtatContrat e = new EtatContrat();
            e.setContrat(c);
            e.setStatut(etat);
            etatRepo.save(e);
        }
        contratRepo.save(c);
        remunerer(c, debut, salaire);
        return c;
    }

    /** Salaire mensuel en vigueur sur le contrat (0 si aucun). */
    @Transactional
    public java.math.BigDecimal salaireActuel(Integer idContrat) {
        return remunerationRepo.findByContratIdOrderByDateEffetDesc(idContrat).stream().findFirst()
                .map(r -> java.math.BigDecimal.valueOf(r.getSalaire())).orElse(java.math.BigDecimal.ZERO);
    }

    /** Fourchette de la grille pour le poste du contrat à une date (null si aucune grille). */
    @Transactional
    public s5.itu.projetgestion.service.finance.FourchetteSalariale fourchette(Contrat c, LocalDate date) {
        return grilleService.fourchette(c.getAffectation().getPoste().getId(), date).orElse(null);
    }

    /** Le salaire doit être dans la fourchette de la grille en vigueur à la date d'effet. */
    @Transactional
    public void verifierGrille(Contrat c, java.math.BigDecimal salaire, LocalDate date) {
        if (salaire == null || salaire.signum() <= 0) throw new IllegalArgumentException("Le salaire mensuel est obligatoire.");
        grilleService.fourchette(c.getAffectation().getPoste().getId(), date).ifPresent(f -> {
            if (!f.contient(salaire))
                throw new IllegalArgumentException(String.format(java.util.Locale.FRANCE,
                        "Le salaire doit être compris entre %,.0f et %,.0f Ar (grille : catégorie %s, échelon %s).",
                        f.min(), f.max(), f.categorie(), f.echelon()));
        });
    }

    /** Avenant de salaire : une nouvelle ligne de rémunération sur le contrat signé, à la date d'effet. */
    @Transactional
    public Contrat avenantSalaire(Integer idContrat, LocalDate dateEffet, java.math.BigDecimal salaire) {
        Contrat c = trouver(idContrat);
        exigerEtat(c, "SIGNE", "Seul un contrat SIGNE peut recevoir un avenant de salaire.");
        if (dateEffet == null || dateEffet.isBefore(c.getDateDebut()))
            throw new IllegalArgumentException("La date d'effet ne peut pas précéder le début du contrat.");
        verifierGrille(c, salaire, dateEffet);
        remunerer(c, dateEffet, salaire);
        return c;
    }

    private void remunerer(Contrat c, LocalDate dateEffet, java.math.BigDecimal salaire) {
        s5.itu.projetgestion.model.contrat.ContratRemuneration r = new s5.itu.projetgestion.model.contrat.ContratRemuneration();
        r.setContrat(c);
        r.setDateEffet(dateEffet);
        r.setSalaire(salaire.doubleValue());
        r.setEchelon(c.getAffectation().getPoste().getEchelon());
        remunerationRepo.save(r);
    }

    private String genererReference() {
        String prefix = "CTR-" + LocalDate.now().getYear() + "-";
        long n = contratRepo.countByReferenceStartingWith(prefix) + 1;
        String ref = prefix + String.format("%04d", n);
        while (contratRepo.existsByReference(ref)) { n++; ref = prefix + String.format("%04d", n); }
        return ref;
    }
}