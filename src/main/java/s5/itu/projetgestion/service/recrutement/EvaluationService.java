package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.recrutement.EvaluationAVenirDto;
import s5.itu.projetgestion.dto.recrutement.MesEvaluationsDto;
import s5.itu.projetgestion.dto.recrutement.PlanificationRequest;
import s5.itu.projetgestion.dto.recrutement.PosteOptionDto;
import s5.itu.projetgestion.dto.recrutement.ResultatRequest;
import s5.itu.projetgestion.dto.recrutement.SelectionDto;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.CandidatureException;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.Evaluation;
import s5.itu.projetgestion.model.recrutement.NiveauSelection;
import s5.itu.projetgestion.repository.recrutement.EvaluationRepository;
import s5.itu.projetgestion.service.recrutement.CandidatureRhService.RessourceIntrouvableException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluationRepo;
    private final SelectionService selectionService;
    private final HistoCandidatureService histoService;
    private final s5.itu.projetgestion.service.organisation.PostesUtilisateurService postesUtilisateur;
    private final s5.itu.projetgestion.service.notification.NotificationService notifications;
    private final s5.itu.projetgestion.service.notification.CommunicationCandidatService communication;

    public EvaluationService(s5.itu.projetgestion.service.notification.NotificationService notifications,
                             s5.itu.projetgestion.service.notification.CommunicationCandidatService communication,EvaluationRepository evaluationRepo, SelectionService selectionService,
                             HistoCandidatureService histoService,
                             s5.itu.projetgestion.service.organisation.PostesUtilisateurService postesUtilisateur) {
        this.notifications = notifications;
        this.communication = communication;
        this.postesUtilisateur = postesUtilisateur;
        this.evaluationRepo = evaluationRepo;
        this.selectionService = selectionService;
        this.histoService = histoService;
    }

    @Transactional(readOnly = true)
    public List<PosteOptionDto> postesResponsables() {
        return evaluationRepo.findPostesActifs().stream()
                .map(p -> new PosteOptionDto(p.getId(), p.getLibelle(), p.getUnite()))
                .toList();
    }

    /** Évaluations à venir pour le poste actuel de l'utilisateur connecté. */
    @Transactional(readOnly = true)
    public MesEvaluationsDto mesEvaluations(SessionUser user) {
        List<Integer> postes = List.copyOf(postesUtilisateur.postesDe(user.getId()));   // postes occupés et délégués
        if (user.getIdEmploye() == null && postes.isEmpty()) {
            return new MesEvaluationsDto(false, List.of());
        }
        if (postes.isEmpty()) {
            return new MesEvaluationsDto(true, List.of());
        }
        return new MesEvaluationsDto(true, evaluationRepo.findAVenirPourPostes(postes).stream()
                .map(r -> new EvaluationAVenirDto(r.getId(), r.getOrdre(), r.getTypeEvaluation(), r.getScoreMin(),
                        r.getDatePrevue(), r.getLibelle(), r.getPosteResponsable(), r.getIdCandidature(),
                        r.getPrenom() == null ? r.getNom() : r.getNom() + " " + r.getPrenom(),
                        r.getReferenceOffre(), Boolean.TRUE.equals(r.getSaisissable())))
                .toList());
    }

    /** La RH fixe la date et le poste qui fera passer l'évaluation (un niveau à venir peut être planifié d'avance). */
    public SelectionDto planifier(Integer idEvaluation, PlanificationRequest req) {
        Evaluation e = trouver(idEvaluation);
        Candidature c = e.getTraitement().getCandidature();
        if (!Candidature.ETAT_PRESELECTIONNER.equals(c.getEtat())) {
            throw new CandidatureException(409, "La sélection de cette candidature est terminée");
        }
        if (e.estFaite()) {
            throw new CandidatureException(409, "Cette évaluation a déjà eu lieu");
        }
        if (!evaluationRepo.existsPosteActif(req.idPosteResponsable())) {
            throw new CandidatureException(400, "Poste introuvable ou inactif", "idPosteResponsable");
        }
        java.time.LocalDate ancienneDate = e.getDatePrevue();
        Integer ancienPoste = e.getIdPosteResponsable();
        String ancienLibelle = e.getLibelle();
        e.setDatePrevue(req.datePrevue());
        e.setIdPosteResponsable(req.idPosteResponsable());
        e.setLibelle(req.libelle() == null || req.libelle().isBlank() ? null : req.libelle().trim());
        evaluationRepo.save(e);
        boolean dejaPlanifiee = ancienPoste != null;
        boolean dateChangee = !java.util.Objects.equals(ancienneDate, e.getDatePrevue());
        boolean posteChange = !req.idPosteResponsable().equals(ancienPoste);
        boolean libelleChange = !java.util.Objects.equals(ancienLibelle, e.getLibelle());
        if (dateChangee || posteChange || libelleChange) prevenirPlanification(e, c, dejaPlanifiee, dateChangee || libelleChange, posteChange || dateChangee);
        return selectionService.dossier(c.getId());
    }

    /**
     * Prévient le candidat (email de convocation, sans effet sur l'enregistrement s'il échoue) et le poste responsable
     * (notification dans l'application). En cas de replanification, le message le précise.
     */
    private void prevenirPlanification(Evaluation e, Candidature c, boolean replanification, boolean prevenirCandidat, boolean prevenirResponsable) {
        String type = libelleType(e.getNiveau().getTypeEvaluation());
        var ca = c.getCandidat();
        String nom = ca.getPrenom() == null ? ca.getNom() : ca.getNom() + " " + ca.getPrenom();
        String quand = e.getDatePrevue() == null ? "" : " le " + s5.itu.projetgestion.service.embauche.ConditionsProposition.date(e.getDatePrevue());
        if (prevenirResponsable) {
            notifications.notifierPoste(e.getIdPosteResponsable(), "EVALUATION_PLANIFIEE",
                    (replanification ? "Mise à jour : " : "") + type.substring(0, 1).toUpperCase() + type.substring(1)
                            + " de " + nom + " prévu" + quand + " : vous êtes responsable de son déroulement.",
                    "/mes-evaluations");
        }
        if (prevenirCandidat) communication.evaluationPlanifiee(c, type, e.getDatePrevue(), e.getLibelle(), replanification);
    }

    private static String libelleType(String t) {
        return switch (t == null ? "" : t) {
            case NiveauSelection.TYPE_TEST -> "test";
            case NiveauSelection.TYPE_ENTRETIEN_RH -> "entretien RH";
            case NiveauSelection.TYPE_ENTRETIEN_CHEF_DEPARTEMENT -> "entretien avec le chef de département";
            case NiveauSelection.TYPE_ENTRETIEN_DIRECTION -> "entretien avec la direction";
            default -> "évaluation";
        };
    }

    /** Postes au nom desquels l'utilisateur peut saisir des résultats (occupés ou délégués). */
    @Transactional(readOnly = true)
    public List<Integer> mesPostes(SessionUser user) {
        return List.copyOf(postesUtilisateur.postesDe(user.getId()));
    }

    /**
     * Saisie du résultat par le titulaire du poste responsable de l'évaluation (ou son délégué).
     * La RH planifie mais ne saisit pas à la place de l'évaluateur, sauf quand l'évaluation est confiée à son poste.
     */
    public SelectionDto saisirResultat(Integer idEvaluation, ResultatRequest req, SessionUser user) {
        Evaluation e = trouver(idEvaluation);
        Candidature c = e.getTraitement().getCandidature();
        verifierDroitSaisie(e, user);
        List<Evaluation> toutes = evaluationRepo.findByTraitement_Candidature_IdOrderByNiveau_OrdreAsc(c.getId());
        verifierNiveauEnCours(e, c, toutes);

        NiveauSelection niveau = e.getNiveau();
        String decision = decider(niveau, req);
        e.setDateRealisee(req.dateRealisee());
        e.setScore(Evaluation.DECISION_ABSENT.equals(decision) ? null : req.score());
        e.setDecision(decision);
        e.setCommentaire(req.commentaire() == null || req.commentaire().isBlank() ? null : req.commentaire().trim());
        e.setIdUser(user.getId());
        evaluationRepo.save(e);

        String libelleNiveau = "niveau " + niveau.getOrdre() + " (" + niveau.getTypeEvaluation() + ")";
        var ca = c.getCandidat();
        String nomCandidat = ca.getPrenom() == null ? ca.getNom() : ca.getNom() + " " + ca.getPrenom();
        notifications.notifierRole("RH", "EVALUATION_RESULTAT",
                "Résultat du " + libelleType(niveau.getTypeEvaluation()) + " de " + nomCandidat + " : "
                        + switch (decision) { case Evaluation.DECISION_ADMIS -> "admis"; case Evaluation.DECISION_ABSENT -> "absent"; default -> "non admis"; }
                        + (e.getScore() != null ? " (" + e.getScore().stripTrailingZeros().toPlainString() + "/20)" : "")
                        + ", saisi par " + (user.getNomComplet() != null ? user.getNomComplet() : user.getLogin()) + ".",
                "/candidatures");
        if (!Evaluation.DECISION_ADMIS.equals(decision)) {
            String motif = (Evaluation.DECISION_ABSENT.equals(decision) ? "Absent au " : "Non admis au ") + libelleNiveau
                    + (e.getScore() != null ? " : " + e.getScore().stripTrailingZeros().toPlainString() + "/20" : "")
                    + (e.getCommentaire() != null ? " – " + e.getCommentaire() : "");
            histoService.changerEtat(c, Candidature.ETAT_REFUSE, user.getId(), motif);
        } else if (e.getId().equals(toutes.get(toutes.size() - 1).getId())) {
            histoService.changerEtat(c, Candidature.ETAT_RETENUE, user.getId(),
                    "Admis à tous les niveaux de sélection");
        }
        return selectionService.dossier(c.getId());
    }

    /** ABSENT est toujours possible ; sinon le score décide, ou l'évaluateur quand le niveau n'a pas de minimum. */
    static String decider(NiveauSelection niveau, ResultatRequest req) {
        String demandee = req.decision() == null ? null : req.decision().trim().toUpperCase();
        if (Evaluation.DECISION_ABSENT.equals(demandee)) {
            return Evaluation.DECISION_ABSENT;
        }
        BigDecimal min = niveau.getScoreMin();
        if (min != null) {
            if (req.score() == null) {
                throw new CandidatureException(400, "Le score est obligatoire pour ce niveau (minimum "
                        + min.stripTrailingZeros().toPlainString() + "/20)", "score");
            }
            return req.score().compareTo(min) >= 0 ? Evaluation.DECISION_ADMIS : Evaluation.DECISION_NON_ADMIS;
        }
        if (!Evaluation.DECISION_ADMIS.equals(demandee) && !Evaluation.DECISION_NON_ADMIS.equals(demandee)) {
            throw new CandidatureException(400, "Choisissez une décision : admis, non admis ou absent", "decision");
        }
        return demandee;
    }

    /** Seul le niveau en cours peut recevoir un résultat : tous les niveaux précédents doivent être réussis. */
    private static void verifierNiveauEnCours(Evaluation e, Candidature c, List<Evaluation> toutes) {
        if (!Candidature.ETAT_PRESELECTIONNER.equals(c.getEtat())) {
            throw new CandidatureException(409, "La sélection de cette candidature est terminée");
        }
        if (e.estFaite()) {
            throw new CandidatureException(409, "Le résultat de cette évaluation est déjà enregistré");
        }
        for (Evaluation precedente : toutes) {
            if (precedente.getId().equals(e.getId())) {
                return;
            }
            if (!Evaluation.DECISION_ADMIS.equals(precedente.getDecision())) {
                throw new CandidatureException(409, "Le niveau " + precedente.getNiveau().getOrdre()
                        + " doit d'abord être réussi");
            }
        }
    }

    private void verifierDroitSaisie(Evaluation e, SessionUser user) {
        if (e.getIdPosteResponsable() == null) {
            throw new CandidatureException(409, "Planifiez d'abord cette évaluation : choisissez le poste qui la fera passer");
        }
        if (!postesUtilisateur.postesDe(user.getId()).contains(e.getIdPosteResponsable())) {
            throw new AccesRefuseException("Seul le titulaire du poste responsable de cette évaluation (ou son délégué) peut saisir le résultat");
        }
    }

    private Evaluation trouver(Integer id) {
        return evaluationRepo.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Évaluation introuvable : " + id));
    }
}
