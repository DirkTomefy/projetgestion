package s5.itu.projetgestion.service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.contrat.DecisionContratRequest;
import s5.itu.projetgestion.dto.contrat.DemandeContratDto;
import s5.itu.projetgestion.dto.contrat.DemandeContratRequest;
import s5.itu.projetgestion.dto.contrat.RenouvellementRequest;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.contrat.DemandeContrat;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.contrat.DemandeContratRepository;
import s5.itu.projetgestion.repository.contrat.TypeContratRepository;
import s5.itu.projetgestion.service.finance.BudgetService;
import s5.itu.projetgestion.service.notification.NotificationService;
import s5.itu.projetgestion.service.organisation.PerimetreService;

/**
 * Nouveau contrat ou avenant pour un employé en poste. Circuit : RH → (Finance si le surcoût dépasse le disponible
 * du budget de l'unité) → DG. Une demande de l'employé ou de son manager commence par l'instruction de la RH.
 */
@Service
public class DemandeContratService {

    public static final String RENOUVELLEMENT = "RENOUVELLEMENT";
    public static final String TRANSFORMATION = "TRANSFORMATION";
    public static final String AUGMENTATION = "AUGMENTATION";
    private static final Set<String> MOTIFS = Set.of(RENOUVELLEMENT, TRANSFORMATION, AUGMENTATION);
    private static final List<String> OUVERTES = List.of(DemandeContrat.A_INSTRUIRE, DemandeContrat.A_VALIDER_FINANCE, DemandeContrat.A_VALIDER_DG);

    private final DemandeContratRepository repo;
    private final ContratRepository contratRepo;
    private final TypeContratRepository typeRepo;
    private final UserRepository userRepo;
    private final ContratService contratService;
    private final BudgetService budgetService;
    private final NotificationService notifications;
    private final PerimetreService perimetre;

    public DemandeContratService(DemandeContratRepository repo, ContratRepository contratRepo, TypeContratRepository typeRepo,
                                 UserRepository userRepo, ContratService contratService, BudgetService budgetService,
                                 NotificationService notifications, PerimetreService perimetre) {
        this.repo = repo;
        this.contratRepo = contratRepo;
        this.typeRepo = typeRepo;
        this.userRepo = userRepo;
        this.contratService = contratService;
        this.budgetService = budgetService;
        this.notifications = notifications;
        this.perimetre = perimetre;
    }

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<DemandeContratDto> lister(String vue, Integer idEmploye, SessionUser su) {
        List<DemandeContrat> l;
        if ("a-traiter".equals(vue)) {
            List<String> etats = new ArrayList<>();
            if (su.hasRole("RH")) etats.add(DemandeContrat.A_INSTRUIRE);
            if (su.hasRole("Finance")) etats.add(DemandeContrat.A_VALIDER_FINANCE);
            if (su.hasRole("DG")) etats.add(DemandeContrat.A_VALIDER_DG);
            l = etats.isEmpty() ? List.of() : repo.parEtats(etats);
        } else if ("mes".equals(vue)) {
            Integer emp = su.getIdEmploye();
            l = repo.mesDemandes(su.getId(), emp);
        } else {
            if (!(su.hasRole("RH") || su.hasRole("DG") || su.hasRole("Finance") || su.hasRole("ADMIN")))
                throw new AccesRefuseException("Liste réservée à la RH, au DG et à la Finance.");
            l = repo.toutes(idEmploye);
        }
        return l.stream().map(d -> dto(d, su)).toList();
    }

    @Transactional(readOnly = true)
    public DemandeContratDto fiche(Integer id, SessionUser su) {
        DemandeContrat d = trouver(id);
        boolean role = su.hasRole("RH") || su.hasRole("DG") || su.hasRole("Finance") || su.hasRole("ADMIN");
        boolean concerne = d.getIdDemandeur().equals(su.getId())
                || (su.getIdEmploye() != null && su.getIdEmploye().equals(d.getContrat().getAffectation().getEmploye().getId()));
        if (!role && !concerne) throw new AccesRefuseException("Vous n'avez pas accès à cette demande.");
        return dto(d, su);
    }

