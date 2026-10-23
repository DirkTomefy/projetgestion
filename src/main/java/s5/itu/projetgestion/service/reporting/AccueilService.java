package s5.itu.projetgestion.service.reporting;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.reporting.AccueilDto;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.recrutement.DelegationRepository;
import s5.itu.projetgestion.service.besoin.BesoinSuiviService;
import s5.itu.projetgestion.service.notification.NotificationService;
import s5.itu.projetgestion.service.onboarding.OnboardingService;
import s5.itu.projetgestion.service.recrutement.EvaluationService;

/** Résumé personnel affiché sur la page d'accueil. */
@Service
@Transactional(readOnly = true)
public class AccueilService {

    private final UserRepository userRepo;
    private final AffectationRepository affectationRepo;
    private final DelegationRepository delegationRepo;
    private final BesoinSuiviService besoinSuivi;
    private final EvaluationService evaluationService;
    private final OnboardingService onboardingService;
    private final NotificationService notificationService;
    private final s5.itu.projetgestion.repository.organisation.PosteRepository posteRepo;
    private final s5.itu.projetgestion.service.service.DemandeContratService demandeContrat;
    private final s5.itu.projetgestion.service.finance.BudgetService budgetService;
    private final s5.itu.projetgestion.service.finance.GrilleSalarialeService grilleService;

    public AccueilService(UserRepository userRepo, AffectationRepository affectationRepo, DelegationRepository delegationRepo,
                          BesoinSuiviService besoinSuivi, EvaluationService evaluationService,
                          OnboardingService onboardingService, NotificationService notificationService,
                          s5.itu.projetgestion.service.finance.BudgetService budgetService,
                          s5.itu.projetgestion.service.finance.GrilleSalarialeService grilleService,
                          s5.itu.projetgestion.repository.organisation.PosteRepository posteRepo,
                          s5.itu.projetgestion.service.service.DemandeContratService demandeContrat) {
        this.posteRepo = posteRepo;
        this.demandeContrat = demandeContrat;
        this.budgetService = budgetService;
        this.grilleService = grilleService;
        this.userRepo = userRepo;
        this.affectationRepo = affectationRepo;
        this.delegationRepo = delegationRepo;
        this.besoinSuivi = besoinSuivi;
        this.evaluationService = evaluationService;
        this.onboardingService = onboardingService;
        this.notificationService = notificationService;
    }

    public AccueilDto accueil(SessionUser su) {
        User u = userRepo.findById(su.getId()).orElseThrow();
        var a = u.getIdEmploye() == null ? null
                : affectationRepo.findEnCoursParEmploye(u.getIdEmploye()).stream().findFirst().orElse(null);
        long aValider = besoinSuivi.lister(su, "a-valider", null, null).size();
        long mesBesoins = besoinSuivi.lister(su, "mes-demandes", null, null).stream()
                .filter(b -> "EN_ATTENTE".equals(b.etat()) || "OUVERT".equals(b.etat())).count();
        long evaluations;
        try {
            evaluations = evaluationService.mesEvaluations(su).evaluations().size();
        } catch (RuntimeException e) {
            evaluations = 0;
        }
        LocalDate j = LocalDate.now();
        List<String> delegations = delegationRepo.findByUserDelegueAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(u, j, j).stream()
                .map(d -> (d.getUserDelegant().getNomComplet() == null ? d.getUserDelegant().getLogin() : d.getUserDelegant().getNomComplet())
                        + " jusqu'au " + d.getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .toList();
        long inscriptions = su.hasRole("ADMIN") ? userRepo.countByStatut("EN_ATTENTE") : 0;
        return new AccueilDto(u.getNomComplet() != null ? u.getNomComplet() : u.getLogin(), su.getRoles(),
                a == null ? null : a.getPoste().getLibelle(), a == null ? null : a.getPoste().getUniteOrga().getLibelle(),
                aValider, evaluations, onboardingService.mesTaches(su).size(), notificationService.compterNonLues(su.getId()),
                mesBesoins, inscriptions, delegations,
                su.hasRole("DG") ? budgetService.aValider() : 0, su.hasRole("DG") ? grilleService.aValider() : 0,
                su.hasRole("DG") ? posteRepo.countProposesAValider() : 0,
                demandeContrat.aTraiter(su));
    }
}
