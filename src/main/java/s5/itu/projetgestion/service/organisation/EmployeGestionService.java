package s5.itu.projetgestion.service.organisation;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.organisation.AffectationDto;
import s5.itu.projetgestion.dto.organisation.CoordonneesBancairesDto;
import s5.itu.projetgestion.dto.organisation.EmployeDossierDto;
import s5.itu.projetgestion.dto.organisation.EmployeRequest;
import s5.itu.projetgestion.dto.organisation.EmployeResumeDto;
import s5.itu.projetgestion.dto.organisation.MouvementRequest;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.CoordonneesBancaires;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.recrutement.Personne;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.CoordonneesBancairesRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.recrutement.PersonneRepository;
import s5.itu.projetgestion.service.onboarding.OnboardingService;
import s5.itu.projetgestion.service.recrutement.EmployeService;
import s5.itu.projetgestion.service.service.ContratService;

/**
 * Gestion du personnel : saisie d'un employé déjà en poste (reprise), identité, mutation, départ,
 * coordonnées bancaires. Les embauches issues du recrutement passent par EmployeService.embaucher.
 */
@Service
@Transactional
public class EmployeGestionService {

    private static final Logger log = LoggerFactory.getLogger(EmployeGestionService.class);
    public static final Set<String> MOTIFS_DEPART = Set.of("DEMISSION", "LICENCIEMENT", "FIN_CONTRAT", "RETRAITE", "AUTRE");

    private final EmployeRepository employeRepo;
    private final PersonneRepository personneRepo;
    private final AffectationRepository affectationRepo;
    private final PosteRepository posteRepo;
    private final ContratRepository contratRepo;
    private final CoordonneesBancairesRepository banqueRepo;
    private final UserRepository userRepo;
    private final EmployeService employeService;
    private final ContratService contratService;
    private final EmployeFicheService ficheService;
    private final OnboardingService onboardingService;

    public EmployeGestionService(EmployeRepository employeRepo, PersonneRepository personneRepo,
                                 AffectationRepository affectationRepo, PosteRepository posteRepo, ContratRepository contratRepo,
                                 CoordonneesBancairesRepository banqueRepo, UserRepository userRepo,
                                 EmployeService employeService, ContratService contratService,
                                 EmployeFicheService ficheService, OnboardingService onboardingService) {
        this.employeRepo = employeRepo;
        this.personneRepo = personneRepo;
        this.affectationRepo = affectationRepo;
        this.posteRepo = posteRepo;
        this.contratRepo = contratRepo;
        this.banqueRepo = banqueRepo;
        this.userRepo = userRepo;
        this.employeService = employeService;
        this.contratService = contratService;
        this.ficheService = ficheService;
        this.onboardingService = onboardingService;
    }

    // ------------------------------------------------------------------ consultation