    /** Nombre de demandes qui attendent une action de l'utilisateur (accueil, barre latérale). */
    @Transactional(readOnly = true)
    public long aTraiter(SessionUser su) {
        long n = 0;
        if (su.hasRole("RH")) n += repo.countByEtat(DemandeContrat.A_INSTRUIRE);
        if (su.hasRole("Finance")) n += repo.countByEtat(DemandeContrat.A_VALIDER_FINANCE);
        if (su.hasRole("DG")) n += repo.countByEtat(DemandeContrat.A_VALIDER_DG);
        return n;
    }

    /** Contrat signé en cours de l'employé connecté, avec son salaire (null s'il n'a pas de compte employé ou de contrat). */
    @Transactional(readOnly = true)
    public MonContrat monContrat(SessionUser su) {
        if (su.getIdEmploye() == null) return null;
        return contratRepo.findByAffectationEmployeIdOrderByDateDebutDesc(su.getIdEmploye()).stream()
                .filter(c -> "SIGNE".equals(c.getStatutActuel())).findFirst()
                .map(c -> {
                    var f = contratService.fourchette(c, LocalDate.now());
                    return new MonContrat(contratService.fiche(c.getId()), contratService.salaireActuel(c.getId()),
                            f == null ? null : f.min(), f == null ? null : f.max(),
                            repo.existsByContratIdAndEtatIn(c.getId(), OUVERTES));
                }).orElse(null);
    }

    /** Contrats signés pour lesquels l'utilisateur peut faire une demande : tous (RH, DG), son équipe (manager), le sien. */
    @Transactional(readOnly = true)
    public List<ContratEligible> eligibles(SessionUser su) {
        boolean tous = su.hasRole("RH") || su.hasRole("DG");
        Set<Integer> unites = !tous && su.hasRole("Manager") ? perimetre.unitesDemandables(su) : Set.of();
        return contratRepo.findSignes().stream()
                .filter(c -> tous
                        || (su.getIdEmploye() != null && su.getIdEmploye().equals(c.getAffectation().getEmploye().getId()))
                        || unites.contains(c.getAffectation().getPoste().getUniteOrga().getId()))
                .map(c -> {
                    var f = contratService.fourchette(c, LocalDate.now());
                    return new ContratEligible(c.getId(), c.getReference(), nomEmploye(c.getAffectation().getEmploye()),
                            c.getAffectation().getEmploye().getMatricule(), c.getAffectation().getPoste().getLibelle(),
                            c.getType().getLibelle(), c.getDateFinPrevue(), contratService.salaireActuel(c.getId()),
                            f == null ? null : f.min(), f == null ? null : f.max(),
                            repo.existsByContratIdAndEtatIn(c.getId(), OUVERTES));
                }).toList();
    }

    public record ContratEligible(Integer idContrat, String reference, String employe, String matricule, String poste,
                                  String type, LocalDate dateFinPrevue, BigDecimal salaire,
                                  BigDecimal salaireMin, BigDecimal salaireMax, boolean demandeEnCours) { }

    public record MonContrat(s5.itu.projetgestion.dto.contrat.ContratDto contrat, BigDecimal salaire,
                             BigDecimal salaireMin, BigDecimal salaireMax, boolean demandeEnCours) { }

    // ------------------------------------------------------------------ création

