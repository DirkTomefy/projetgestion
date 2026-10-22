package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.recrutement.CandidatureRetenueDto;
import s5.itu.projetgestion.dto.recrutement.EvaluationDto;
import s5.itu.projetgestion.dto.recrutement.SelectionDto;
import s5.itu.projetgestion.err.CandidatureException;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.Evaluation;
import s5.itu.projetgestion.model.recrutement.NiveauSelection;
import s5.itu.projetgestion.model.recrutement.Traitement;
import s5.itu.projetgestion.repository.recrutement.CandidatureRepository;
import s5.itu.projetgestion.repository.recrutement.EvaluationRepository;
import s5.itu.projetgestion.repository.recrutement.TraitementRepository;
import s5.itu.projetgestion.service.recrutement.CandidatureRhService.RessourceIntrouvableException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SelectionService {

    public static final String STATUT_FAITE = "FAITE";
    public static final String STATUT_A_FAIRE = "A_FAIRE";
    public static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUT_ANNULEE = "ANNULEE";

    private final CandidatureRepository candidatureRepo;
    private final TraitementRepository traitementRepo;
    private final EvaluationRepository evaluationRepo;
    private final NiveauSelectionService niveauService;
    private final HistoCandidatureService histoService;

    public SelectionService(CandidatureRepository candidatureRepo, TraitementRepository traitementRepo,
                            EvaluationRepository evaluationRepo, NiveauSelectionService niveauService,
                            HistoCandidatureService histoService) {
        this.candidatureRepo = candidatureRepo;
        this.traitementRepo = traitementRepo;
        this.evaluationRepo = evaluationRepo;
        this.niveauService = niveauService;
        this.histoService = histoService;
    }

    @Transactional(readOnly = true)
    public SelectionDto dossier(Integer idCandidature) {
        Candidature c = trouver(idCandidature);
        String etat = c.getEtat();
        return new SelectionDto(c.getId(), etat, evaluations(c), histoService.historique(c.getId()),
                Candidature.ETAT_DEPOSEE.equals(etat), peutEtreRefusee(etat));
    }

    /** Pour P4 : candidatures RETENUE qui attendent une proposition d'embauche. */
    @Transactional(readOnly = true)
    public List<CandidatureRetenueDto> retenuesSansProposition() {
        return traitementRepo.findRetenuesSansProposition().stream()
                .map(r -> new CandidatureRetenueDto(r.getIdCandidature(), r.getDateDepot(), r.getDateRetenue(),
                        r.getIdOffre(), r.getReferenceOffre(), r.getIdBesoin(), r.getPostes(), r.getIdCandidat(),
                        r.getNom(), r.getPrenom(), r.getEmail(), r.getTelephone()))
                .toList();
    }

    /** DEPOSEE → PRESELECTIONNER, et création des évaluations à passer. */
    public SelectionDto preselectionner(Integer idCandidature, Integer idUser, String commentaire) {
        Candidature c = trouver(idCandidature);
        if (!Candidature.ETAT_DEPOSEE.equals(c.getEtat())) {
            throw new CandidatureException(409, "Seule une candidature nouvelle peut être présélectionnée");
        }
        List<NiveauSelection> niveaux = niveauService.applicables(c.getIdOffreEmploi());
        if (niveaux.isEmpty()) {
            throw new CandidatureException(409, "Aucun niveau de sélection n'est défini : ajoutez-en d'abord");
        }
        if (traitementRepo.findByCandidature_Id(c.getId()).isPresent()) {
            throw new CandidatureException(409, "La sélection de cette candidature a déjà commencé");
        }
        Traitement t = new Traitement();
        t.setCandidature(c);
        t = traitementRepo.save(t);
        for (NiveauSelection n : niveaux) {
            Evaluation e = new Evaluation();
            e.setTraitement(t);
            e.setNiveau(n);
            evaluationRepo.save(e);
        }
        histoService.changerEtat(c, Candidature.ETAT_PRESELECTIONNER, idUser, commentaire);
        return dossier(c.getId());
    }

    /** Refus par la RH, avant ou pendant la sélection ; le motif est obligatoire. */
    public SelectionDto refuser(Integer idCandidature, Integer idUser, String commentaire) {
        Candidature c = trouver(idCandidature);
        if (!peutEtreRefusee(c.getEtat())) {
            throw new CandidatureException(409, "Cette candidature ne peut plus être refusée (état : " + c.getEtat() + ")");
        }
        if (commentaire == null || commentaire.isBlank()) {
            throw new CandidatureException(400, "Indiquez le motif du refus", "commentaire");
        }
        histoService.changerEtat(c, Candidature.ETAT_REFUSE, idUser, commentaire);
        return dossier(c.getId());
    }

    private static boolean peutEtreRefusee(String etat) {
        return Candidature.ETAT_DEPOSEE.equals(etat) || Candidature.ETAT_PRESELECTIONNER.equals(etat);
    }

    /** Évaluations dans l'ordre des niveaux, avec le statut de chacune. */
    private List<EvaluationDto> evaluations(Candidature c) {
        boolean enCours = Candidature.ETAT_PRESELECTIONNER.equals(c.getEtat());
        boolean niveauEnCoursTrouve = false;
        List<EvaluationDto> resultat = new ArrayList<>();
        for (EvaluationRepository.EvaluationRow r : evaluationRepo.findLignes(c.getId())) {
            String statut;
            if (r.getDecision() != null) {
                statut = STATUT_FAITE;
            } else if (!enCours) {
                statut = STATUT_ANNULEE;
            } else if (!niveauEnCoursTrouve) {
                statut = STATUT_A_FAIRE;
                niveauEnCoursTrouve = true;
            } else {
                statut = STATUT_EN_ATTENTE;
            }
            resultat.add(new EvaluationDto(r.getId(), r.getOrdre(), r.getTypeEvaluation(), r.getScoreMin(),
                    r.getDatePrevue(), r.getDateRealisee(), r.getLibelle(), r.getIdPosteResponsable(),
                    r.getPosteResponsable(), r.getEvaluateur(), r.getScore(), r.getDecision(),
                    r.getCommentaire(), statut));
        }
        return resultat;
    }

    private Candidature trouver(Integer idCandidature) {
        return candidatureRepo.findById(idCandidature)
                .orElseThrow(() -> new RessourceIntrouvableException("Candidature introuvable : " + idCandidature));
    }
}
