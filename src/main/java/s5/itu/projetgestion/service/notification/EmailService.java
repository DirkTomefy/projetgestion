package s5.itu.projetgestion.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Emails aux candidats (P4). Règle : un email qui échoue ne fait jamais échouer l'action qui l'a déclenché.
 *  - le contenu est produit tout de suite (modèle Thymeleaf templates/emails/…), toute erreur est journalisée ;
 *  - l'envoi part après la validation de la transaction (rien n'est envoyé si l'action est annulée),
 *    puis dans un fil à part (EmailExpediteur, @Async).
 * app.mail.actif=false coupe l'envoi (les emails sont seulement journalisés).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ITemplateEngine templateEngine;
    private final EmailExpediteur expediteur;
    private final boolean actif;

    public EmailService(ITemplateEngine templateEngine, EmailExpediteur expediteur,
                        @Value("${app.mail.actif:true}") boolean actif) {
        this.templateEngine = templateEngine;
        this.expediteur = expediteur;
        this.actif = actif;
    }

    /** @param modele nom du modèle sous templates/, ex. "emails/candidature-recue" */
    public void envoyer(String destinataire, String sujet, String modele, Map<String, Object> variables) {
        try {
            if (destinataire == null || !EMAIL.matcher(destinataire.trim()).matches()) {
                log.info("Email « {} » non envoyé : adresse absente ou invalide ({})", sujet, destinataire);
                return;
            }
            Context ctx = new Context();
            ctx.setVariables(variables);
            ctx.setVariable("sujet", sujet);
            Email email = new Email(destinataire.trim(), sujet, templateEngine.process(modele, ctx));
            if (!actif) {
                log.info("Email désactivé (app.mail.actif=false) : « {} » pour {}", sujet, email.destinataire());
                return;
            }
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            expediteur.envoyer(email);
                        } catch (RuntimeException e) {   // file d'attente pleine, etc. : l'action est déjà validée
                            log.warn("Email « {} » non envoyé à {} : {}", sujet, email.destinataire(), e.getMessage());
                        }
                    }
                });
            } else {
                expediteur.envoyer(email);
            }
        } catch (Exception e) {
            log.warn("Email « {} » non préparé pour {} : {}", sujet, destinataire, e.getMessage(), e);
        }
    }
}