    @Transactional
    public DemandeContratDto creer(DemandeContratRequest req, SessionUser su) {
        if (req == null || req.idContrat() == null) throw new IllegalArgumentException("Le contrat concerné est obligatoire.");
        if (req.motifType() == null || !MOTIFS.contains(req.motifType()))
            throw new IllegalArgumentException("Motif inconnu : RENOUVELLEMENT, TRANSFORMATION ou AUGMENTATION.");
        Contrat c = contratRepo.findById(req.idContrat()).orElseThrow(() -> new IllegalArgumentException("Contrat introuvable."));
        if (!"SIGNE".equals(c.getStatutActuel()))
            throw new IllegalStateException("Seul un contrat signé peut faire l'objet d'une demande.");
        if (repo.existsByContratIdAndEtatIn(c.getId(), OUVERTES))
            throw new IllegalStateException("Une demande est déjà en cours sur ce contrat.");

        Employe emp = c.getAffectation().getEmploye();
        String origine;
        if (su.hasRole("RH")) origine = "RH";
        else if (su.hasRole("DG")) origine = "DG";
        else if (su.getIdEmploye() != null && su.getIdEmploye().equals(emp.getId())) origine = "EMPLOYE";
        else if (su.hasRole("Manager")
                && perimetre.unitesDemandables(su).contains(c.getAffectation().getPoste().getUniteOrga().getId())) origine = "MANAGER";
        else throw new AccesRefuseException("Vous ne pouvez faire une demande que pour votre propre contrat ou pour votre équipe.");

        DemandeContrat d = new DemandeContrat();
        d.setReference(genererReference());
        d.setContrat(c);
        d.setOrigine(origine);
        d.setMotifType(req.motifType());
        d.setIdDemandeur(su.getId());
        d.setMotif(req.motif() == null || req.motif().isBlank() ? null : req.motif().trim());
        if (AUGMENTATION.equals(req.motifType())) {
            d.setTypeContrat(null);
            d.setDateFinPrevue(null);
        } else {
            d.setTypeContrat(TRANSFORMATION.equals(req.motifType()) ? type(req.idTypeContrat()) : null);
            d.setDateFinPrevue(req.dateFinPrevue());
        }
        d.setDateEffet(req.dateEffet() != null ? req.dateEffet() : dateEffetParDefaut(c, req.motifType()));
        d.setSalaire(req.salaire() != null ? req.salaire() : contratService.salaireActuel(c.getId()));
        verifier(d);
        if (d.getDateEffet().isBefore(LocalDate.now()) && !"RH".equals(origine) && !"DG".equals(origine))
            throw new IllegalArgumentException("La date d'effet ne peut pas être dans le passé.");

        String nomEmp = nomEmploye(emp);
        if ("RH".equals(origine) || "DG".equals(origine)) {
            d.setIdRh(su.getId());
            d.setDateRh(LocalDateTime.now());
            d.setCommentaireRh("Proposition de la direction des ressources humaines.");
            d.setEtat(DemandeContrat.A_INSTRUIRE);
            repo.save(d);
            acheminer(d, nomEmp);
        } else {
            d.setEtat(DemandeContrat.A_INSTRUIRE);
            repo.save(d);
            notifications.notifierRole("RH", "DEMANDE_CONTRAT_A_INSTRUIRE",
                    "Demande « " + libelleMotif(d) + " » de " + nomUser(su.getId()) + " pour " + nomEmp + " : à instruire.",
                    "/contrats/demandes/" + d.getId());
        }
        return dto(d, su);
    }

    // ------------------------------------------------------------------ décisions

    /** Instruction par la RH d'une demande de l'employé ou du manager ; elle peut ajuster le salaire et les dates. */
    @Transactional
    public DemandeContratDto instruire(Integer id, DecisionContratRequest req, SessionUser su) {
        DemandeContrat d = trouver(id);
        exiger(d, DemandeContrat.A_INSTRUIRE, "à l'instruction de la RH");
        d.setIdRh(su.getId());
        d.setDateRh(LocalDateTime.now());
        d.setCommentaireRh(commentaire(req));
        String nomEmp = nomEmploye(d.getContrat().getAffectation().getEmploye());
        if (!accepte(req)) {
            refuser(d, "RH", nomEmp);
        } else {
            if (req.salaire() != null) d.setSalaire(req.salaire());
            if (req.dateFinPrevue() != null) d.setDateFinPrevue(req.dateFinPrevue());
            if (req.dateEffet() != null) d.setDateEffet(req.dateEffet());
            verifier(d);
            acheminer(d, nomEmp);
        }
        return dto(repo.save(d), su);
    }

    @Transactional
    public DemandeContratDto finance(Integer id, DecisionContratRequest req, SessionUser su) {
        DemandeContrat d = trouver(id);
        exiger(d, DemandeContrat.A_VALIDER_FINANCE, "à l'avis de la Finance");
        d.setIdFinance(su.getId());
        d.setDateFinance(LocalDateTime.now());
        d.setCommentaireFinance(commentaire(req));
        String nomEmp = nomEmploye(d.getContrat().getAffectation().getEmploye());
        if (!accepte(req)) refuser(d, "la Finance", nomEmp);
        else {
            d.setEtat(DemandeContrat.A_VALIDER_DG);
            notifications.notifierRole("DG", "DEMANDE_CONTRAT_A_VALIDER",
                    "Demande « " + libelleMotif(d) + " » pour " + nomEmp + " : avis favorable de la Finance, à valider.",
                    "/contrats/demandes/" + d.getId());
        }
        return dto(repo.save(d), su);
    }

