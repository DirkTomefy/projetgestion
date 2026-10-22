package s5.itu.projetgestion.service.recrutement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import s5.itu.projetgestion.service.embauche.PropositionExpirationService;

import java.time.LocalDate;

/**
 * Tâche quotidienne P4 (par défaut à 00:05, propriété app.recrutement.cron) :
 * clôture des offres dont la date limite est dépassée ou dont tous les sièges sont pourvus,
 * et expiration des propositions restées sans réponse après leur date limite.
 * Elle tourne aussi au démarrage, pour rattraper les jours où l'application était arrêtée.
 */
@Component
public class TacheQuotidienneRecrutement {

    private static final Logger log = LoggerFactory.getLogger(TacheQuotidienneRecrutement.class);

    private final OffreClotureService clotureService;
    private final PropositionExpirationService expirationService;

    public TacheQuotidienneRecrutement(OffreClotureService clotureService,
                                       PropositionExpirationService expirationService) {
        this.clotureService = clotureService;
        this.expirationService = expirationService;
    }

    @Scheduled(cron = "${app.recrutement.cron:0 5 0 * * *}")
    public void executer() {
        LocalDate aujourdhui = LocalDate.now();
        try {
            int dates = clotureService.cloturerDatesDepassees(aujourdhui);
            int pourvues = clotureService.cloturerSiPostesPourvus(null);
            int expirees = expirationService.expirerPropositions(aujourdhui);
            log.info("Tâche quotidienne recrutement : {} offre(s) clôturée(s) par date limite, "
                    + "{} par postes pourvus, {} proposition(s) expirée(s)", dates, pourvues, expirees);
        } catch (RuntimeException e) {
            log.error("Échec de la tâche quotidienne de recrutement", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        executer();
    }
}
