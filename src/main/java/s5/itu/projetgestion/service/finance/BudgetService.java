package s5.itu.projetgestion.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.finance.BudgetDto;
import s5.itu.projetgestion.dto.finance.BudgetRequest;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.finance.BudgetUnite;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.finance.BudgetUniteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;
import s5.itu.projetgestion.repository.reporting.IndicateurRepository;
import s5.itu.projetgestion.service.notification.NotificationService;

/**
 * Budgets annuels des unités : la Finance propose, le DG valide (ou saisit directement).
 * Seul le montant validé sert au contrôle budgétaire des besoins.
 */
@Service
@Transactional
public class BudgetService {

    private static final BigDecimal DOUZE = BigDecimal.valueOf(12);

    private final BudgetUniteRepository budgetRepo;
    private final UniteOrgaRepository uniteRepo;
    private final IndicateurRepository indicateurs;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public BudgetService(BudgetUniteRepository budgetRepo, UniteOrgaRepository uniteRepo, IndicateurRepository indicateurs,
                         UserRepository userRepo, NotificationService notificationService) {
        this.budgetRepo = budgetRepo;
        this.uniteRepo = uniteRepo;
        this.indicateurs = indicateurs;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<BudgetDto> lister(Integer exercice) {
        int annee = exercice == null ? LocalDate.now().getYear() : exercice;
        Map<Integer, IndicateurRepository.LigneUnite> parUnite = indicateurs.parUnite(annee).stream()
                .collect(Collectors.toMap(IndicateurRepository.LigneUnite::getIdUnite, Function.identity()));
        return budgetRepo.rechercher(annee).stream().map(b -> toDto(b, parUnite.get(b.getUniteOrga().getId()))).toList();
    }

    /** Budget validé de l'unité pour l'exercice, s'il existe. */
    @Transactional(readOnly = true)
    public BigDecimal budget(Integer idUnite, int exercice) {
        return budgetRepo.findByUniteOrgaIdAndExercice(idUnite, exercice)
                .map(BudgetUnite::getMontant).map(BigDecimal::valueOf).orElse(null);
    }

    /**
     * Engagements d'une unité pour un exercice : budget validé, masse salariale annuelle des contrats en cours
     * et coût des besoins en cours (validation ou recrutement), sans le besoin examiné (coutExclu).
     */
    @Transactional(readOnly = true)
    public Engagement engagement(Integer idUnite, int exercice, BigDecimal coutExclu) {
        BigDecimal budget = budget(idUnite, exercice);
        IndicateurRepository.LigneUnite l = indicateurs.parUnite(exercice).stream()
                .filter(x -> x.getIdUnite().equals(idUnite)).findFirst().orElse(null);
        BigDecimal masse = l == null ? BigDecimal.ZERO : l.getMasseMensuelle().multiply(DOUZE);
        BigDecimal besoins = l == null ? BigDecimal.ZERO : l.getBesoinsEnCours();
        if (coutExclu != null) besoins = besoins.subtract(coutExclu).max(BigDecimal.ZERO);
        BigDecimal disponible = budget == null ? null : budget.subtract(masse).subtract(besoins);
        return new Engagement(budget, masse, besoins, disponible);
    }

    public record Engagement(BigDecimal budget, BigDecimal masseSalariale, BigDecimal autresBesoins, BigDecimal disponible) { }

    @Transactional(readOnly = true)
    public long aValider() {
        return budgetRepo.countByStatut(BudgetUnite.A_VALIDER);
    }

    /**
     * Saisie d'un budget. Par le DG : montant validé tout de suite. Par la Finance (ou l'administrateur) :
     * montant proposé, à valider par le DG ; un montant déjà validé reste appliqué en attendant.
     */
    public BudgetDto enregistrer(Integer id, BudgetRequest req, SessionUser su) {
        if (req == null || req.idUnite() == null || req.exercice() == null || req.montant() == null)
            throw new IllegalArgumentException("L'unité, l'exercice et le montant sont obligatoires.");
        if (req.montant().signum() < 0) throw new IllegalArgumentException("Le montant ne peut pas être négatif.");
        if (req.exercice() < 2000 || req.exercice() > 2100) throw new IllegalArgumentException("Exercice invalide.");
        BudgetUnite b = id == null ? new BudgetUnite() : trouver(id);
        budgetRepo.findByUniteOrgaIdAndExercice(req.idUnite(), req.exercice())
                .filter(x -> !x.getId().equals(b.getId()))
                .ifPresent(x -> { throw new IllegalArgumentException("Cette unité a déjà un budget pour l'exercice " + req.exercice() + "."); });
        if (b.getId() != null && b.getMontant() != null
                && (!b.getUniteOrga().getId().equals(req.idUnite()) || !b.getExercice().equals(req.exercice())))
            throw new IllegalArgumentException("Un budget validé ne change ni d'unité ni d'exercice : saisissez un nouveau budget.");
        b.setUniteOrga(uniteRepo.findById(req.idUnite()).orElseThrow(() -> new IllegalArgumentException("Unité introuvable.")));
        b.setExercice(req.exercice());
        User auteur = userRepo.getReferenceById(su.getId());
        b.setProposePar(auteur);
        b.setCommentaire(null);
        if (su.hasRole("DG")) {
            b.setMontant(req.montant().doubleValue());
            b.setMontantPropose(null);
            decider(b, BudgetUnite.VALIDE, auteur, null);
        } else {
            b.setMontantPropose(req.montant().doubleValue());
            b.setStatut(BudgetUnite.A_VALIDER);
        }
        budgetRepo.save(b);
        if (BudgetUnite.A_VALIDER.equals(b.getStatut())) {
            notificationService.notifierRole("DG", "BUDGET_A_VALIDER", "Budget " + b.getExercice() + " de " + b.getUniteOrga().getLibelle()
                    + " proposé : " + ariary(req.montant()) + " — à valider", "/finance/budgets?exercice=" + b.getExercice());
        }
        return dto(b);
    }

    public BudgetDto valider(Integer id, SessionUser su) {
        BudgetUnite b = trouver(id);
        if (!BudgetUnite.A_VALIDER.equals(b.getStatut())) throw new IllegalStateException("Ce budget n'attend pas de validation.");
        b.setMontant(b.getMontantPropose());
        b.setMontantPropose(null);
        decider(b, BudgetUnite.VALIDE, userRepo.getReferenceById(su.getId()), null);
        notifierFinance(b, "validé : " + ariary(BigDecimal.valueOf(b.getMontant())));
        return dto(b);
    }

    public BudgetDto refuser(Integer id, String commentaire, SessionUser su) {
        BudgetUnite b = trouver(id);
        if (!BudgetUnite.A_VALIDER.equals(b.getStatut())) throw new IllegalStateException("Ce budget n'attend pas de validation.");
        if (commentaire == null || commentaire.isBlank()) throw new IllegalArgumentException("Le motif du refus est obligatoire.");
        b.setMontantPropose(null);
        // un montant validé auparavant reste appliqué ; sinon le budget est refusé
        decider(b, b.getMontant() != null ? BudgetUnite.VALIDE : BudgetUnite.REFUSE, userRepo.getReferenceById(su.getId()), commentaire.trim());
        notifierFinance(b, "refusé : " + commentaire.trim());
        return dto(b);
    }

    /** Un budget jamais validé peut être supprimé par la Finance ; un budget validé seulement par le DG. */
    public void supprimer(Integer id, SessionUser su) {
        BudgetUnite b = trouver(id);
        if (b.getMontant() != null && !su.hasRole("DG"))
            throw new AccesRefuseException("Seul le DG peut supprimer un budget validé.");
        budgetRepo.delete(b);
    }

    private void decider(BudgetUnite b, String statut, User decideur, String commentaire) {
        b.setStatut(statut);
        b.setDecidePar(decideur);
        b.setDateDecision(LocalDateTime.now());
        b.setCommentaire(commentaire);
    }

    private void notifierFinance(BudgetUnite b, String quoi) {
        notificationService.notifierRole("Finance", "BUDGET_DECISION", "Budget " + b.getExercice() + " de "
                + b.getUniteOrga().getLibelle() + " " + quoi, "/finance/budgets?exercice=" + b.getExercice());
    }

    private BudgetDto dto(BudgetUnite b) {
        budgetRepo.flush();
        return lister(b.getExercice()).stream().filter(x -> x.id().equals(b.getId())).findFirst().orElseThrow();
    }

    private BudgetUnite trouver(Integer id) {
        return budgetRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget introuvable : " + id));
    }

    private static String ariary(BigDecimal v) {
        return String.format(Locale.FRANCE, "%,.0f Ar", v);
    }

    private static BudgetDto toDto(BudgetUnite b, IndicateurRepository.LigneUnite l) {
        BigDecimal montant = b.getMontant() == null ? null : BigDecimal.valueOf(b.getMontant());
        BigDecimal masse = l == null ? BigDecimal.ZERO : l.getMasseMensuelle().multiply(DOUZE);
        BigDecimal besoins = l == null ? BigDecimal.ZERO : l.getBesoinsEnCours();
        return new BudgetDto(b.getId(), b.getUniteOrga().getId(), b.getUniteOrga().getLibelle(), b.getExercice(), montant,
                masse, besoins, montant == null ? null : montant.subtract(masse).subtract(besoins),
                b.getMontantPropose() == null ? null : BigDecimal.valueOf(b.getMontantPropose()), b.getStatut(),
                nom(b.getProposePar()), nom(b.getDecidePar()), b.getDateDecision(), b.getCommentaire());
    }

    private static String nom(User u) {
        return u == null ? null : u.getNomComplet() != null ? u.getNomComplet() : u.getLogin();
    }
}