    @Transactional
    public DemandeContratDto decider(Integer id, DecisionContratRequest req, SessionUser su) {
        DemandeContrat d = trouver(id);
        exiger(d, DemandeContrat.A_VALIDER_DG, "à la décision du DG");
        d.setIdDg(su.getId());
        d.setDateDg(LocalDateTime.now());
        d.setCommentaireDg(commentaire(req));
        Employe emp = d.getContrat().getAffectation().getEmploye();
        String nomEmp = nomEmploye(emp);
        if (!accepte(req)) {
            refuser(d, "le DG", nomEmp);
            return dto(repo.save(d), su);
        }
        verifier(d);   // la grille ou les règles du contrat ont pu changer depuis la demande
        Contrat resultat;
        if (AUGMENTATION.equals(d.getMotifType())) {
            resultat = contratService.avenantSalaire(d.getContrat().getId(), d.getDateEffet(), d.getSalaire());
        } else {
            var nouveau = contratService.renouveler(d.getContrat().getId(),
                    new RenouvellementRequest(d.getTypeContrat() == null ? null : d.getTypeContrat().getId(), d.getDateFinPrevue(), d.getMotif()),
                    d.getSalaire(), d.getDateEffet(), false);
            resultat = contratRepo.findById(nouveau.id()).orElseThrow();
        }
        d.setContratResultat(resultat);
        d.setEtat(DemandeContrat.VALIDEE);
        repo.save(d);
        String msg = "La demande « " + libelleMotif(d) + " » pour " + nomEmp + " a été validée par le DG"
                + (AUGMENTATION.equals(d.getMotifType()) ? " : le nouveau salaire s'applique au " + d.getDateEffet() + "."
                : " : le contrat " + resultat.getReference() + " est créé, à valider et signer.");
        notifications.notifierRole("RH", "DEMANDE_CONTRAT_DECISION", msg, "/contrats/demandes/" + d.getId());
        notifier(d, "DEMANDE_CONTRAT_DECISION", msg);
        return dto(d, su);
    }

    @Transactional
    public DemandeContratDto annuler(Integer id, SessionUser su) {
        DemandeContrat d = trouver(id);
        if (!OUVERTES.contains(d.getEtat())) throw new IllegalStateException("Cette demande n'est plus en cours.");
        if (!d.getIdDemandeur().equals(su.getId()) && !su.hasRole("RH"))
            throw new AccesRefuseException("Seul le demandeur ou la RH peut annuler la demande.");
        d.setEtat(DemandeContrat.ANNULEE);
        repo.save(d);
        return dto(d, su);
    }

    // ------------------------------------------------------------------ règles

    /** Contrôle les règles du contrat (simulation), la grille et le sens de l'évolution du salaire. */
    private void verifier(DemandeContrat d) {
        Contrat c = d.getContrat();
        if (d.getSalaire() == null || d.getSalaire().signum() <= 0) throw new IllegalArgumentException("Le salaire mensuel est obligatoire.");
        if (AUGMENTATION.equals(d.getMotifType())) {
            if (d.getSalaire().compareTo(contratService.salaireActuel(c.getId())) <= 0)
                throw new IllegalArgumentException("Une augmentation doit dépasser le salaire actuel ("
                        + String.format(java.util.Locale.FRANCE, "%,.0f", contratService.salaireActuel(c.getId())) + " Ar).");
            if (d.getDateEffet().isBefore(c.getDateDebut()))
                throw new IllegalArgumentException("La date d'effet ne peut pas précéder le début du contrat.");
            contratService.verifierGrille(c, d.getSalaire(), d.getDateEffet());
        } else {
            if (TRANSFORMATION.equals(d.getMotifType()) && (d.getTypeContrat() == null || d.getTypeContrat().getId().equals(c.getType().getId())))
                throw new IllegalArgumentException("Choisissez un type de contrat différent du type actuel (" + c.getType().getLibelle() + ").");
            contratService.renouveler(c.getId(),
                    new RenouvellementRequest(d.getTypeContrat() == null ? null : d.getTypeContrat().getId(), d.getDateFinPrevue(), d.getMotif()),
                    d.getSalaire(), d.getDateEffet(), true);
        }
    }

