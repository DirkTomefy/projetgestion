package s5.itu.projetgestion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Properties;

/**
 * Emails (P4) : envoi asynchrone (@EnableAsync) vers un serveur SMTP.
 * Par défaut : Mailpit ou MailHog en local (localhost:1025, sans authentification ; boîte web sur :8025).
 * Les propriétés spring.mail.* habituelles sont prises en compte si elles sont définies.
 */
@Configuration
@EnableAsync
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(@Value("${spring.mail.host:localhost}") String host,
                                         @Value("${spring.mail.port:1025}") int port,
                                         @Value("${spring.mail.username:}") String username,
                                         @Value("${spring.mail.password:}") String password,
                                         @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean starttls) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setDefaultEncoding("UTF-8");
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        if (!username.isBlank()) {
            sender.setUsername(username);
            sender.setPassword(password);
            props.put("mail.smtp.auth", "true");
        }
        if (starttls) props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
