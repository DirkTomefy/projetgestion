package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.auth.SessionUser;
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

    public EvaluationService(EvaluationRepository evaluationRepo, SelectionService selectionService,
                             HistoCandidatureService histoService) {
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
        e.setDatePrevue(req.datePrevue());
        e.setIdPosteResponsable(req.idPosteResponsable());
        e.setLibelle(req.libelle() == null || req.libelle().isBlank() ? null : req.libelle().trim());
        evaluationRepo.save(e);
        return selectionService.dossier(c.getId());
    }

    /** Saisie du résultat par la RH ou par le titulaire du poste responsable. */
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
        if (user.hasRole(OffreEmploiService.ROLE_RH)) {
            return;
        }
        boolean responsable = e.getIdPosteResponsable() != null && user.getIdEmploye() != null
                && evaluationRepo.findPostesActuels(user.getIdEmploye()).contains(e.getIdPosteResponsable());
        if (!responsable) {
            throw new AccesRefuseException("Seuls la RH et le responsable de cette évaluation peuvent saisir le résultat");
        }
    }

    private Evaluation trouver(Integer id) {
        return evaluationRepo.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Évaluation introuvable : " + id));
    }
}
