package s5.itu.projetgestion.service.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Envoi SMTP, dans un fil à part : une panne du serveur de mail ne ralentit ni ne fait échouer l'action. */
@Component
public class EmailExpediteur {

    private static final Logger log = LoggerFactory.getLogger(EmailExpediteur.class);

    private final JavaMailSender mailSender;
    private final String expediteur;
    private final String nomExpediteur;

    public EmailExpediteur(JavaMailSender mailSender,
                           @Value("${app.mail.expediteur:recrutement@tovogestion.mg}") String expediteur,
                           @Value("${app.mail.nom-expediteur:Service des ressources humaines}") String nomExpediteur) {
        this.mailSender = mailSender;
        this.expediteur = expediteur;
        this.nomExpediteur = nomExpediteur;
    }

    @Async
    public void envoyer(Email email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(expediteur, nomExpediteur);
            helper.setTo(email.destinataire());
            helper.setSubject(email.sujet());
            helper.setText(email.html(), true);
            mailSender.send(message);
            log.info("Email « {} » envoyé à {}", email.sujet(), email.destinataire());
        } catch (Exception e) {
            log.warn("Email « {} » non envoyé à {} : {}", email.sujet(), email.destinataire(), e.getMessage());
        }
    }
}
