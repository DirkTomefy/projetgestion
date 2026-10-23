package s5.itu.projetgestion.service.recrutement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.recrutement.HistoCandidatureDto;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.HistoCandidature;
import s5.itu.projetgestion.repository.recrutement.CandidatureRepository;
import s5.itu.projetgestion.repository.recrutement.HistoCandidatureRepository;
import s5.itu.projetgestion.service.notification.CommunicationCandidatService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HistoCandidatureService {

    private final HistoCandidatureRepository histoRepo;
    private final CandidatureRepository candidatureRepo;
    private final CommunicationCandidatService communication;
    private final s5.itu.projetgestion.service.notification.NotificationService notificationService;

    public HistoCandidatureService(HistoCandidatureRepository histoRepo, CandidatureRepository candidatureRepo,
                                   CommunicationCandidatService communication,
                                   s5.itu.projetgestion.service.notification.NotificationService notificationService) {
        this.notificationService = notificationService;
        this.histoRepo = histoRepo;
        this.candidatureRepo = candidatureRepo;
        this.communication = communication;
    }

    /** Première ligne d'historique, au dépôt depuis le site public. */
    public void depot(Candidature candidature) {
        enregistrer(candidature, null, candidature.getEtat(), null, "Dépôt de la candidature");
        communication.candidatureRecue(candidature);   // P4 : accusé de réception (après validation, sans risque)
        var ca = candidature.getCandidat();
        Integer idOffre = candidature.getIdOffreEmploi();
        notificationService.notifierRole("RH", s5.itu.projetgestion.service.notification.NotificationService.CANDIDATURE_RECUE,
                "Nouvelle candidature de " + ca.getNom() + (ca.getPrenom() == null ? "" : " " + ca.getPrenom()),
                "/candidatures" + (idOffre == null ? "" : "?offre=" + idOffre));
    }

    public void changerEtat(Candidature candidature, String nouvelEtat, Integer idUser, String commentaire) {
        String avant = candidature.getEtat();
        candidature.setEtat(nouvelEtat);
        candidatureRepo.save(candidature);
        enregistrer(candidature, avant, nouvelEtat, idUser, commentaire);
        if (Candidature.ETAT_REFUSE.equals(nouvelEtat) && !Candidature.ETAT_REFUSE.equals(avant)) {
            communication.candidatureRefusee(candidature);   // P4 : email de résultat
        }
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
