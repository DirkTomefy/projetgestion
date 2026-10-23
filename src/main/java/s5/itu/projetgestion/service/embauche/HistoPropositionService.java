package s5.itu.projetgestion.service.embauche;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.embauche.HistoPropositionDto;
import s5.itu.projetgestion.model.embauche.HistoPropositionEmbauche;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.repository.embauche.HistoPropositionEmbaucheRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Historique d'une proposition : une ligne à chaque changement de statut et à chaque échange. */
@Service
@Transactional
public class HistoPropositionService {

    private final HistoPropositionEmbaucheRepository histoRepo;

    public HistoPropositionService(HistoPropositionEmbaucheRepository histoRepo) {
        this.histoRepo = histoRepo;
    }

    /** Passe la proposition au statut donné et l'historise. idUser NULL : candidat ou tâche automatique. */
    public void changerStatut(PropositionEmbauche p, String statut, Integer idUser, String commentaire) {
        p.setStatutActuel(statut);
        enregistrer(p, idUser, commentaire);
    }

    /** Événement sans changement de statut (réponse partielle, contre-proposition) : statut courant. */
    public void enregistrer(PropositionEmbauche p, Integer idUser, String commentaire) {
        HistoPropositionEmbauche h = new HistoPropositionEmbauche();
        h.setPropositionEmbauche(p);
        h.setStatut(p.getStatutActuel());
        h.setDate(LocalDateTime.now());
        h.setIdUser(idUser);
        h.setCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        histoRepo.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistoPropositionDto> historique(Integer idProposition) {
        return histoRepo.findHistorique(idProposition).stream()
                .map(h -> new HistoPropositionDto(h.getDate(), h.getStatut(),
                        h.getIdUser() == null ? null : h.getUtilisateur(), h.getCommentaire()))
                .toList();
    }
}
