package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.recrutement.NiveauSelectionDto;
import s5.itu.projetgestion.dto.recrutement.NiveauSelectionRequest;
import s5.itu.projetgestion.dto.recrutement.NiveauxSelectionDto;
import s5.itu.projetgestion.err.CandidatureException;
import s5.itu.projetgestion.model.recrutement.NiveauSelection;
import s5.itu.projetgestion.repository.recrutement.EvaluationRepository;
import s5.itu.projetgestion.repository.recrutement.NiveauSelectionRepository;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.recrutement.CandidatureRhService.RessourceIntrouvableException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class NiveauSelectionService {

    private final NiveauSelectionRepository niveauRepo;
    private final EvaluationRepository evaluationRepo;
    private final OffreEmploiRepository offreRepo;

    public NiveauSelectionService(NiveauSelectionRepository niveauRepo, EvaluationRepository evaluationRepo,
                                  OffreEmploiRepository offreRepo) {
        this.niveauRepo = niveauRepo;
        this.evaluationRepo = evaluationRepo;
        this.offreRepo = offreRepo;
    }

    /** Niveaux à afficher pour une offre ; idOffre nul = niveaux communs. */
    @Transactional(readOnly = true)
    public NiveauxSelectionDto lister(Integer idOffre) {
        if (idOffre == null) {
            return new NiveauxSelectionDto(null, false, versDto(niveauRepo.findByIdOffreEmploiIsNullOrderByOrdreAsc()));
        }
        verifierOffre(idOffre);
        List<NiveauSelection> propres = niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre);
        return new NiveauxSelectionDto(idOffre, !propres.isEmpty(), versDto(applicables(idOffre)));
    }

    /** Niveaux qui s'appliquent aux candidatures d'une offre, dans l'ordre. */
    @Transactional(readOnly = true)
    public List<NiveauSelection> applicables(Integer idOffre) {
        List<NiveauSelection> propres = niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre);
        return propres.isEmpty() ? niveauRepo.findByIdOffreEmploiIsNullOrderByOrdreAsc() : propres;
    }

    public NiveauSelectionDto creer(NiveauSelectionRequest req) {
        if (req.idOffreEmploi() != null) {
            verifierOffre(req.idOffreEmploi());
        }
        NiveauSelection n = new NiveauSelection();
        n.setIdOffreEmploi(req.idOffreEmploi());
        remplir(n, req);
        return versDto(niveauRepo.save(n));
    }

    public NiveauSelectionDto modifier(Integer id, NiveauSelectionRequest req) {
        NiveauSelection n = trouver(id);
        verifierNonUtilise(n);
        remplir(n, req);
        return versDto(niveauRepo.save(n));
    }

    public void supprimer(Integer id) {
        NiveauSelection n = trouver(id);
        verifierNonUtilise(n);
        niveauRepo.delete(n);
    }

    /** Copie les niveaux communs dans l'offre : la RH peut ensuite les adapter à ce poste. */
    public NiveauxSelectionDto personnaliser(Integer idOffre) {
        verifierOffre(idOffre);
        if (!niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre).isEmpty()) {
            throw new CandidatureException(409, "Cette offre a déjà ses propres niveaux");
        }
        for (NiveauSelection commun : niveauRepo.findByIdOffreEmploiIsNullOrderByOrdreAsc()) {
            NiveauSelection copie = new NiveauSelection();
            copie.setIdOffreEmploi(idOffre);
            copie.setOrdre(commun.getOrdre());
            copie.setTypeEvaluation(commun.getTypeEvaluation());
            copie.setScoreMin(commun.getScoreMin());
            niveauRepo.save(copie);
        }
        return lister(idOffre);
    }

    /** Supprime les niveaux propres à l'offre : elle reprend les niveaux communs. */
    public NiveauxSelectionDto reprendreCommuns(Integer idOffre) {
        verifierOffre(idOffre);
        List<NiveauSelection> propres = niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre);
        propres.forEach(this::verifierNonUtilise);
        niveauRepo.deleteAll(propres);
        return lister(idOffre);
    }

    private void remplir(NiveauSelection n, NiveauSelectionRequest req) {
        String type = req.typeEvaluation().trim().toUpperCase();
        if (!NiveauSelection.TYPES.contains(type)) {
            throw new CandidatureException(400, "Type inconnu (attendu : " + String.join(", ", NiveauSelection.TYPES) + ")",
                    "typeEvaluation");
        }
        boolean ordrePris = groupe(n.getIdOffreEmploi()).stream()
                .anyMatch(autre -> !Objects.equals(autre.getId(), n.getId()) && autre.getOrdre().equals(req.ordre()));
        if (ordrePris) {
            throw new CandidatureException(409, "Un autre niveau a déjà l'ordre " + req.ordre(), "ordre");
        }
        n.setOrdre(req.ordre());
        n.setTypeEvaluation(type);
        n.setScoreMin(req.scoreMin());
    }

    private List<NiveauSelection> groupe(Integer idOffre) {
        return idOffre == null ? niveauRepo.findByIdOffreEmploiIsNullOrderByOrdreAsc()
                : niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre);
    }

    private void verifierNonUtilise(NiveauSelection n) {
        if (evaluationRepo.existsByNiveau_Id(n.getId())) {
            throw new CandidatureException(409,
                    "Ce niveau est déjà utilisé par des candidatures en cours : il ne peut plus être modifié");
        }
    }

    private NiveauSelection trouver(Integer id) {
        return niveauRepo.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Niveau de sélection introuvable : " + id));
    }

    private void verifierOffre(Integer idOffre) {
        if (!offreRepo.existsById(idOffre)) {
            throw new RessourceIntrouvableException("Offre introuvable : " + idOffre);
        }
    }

    private List<NiveauSelectionDto> versDto(List<NiveauSelection> niveaux) {
        return niveaux.stream().map(this::versDto).toList();
    }

    private NiveauSelectionDto versDto(NiveauSelection n) {
        return new NiveauSelectionDto(n.getId(), n.getOrdre(), n.getTypeEvaluation(), n.getScoreMin(),
                n.getIdOffreEmploi(), evaluationRepo.existsByNiveau_Id(n.getId()));
    }
}