    /** Surcoût annuel du salaire demandé par rapport au salaire actuel (0 s'il n'augmente pas). */
    private BigDecimal surcoutAnnuel(DemandeContrat d) {
        BigDecimal delta = d.getSalaire().subtract(contratService.salaireActuel(d.getContrat().getId()));
        return delta.signum() > 0 ? delta.multiply(BigDecimal.valueOf(12)) : BigDecimal.ZERO;
    }

    private BudgetService.Engagement engagement(DemandeContrat d) {
        Integer idUnite = d.getContrat().getAffectation().getPoste().getUniteOrga().getId();
        return budgetService.engagement(idUnite, d.getDateEffet().getYear(), null);
    }

    /** Passe à l'étape suivante : Finance si le surcoût dépasse le disponible, sinon DG. */
    private void acheminer(DemandeContrat d, String nomEmp) {
        BigDecimal surcout = surcoutAnnuel(d);
        BigDecimal dispo = engagement(d).disponible();
        boolean depasse = surcout.signum() > 0 && dispo != null && surcout.compareTo(dispo) > 0;
        d.setDepassementBudget(depasse);
        if (depasse) {
            d.setEtat(DemandeContrat.A_VALIDER_FINANCE);
            notifications.notifierRole("Finance", "DEMANDE_CONTRAT_FINANCE",
                    "Demande « " + libelleMotif(d) + " » pour " + nomEmp + " : le surcoût annuel dépasse le disponible du budget, avis demandé.",
                    "/contrats/demandes/" + d.getId());
        } else {
            d.setEtat(DemandeContrat.A_VALIDER_DG);
            notifications.notifierRole("DG", "DEMANDE_CONTRAT_A_VALIDER",
                    "Demande « " + libelleMotif(d) + " » pour " + nomEmp + " : à valider ou refuser.",
                    "/contrats/demandes/" + d.getId());
        }
        if (!"RH".equals(d.getOrigine()) && !"DG".equals(d.getOrigine()))
            notifier(d, "DEMANDE_CONTRAT_AVANCEE", "Votre demande « " + libelleMotif(d) + " » a été instruite par la RH et transmise pour validation.");
    }

    private void refuser(DemandeContrat d, String par, String nomEmp) {
        d.setEtat(DemandeContrat.REFUSEE);
        String msg = "La demande « " + libelleMotif(d) + " » pour " + nomEmp + " a été refusée par " + par
                + (dernierCommentaire(d) == null ? "." : " : " + dernierCommentaire(d));
        notifier(d, "DEMANDE_CONTRAT_DECISION", msg);
        if (!"RH".equals(par)) notifications.notifierRole("RH", "DEMANDE_CONTRAT_DECISION", msg, "/contrats/demandes/" + d.getId());
    }

    private String dernierCommentaire(DemandeContrat d) {
        if (d.getCommentaireDg() != null) return d.getCommentaireDg();
        if (d.getCommentaireFinance() != null) return d.getCommentaireFinance();
        return d.getCommentaireRh();
    }

    /** Prévient le demandeur et l'employé concerné. */
    private void notifier(DemandeContrat d, String type, String msg) {
        String lien = "/contrats/demandes/" + d.getId();
        java.util.LinkedHashSet<Integer> dest = new java.util.LinkedHashSet<>();
        dest.add(d.getIdDemandeur());
        userRepo.findFirstByIdEmploye(d.getContrat().getAffectation().getEmploye().getId()).ifPresent(u -> dest.add(u.getId()));
        notifications.notifier(dest, type, msg, lien);
    }

    // ------------------------------------------------------------------ outils

    private LocalDate dateEffetParDefaut(Contrat c, String motif) {
        if (AUGMENTATION.equals(motif)) return LocalDate.now();
        return c.getDateFinPrevue() != null && c.getDateFinPrevue().isAfter(LocalDate.now()) ? c.getDateFinPrevue().plusDays(1) : LocalDate.now();
    }

    private TypeContrat type(Integer id) {
        if (id == null) throw new IllegalArgumentException("Le nouveau type de contrat est obligatoire pour une transformation.");
        return typeRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable."));
    }

    private void exiger(DemandeContrat d, String etat, String libelle) {
        if (!etat.equals(d.getEtat()))
            throw new IllegalStateException("Cette demande n'est pas " + libelle + " (état : " + d.getEtat() + ").");
    }

