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
    private final ResponsableNiveauService responsables;

    public NiveauSelectionService(NiveauSelectionRepository niveauRepo, EvaluationRepository evaluationRepo,
                                  OffreEmploiRepository offreRepo, ResponsableNiveauService responsables) {
        this.responsables = responsables;
        this.niveauRepo = niveauRepo;
        this.evaluationRepo = evaluationRepo;
        this.offreRepo = offreRepo;
    }

    /** Niveaux à afficher pour une offre ; idOffre nul = niveaux communs. */
    @Transactional(readOnly = true)
    public NiveauxSelectionDto lister(Integer idOffre) {
        if (idOffre == null) {
            return new NiveauxSelectionDto(null, false, versDto(niveauRepo.findByIdOffreEmploiIsNullOrderByOrdreAsc(), null));
        }
        verifierOffre(idOffre);
        List<NiveauSelection> propres = niveauRepo.findByIdOffreEmploiOrderByOrdreAsc(idOffre);
        return new NiveauxSelectionDto(idOffre, !propres.isEmpty(), versDto(applicables(idOffre), idOffre));
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
        // un niveau déjà utilisé garde son ordre, son type et son score ; seule la règle du poste responsable
        // peut changer (elle ne vaut que pour les prochaines présélections)
        boolean memeNiveau = n.getOrdre().equals(req.ordre())
                && n.getTypeEvaluation().equalsIgnoreCase(req.typeEvaluation() == null ? "" : req.typeEvaluation().trim())
                && (n.getScoreMin() == null ? req.scoreMin() == null
                    : req.scoreMin() != null && n.getScoreMin().compareTo(req.scoreMin()) == 0);
        if (!memeNiveau) verifierNonUtilise(n);
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
            copie.setResponsableRegle(commun.getResponsableRegle());
            copie.setIdPosteResponsable(commun.getIdPosteResponsable());
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

        String regle = req.responsableRegle() == null || req.responsableRegle().isBlank() ? null : req.responsableRegle().trim().toUpperCase();
        if (regle != null && !NiveauSelection.REGLES.contains(regle)) {
            throw new CandidatureException(400, "Règle inconnue (attendu : " + String.join(", ", NiveauSelection.REGLES) + ")",
                    "responsableRegle");
        }
        Integer fixe = null;
        if (NiveauSelection.REGLE_POSTE_FIXE.equals(regle)) {
            fixe = req.idPosteResponsable();
            if (fixe == null || !evaluationRepo.existsPosteActif(fixe)) {
                throw new CandidatureException(400, "Choisissez un poste actif pour ce niveau", "idPosteResponsable");
            }
        }
        n.setResponsableRegle(regle);
        n.setIdPosteResponsable(fixe);
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

    private List<NiveauSelectionDto> versDto(List<NiveauSelection> niveaux, Integer idOffre) {
        return niveaux.stream().map(n -> versDto(n, idOffre)).toList();
    }

    private NiveauSelectionDto versDto(NiveauSelection n) {
        return versDto(n, n.getIdOffreEmploi());
    }

    /** idOffre : offre pour laquelle on affiche le poste que la règle désigne (null pour les niveaux communs). */
    private NiveauSelectionDto versDto(NiveauSelection n, Integer idOffre) {
        return new NiveauSelectionDto(n.getId(), n.getOrdre(), n.getTypeEvaluation(), n.getScoreMin(),
                n.getIdOffreEmploi(), evaluationRepo.existsByNiveau_Id(n.getId()),
                n.getResponsableRegle(), n.getIdPosteResponsable(),
                n.getIdPosteResponsable() == null ? null : evaluationRepo.findLibellePoste(n.getIdPosteResponsable()),
                idOffre == null ? null : responsables.apercu(n, idOffre));
    }
}
