package s5.itu.projetgestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Active les tâches @Scheduled (clôture des offres, expiration des propositions...). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
