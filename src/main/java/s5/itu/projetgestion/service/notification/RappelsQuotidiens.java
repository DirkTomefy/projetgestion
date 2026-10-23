package s5.itu.projetgestion.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.contrat.ContratDto;
import s5.itu.projetgestion.service.organisation.EmployeGestionService;
import s5.itu.projetgestion.service.service.ContratService;

/**
 * Rappels quotidiens (et au démarrage) : contrats arrivant à échéance dans 30 jours (une notification par contrat
 * pour chaque RH), libération des postes après un départ.
 */
@Component
public class RappelsQuotidiens {

    private static final Logger log = LoggerFactory.getLogger(RappelsQuotidiens.class);

    private final ContratService contratService;
    private final NotificationService notificationService;
    private final EmployeGestionService employeGestionService;

    public RappelsQuotidiens(ContratService contratService, NotificationService notificationService,
                             EmployeGestionService employeGestionService) {
        this.contratService = contratService;
        this.notificationService = notificationService;
        this.employeGestionService = employeGestionService;
    }

    @Scheduled(cron = "0 20 0 * * *")
    @Transactional
    public void executer() {
        try {
            int n = 0;
            for (ContratDto c : contratService.echeances(30)) {
                n += notificationService.notifierRoleUneFois("RH", NotificationService.CONTRAT_ECHEANCE,
                        "Le contrat " + c.reference() + " (" + c.type() + ") de " + c.employe() + " arrive à échéance le " + c.dateFinPrevue(),
                        "/contrats/" + c.id());
            }
            employeGestionService.libererPostes();
            if (n > 0) log.info("Rappels : {} notification(s) d'échéance de contrat", n);
        } catch (RuntimeException e) {
            log.error("Échec des rappels quotidiens", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        executer();
    }
}
