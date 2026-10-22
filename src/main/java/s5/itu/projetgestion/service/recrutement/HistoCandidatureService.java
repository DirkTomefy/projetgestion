package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.recrutement.HistoCandidatureDto;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.HistoCandidature;
import s5.itu.projetgestion.repository.recrutement.CandidatureRepository;
import s5.itu.projetgestion.repository.recrutement.HistoCandidatureRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HistoCandidatureService {

    private final HistoCandidatureRepository histoRepo;
    private final CandidatureRepository candidatureRepo;

    public HistoCandidatureService(HistoCandidatureRepository histoRepo, CandidatureRepository candidatureRepo) {
        this.histoRepo = histoRepo;
        this.candidatureRepo = candidatureRepo;
    }

    /** Première ligne d'historique, au dépôt depuis le site public. */
    public void depot(Candidature candidature) {
        enregistrer(candidature, null, candidature.getEtat(), null, "Dépôt de la candidature");
    }

    public void changerEtat(Candidature candidature, String nouvelEtat, Integer idUser, String commentaire) {
        String avant = candidature.getEtat();
        candidature.setEtat(nouvelEtat);
        candidatureRepo.save(candidature);
        enregistrer(candidature, avant, nouvelEtat, idUser, commentaire);
    }

    @Transactional(readOnly = true)
    public List<HistoCandidatureDto> historique(Integer idCandidature) {
        return histoRepo.findHistorique(idCandidature).stream()
                .map(h -> new HistoCandidatureDto(h.getDateChangement(), h.getEtatAvant(), h.getEtatApres(),
                        h.getUtilisateur(), h.getCommentaire()))
                .toList();
    }

    private void enregistrer(Candidature c, String avant, String apres, Integer idUser, String commentaire) {
        HistoCandidature h = new HistoCandidature();
        h.setCandidature(c);
        h.setEtatAvant(avant);
        h.setEtatApres(apres);
        h.setIdUser(idUser);
        h.setDateChangement(LocalDateTime.now());
        h.setCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        histoRepo.save(h);
    }
}
