package s5.itu.projetgestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",              // ← couvre GET et POST /auth/login, /auth/logout
                        "/error", "/error/**",   // ← indispensable, sinon boucle infinie
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/favicon.ico"
                );
    }
}