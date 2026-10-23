package s5.itu.projetgestion.config;

import s5.itu.projetgestion.service.audit.AuditService;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;
    private final AuditService auditService;

    public WebConfig(UploadProperties uploadProperties, AuditService auditService) {
        this.uploadProperties = uploadProperties;
        this.auditService = auditService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService))
                .addPathPatterns("/api/**", "/auth/**");
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/api/public/**",
                        "/error", "/error/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/favicon.ico"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadProperties.getDir())
                .toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler(uploadProperties.getUrlPrefix() + "**")
                .addResourceLocations(location);
    }
}