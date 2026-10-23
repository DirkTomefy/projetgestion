package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.model.recrutement.NiveauSelection;
import s5.itu.projetgestion.repository.recrutement.EvaluationRepository;

import java.util.Optional;

/**
 * Désigne le poste responsable d'une évaluation selon la règle de son niveau de sélection :
 * supérieur direct du poste à pourvoir, responsable de son unité, ou poste fixe.
 * Sans règle (ou si la règle ne désigne personne), la RH choisit à la planification.
 */
@Service
public class ResponsableNiveauService {

    private final EvaluationRepository repo;

    public ResponsableNiveauService(EvaluationRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Optional<Integer> resoudre(NiveauSelection niveau, Integer idOffre) {
        if (niveau == null || niveau.getResponsableRegle() == null) return Optional.empty();
        if (NiveauSelection.REGLE_POSTE_FIXE.equals(niveau.getResponsableRegle())) {
            Integer fixe = niveau.getIdPosteResponsable();
            return fixe != null && repo.existsPosteActif(fixe) ? Optional.of(fixe) : Optional.empty();
        }
        Integer aPourvoir = idOffre == null ? null : repo.findPosteAPourvoir(idOffre);
        if (aPourvoir == null) return Optional.empty();
        Integer poste = null;
        if (NiveauSelection.REGLE_SUPERIEUR.equals(niveau.getResponsableRegle())) poste = repo.findSuperieurActif(aPourvoir);
        // sans supérieur direct, ou pour la règle « responsable de l'unité »
        if (poste == null) poste = repo.findResponsableUnite(aPourvoir);
        return Optional.ofNullable(poste);
    }

    /** Libellé du poste désigné, pour l'aperçu dans l'écran des niveaux. */
    @Transactional(readOnly = true)
    public String apercu(NiveauSelection niveau, Integer idOffre) {
        return resoudre(niveau, idOffre).map(repo::findLibellePoste).orElse(null);
    }
}
