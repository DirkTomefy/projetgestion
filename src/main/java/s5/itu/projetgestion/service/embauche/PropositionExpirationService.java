package s5.itu.projetgestion.service.embauche;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.repository.embauche.PropositionConditionRepository;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Expiration : une proposition en attente de la réponse du candidat (au moins une condition sans réponse)
 * passe à EXPIREE le lendemain de sa date limite. Une proposition où le candidat a répondu et qui attend
 * la contre-proposition de la RH n'expire pas. La candidature reste RETENUE : la RH peut refaire une proposition.
 */
@Service
@Transactional
public class PropositionExpirationService {

    private final PropositionEmbaucheRepository propositionRepo;
    private final PropositionConditionRepository conditionRepo;
    private final HistoPropositionService histoService;

    public PropositionExpirationService(PropositionEmbaucheRepository propositionRepo,
                                        PropositionConditionRepository conditionRepo,
                                        HistoPropositionService histoService) {
        this.propositionRepo = propositionRepo;
        this.conditionRepo = conditionRepo;
        this.histoService = histoService;
    }

    /** Tâche quotidienne. */
    public int expirerPropositions(LocalDate aujourdhui) {
        List<PropositionEmbauche> aExpirer = propositionRepo.findAExpirer(aujourdhui);
        aExpirer.forEach(this::expirer);
        return aExpirer.size();
    }

    /** Vérification à la volée (page publique, réponse) : true si la proposition vient d'expirer ou l'était. */
    public boolean expirerSiNecessaire(PropositionEmbauche p) {
        if (PropositionEmbauche.STATUT_EXPIREE.equals(p.getStatutActuel())) return true;
        if (!p.estEnCours() || p.getDateLimiteReponse() == null
                || !p.getDateLimiteReponse().isBefore(LocalDate.now())) {
            return false;
        }
        boolean attendCandidat = conditionRepo.findByPropositionId(p.getId()).stream()
                .anyMatch(c -> c.getAccepte() == null);
        if (!attendCandidat) return false;
        expirer(p);
        return true;
    }

    private void expirer(PropositionEmbauche p) {
        histoService.changerStatut(p, PropositionEmbauche.STATUT_EXPIREE, null,
                "Date limite de réponse dépassée (" + ConditionsProposition.date(p.getDateLimiteReponse())
                        + ") sans réponse du candidat");
        propositionRepo.save(p);
    }
}