    /** Tout le personnel, y compris les employés partis (statut ACTIF / PARTI). */
    @Transactional(readOnly = true)
    public List<EmployeResumeDto> listerTous(String recherche) {
        String q = recherche == null ? "" : recherche.trim().toLowerCase(Locale.ROOT);
        return affectationRepo.findDernieresAffectations(q).stream()
                .map(a -> new EmployeResumeDto(a.getEmploye().getId(), a.getEmploye().getMatricule(),
                        a.getEmploye().getNom(), a.getEmploye().getPrenoms(), a.getPoste().getLibelle(),
                        a.getPoste().getUniteOrga().getLibelle(), a.getDateDebut()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeDossierDto dossier(Integer id) {
        var fiche = ficheService.fiche(id);
        List<AffectationDto> affectations = affectationRepo.findHistorique(id).stream().map(EmployeGestionService::toDto).toList();
        boolean enPoste = affectations.stream().anyMatch(AffectationDto::enCours);
        String compte = userRepo.findFirstByIdEmploye(id).map(u -> u.getLogin() + " (" + u.getStatut() + ")").orElse(null);
        return new EmployeDossierDto(fiche, enPoste ? "ACTIF" : "PARTI", affectations,
                onboardingService.parEmploye(id), banque(id), compte);
    }

    // ------------------------------------------------------------------ création / identité

    /** Saisie d'un employé déjà présent dans l'entreprise (reprise de l'existant) : identité + affectation sur un poste libre. */
    public EmployeDossierDto creer(EmployeRequest req) {
        if (req == null) throw new IllegalArgumentException("Formulaire manquant.");
        if (req.idPoste() == null) throw new IllegalArgumentException("Le poste est obligatoire.");
        Poste poste = posteLibre(req.idPoste());
        Personne p = new Personne();
        appliquer(p, req);
        personneRepo.save(p);
        Employe e = new Employe();
        e.setMatricule(employeService.genererMatricule());
        e.setPersonne(p);
        copier(e, p);
        employeRepo.save(e);
        Affectation a = affecter(e, poste, req.dateEntree() == null ? LocalDate.now() : req.dateEntree());
        contratService.creerReprise(a, req.idTypeContrat(), req.dateFinPrevue(), req.salaire());   // contrat en cours et salaire
        return dossier(e.getId());
    }

    public EmployeDossierDto modifier(Integer id, EmployeRequest req) {
        Employe e = trouver(id);
        Personne p = e.getPersonne();
        if (p == null) {
            p = new Personne();
            e.setPersonne(p);
        }
        appliquer(p, req);
        personneRepo.save(p);
        copier(e, p);
        return dossier(id);
    }

    // ------------------------------------------------------------------ mouvements

    /** Mutation : l'affectation en cours se termine la veille, une nouvelle commence à la date donnée sur le nouveau poste. */
    public EmployeDossierDto muter(Integer id, MouvementRequest req) {
        Employe e = trouver(id);
        if (req == null || req.idPoste() == null) throw new IllegalArgumentException("Le nouveau poste est obligatoire.");
        LocalDate date = req.date() == null ? LocalDate.now() : req.date();
        Affectation actuelle = affectationEnCours(e);
        if (actuelle.getPoste().getId().equals(req.idPoste())) throw new IllegalArgumentException("L'employé occupe déjà ce poste.");
        if (!date.isAfter(actuelle.getDateDebut())) throw new IllegalArgumentException("La date de mutation doit suivre le début de l'affectation actuelle.");
        Poste nouveau = posteLibre(req.idPoste());
        actuelle.setDateFin(date.minusDays(1));
        if (!date.isAfter(LocalDate.now())) actuelle.getPoste().setStatutOccupation("LIBRE");
        Affectation a = affecter(e, nouveau, date);
        // le contrat en cours suit l'employé sur sa nouvelle affectation
        for (Contrat c : contratRepo.findByAffectationIdAndStatutActuelOrderByDateDebutDesc(actuelle.getId(), "SIGNE")) c.setAffectation(a);
        for (Contrat c : contratRepo.findByAffectationIdAndStatutActuelOrderByDateDebutDesc(actuelle.getId(), "VALIDE")) c.setAffectation(a);
        for (Contrat c : contratRepo.findByAffectationIdAndStatutActuelOrderByDateDebutDesc(actuelle.getId(), "CREE")) c.setAffectation(a);
        return dossier(id);
    }

    /**
     * Départ : fin de l'affectation et des contrats en cours (motif enregistré). Le poste reste OCCUPE jusqu'à la date
     * de départ (le remplaçant peut être recruté entre-temps), puis il est libéré chaque nuit.
     */
    public EmployeDossierDto depart(Integer id, MouvementRequest req) {
        Employe e = trouver(id);
        if (req == null || req.date() == null) throw new IllegalArgumentException("La date de départ est obligatoire.");
        String motif = req.motif() == null ? "" : req.motif().trim().toUpperCase(Locale.ROOT);
        if (!MOTIFS_DEPART.contains(motif)) throw new IllegalArgumentException("Motif de départ inconnu : " + req.motif());
        Affectation a = affectationEnCours(e);
        if (req.date().isBefore(a.getDateDebut())) throw new IllegalArgumentException("La date de départ précède l'entrée sur le poste.");
        a.setDateFin(req.date());
        String texte = motif + (req.commentaire() == null || req.commentaire().isBlank() ? "" : " : " + req.commentaire().trim());
        for (Contrat c : contratRepo.findByAffectationEmployeIdOrderByDateDebutDesc(id)) {
            if (Set.of("SIGNE", "VALIDE", "CREE").contains(c.getStatutActuel())) contratService.terminerPourDepart(c.getId(), req.date(), texte);
        }
        if (req.date().isBefore(LocalDate.now())) a.getPoste().setStatutOccupation("LIBRE");
        userRepo.findFirstByIdEmploye(id).ifPresent(u -> { if (!req.date().isAfter(LocalDate.now())) u.setStatut("DESACTIVE"); });
        return dossier(id);
    }

    /** Chaque nuit : libère les postes dont l'occupant est parti. */
    @Scheduled(cron = "0 15 0 * * *")
    public void libererPostes() {
        List<Affectation> l = affectationRepo.findTermineesAvecPosteOccupe();
        l.forEach(a -> a.getPoste().setStatutOccupation("LIBRE"));
        if (!l.isEmpty()) log.info("{} poste(s) libéré(s) après départ", l.size());
    }

    // ------------------------------------------------------------------ coordonnées bancaires

    @Transactional(readOnly = true)
    public List<CoordonneesBancairesDto> banque(Integer idEmploye) {
        return banqueRepo.findByEmployeIdOrderByDateEffetDescIdDesc(idEmploye).stream()
                .map(b -> new CoordonneesBancairesDto(b.getId(), b.getBanque(), b.getAgence(), b.getTitulaire(), b.getRib(), b.getDateEffet(), b.isActif()))
                .toList();
    }

    /** Nouvelles coordonnées : remplacent les précédentes (gardées dans l'historique) ; la tâche d'onboarding BANQUE est faite. */
    public List<CoordonneesBancairesDto> enregistrerBanque(Integer idEmploye, CoordonneesBancairesDto req, Integer idUser) {
        Employe e = trouver(idEmploye);
        if (req == null || vide(req.banque()) || vide(req.titulaire()) || vide(req.rib()))
            throw new IllegalArgumentException("La banque, le titulaire et le RIB sont obligatoires.");
        String rib = req.rib().replaceAll("\\s+", "");
        if (!rib.matches("[A-Za-z0-9]{10,34}")) throw new IllegalArgumentException("RIB / IBAN invalide (10 à 34 caractères, lettres et chiffres).");
        banqueRepo.findByEmployeIdOrderByDateEffetDescIdDesc(idEmploye).forEach(b -> b.setActif(false));
        CoordonneesBancaires b = new CoordonneesBancaires();
        b.setEmploye(e);
        b.setBanque(req.banque().trim());
        b.setAgence(vide(req.agence()) ? null : req.agence().trim());
        b.setTitulaire(req.titulaire().trim());
        b.setRib(rib.toUpperCase(Locale.ROOT));
        b.setDateEffet(req.dateEffet() == null ? LocalDate.now() : req.dateEffet());
        b.setActif(true);
        banqueRepo.save(b);
        onboardingService.tacheRealiseeAutomatiquement(idEmploye, "BANQUE", idUser, "Coordonnées bancaires enregistrées");
        return banque(idEmploye);
    }

    // ------------------------------------------------------------------ outils

    private Affectation affecter(Employe e, Poste poste, LocalDate debut) {
        Affectation a = new Affectation();
        a.setEmploye(e);
        a.setPoste(poste);
        a.setDateDebut(debut);
        affectationRepo.save(a);
        poste.setStatutOccupation("OCCUPE");
        return a;
    }

    private Affectation affectationEnCours(Employe e) {
        return affectationRepo.findEnCoursParEmploye(e.getId()).stream().filter(a -> a.getDateFin() == null).findFirst()
                .orElseThrow(() -> new IllegalStateException("Cet employé n'a pas d'affectation en cours."));
    }

    private Poste posteLibre(Integer idPoste) {
        Poste p = posteRepo.findById(idPoste).orElseThrow(() -> new IllegalArgumentException("Poste introuvable."));
        if (!"ACTIF".equals(p.getEtat())) throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'est pas ACTIF.");
        if (!"LIBRE".equals(p.getStatutOccupation())) throw new IllegalArgumentException("Le poste " + p.getLibelle() + " est déjà occupé.");
        return p;
    }

    private void appliquer(Personne p, EmployeRequest req) {
        if (req == null || vide(req.nom())) throw new IllegalArgumentException("Le nom est obligatoire.");
        if (req.email() != null && !req.email().isBlank() && !req.email().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Adresse email invalide.");
        if (req.dateNaissance() != null && req.dateNaissance().isAfter(LocalDate.now().minusYears(16)))
            throw new IllegalArgumentException("Date de naissance invalide (16 ans minimum).");
        if (!vide(req.cin())) {
            personneRepo.findByCin(req.cin().trim()).filter(x -> !x.getId().equals(p.getId()))
                    .ifPresent(x -> { throw new IllegalArgumentException("Ce numéro de CIN est déjà enregistré."); });
        }
        p.setNom(req.nom().trim().toUpperCase(Locale.ROOT));
        p.setPrenoms(vide(req.prenoms()) ? null : req.prenoms().trim());
        p.setDateNaissance(req.dateNaissance());
        p.setAdresse(vide(req.adresse()) ? null : req.adresse().trim());
        p.setTelephone(vide(req.telephone()) ? null : req.telephone().trim());
        p.setEmail(vide(req.email()) ? null : req.email().trim());
        p.setCin(vide(req.cin()) ? null : req.cin().trim());
    }

    private static void copier(Employe e, Personne p) {
        e.setNom(p.getNom());
        e.setPrenoms(p.getPrenoms());
        e.setAdresse(p.getAdresse());
    }

    private static AffectationDto toDto(Affectation a) {
        boolean enCours = a.getDateFin() == null || !a.getDateFin().isBefore(LocalDate.now());
        return new AffectationDto(a.getId(), a.getPoste().getId(), a.getPoste().getCode(), a.getPoste().getLibelle(),
                a.getPoste().getUniteOrga().getLibelle(), a.getDateDebut(), a.getDateFin(), enCours);
    }

    private Employe trouver(Integer id) {
        return employeRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employé introuvable : " + id));
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