    private static boolean accepte(DecisionContratRequest req) {
        if (req == null || req.accepter() == null) throw new IllegalArgumentException("La décision (accepter ou refuser) est obligatoire.");
        if (!req.accepter() && (req.commentaire() == null || req.commentaire().isBlank()))
            throw new IllegalArgumentException("Un motif est obligatoire pour refuser.");
        return req.accepter();
    }

    private static String commentaire(DecisionContratRequest req) {
        return req == null || req.commentaire() == null || req.commentaire().isBlank() ? null : req.commentaire().trim();
    }

    private DemandeContrat trouver(Integer id) {
        return repo.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Demande introuvable : " + id));
    }

    private String genererReference() {
        String prefix = "DCT-" + LocalDate.now().getYear() + "-";
        long n = repo.countByReferenceStartingWith(prefix) + 1;
        String ref = prefix + String.format("%04d", n);
        while (repo.existsByReference(ref)) { n++; ref = prefix + String.format("%04d", n); }
        return ref;
    }

    private static String nomEmploye(Employe e) {
        return e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms());
    }

    private String nomUser(Integer id) {
        if (id == null) return null;
        return userRepo.findById(id).map(u -> u.getNomComplet() != null ? u.getNomComplet() : u.getLogin()).orElse("?");
    }

    private static String libelleMotif(DemandeContrat d) {
        return switch (d.getMotifType()) {
            case RENOUVELLEMENT -> "renouvellement de contrat";
            case TRANSFORMATION -> "transformation de contrat";
            default -> "augmentation de salaire";
        };
    }

    private DemandeContratDto dto(DemandeContrat d, SessionUser su) {
        Contrat c = d.getContrat();
        Employe emp = c.getAffectation().getEmploye();
        var fourchette = contratService.fourchette(c, d.getDateEffet());
        BigDecimal dispo = null;
        try { dispo = engagement(d).disponible(); } catch (RuntimeException e) { /* budget facultatif */ }
        List<DemandeContratDto.Etape> etapes = new ArrayList<>();
        etapes.add(new DemandeContratDto.Etape("Demande (" + d.getOrigine() + ")", nomUser(d.getIdDemandeur()), d.getDateDemande(), d.getMotif()));
        if (d.getIdRh() != null && !d.getIdRh().equals(d.getIdDemandeur()))
            etapes.add(new DemandeContratDto.Etape("Instruction RH", nomUser(d.getIdRh()), d.getDateRh(), d.getCommentaireRh()));
        if (d.getIdFinance() != null) etapes.add(new DemandeContratDto.Etape("Avis Finance", nomUser(d.getIdFinance()), d.getDateFinance(), d.getCommentaireFinance()));
        if (d.getIdDg() != null) etapes.add(new DemandeContratDto.Etape("Décision DG", nomUser(d.getIdDg()), d.getDateDg(), d.getCommentaireDg()));
        boolean ouverte = OUVERTES.contains(d.getEtat());
        return new DemandeContratDto(d.getId(), d.getReference(), d.getEtat(), d.getOrigine(), d.getMotifType(),
                emp.getId(), nomEmploye(emp), emp.getMatricule(), c.getAffectation().getPoste().getLibelle(),
                c.getAffectation().getPoste().getUniteOrga().getLibelle(),
                c.getId(), c.getReference(), c.getType().getLibelle(), c.getDateDebut(), c.getDateFinPrevue(),
                contratService.salaireActuel(c.getId()),
                d.getTypeContrat() != null ? d.getTypeContrat().getLibelle() : c.getType().getLibelle(),
                d.getDateEffet(), d.getDateFinPrevue(), d.getSalaire(), d.getMotif(),
                nomUser(d.getIdDemandeur()), d.getDateDemande(),
                d.isDepassementBudget(), surcoutAnnuel(d), dispo,
                fourchette == null ? null : fourchette.min(), fourchette == null ? null : fourchette.max(),
                etapes, d.getContratResultat() == null ? null : d.getContratResultat().getId(),
                d.getContratResultat() == null ? null : d.getContratResultat().getReference(),
                su.hasRole("RH") && DemandeContrat.A_INSTRUIRE.equals(d.getEtat()),
                su.hasRole("Finance") && DemandeContrat.A_VALIDER_FINANCE.equals(d.getEtat()),
                su.hasRole("DG") && DemandeContrat.A_VALIDER_DG.equals(d.getEtat()),
                ouverte && (d.getIdDemandeur().equals(su.getId()) || su.hasRole("RH")));
    }
}
