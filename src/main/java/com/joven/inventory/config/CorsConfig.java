package com.joven.inventory.config;

import com.joven.inventory.audit.AuditInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for CORS mappings and interceptor registration.
 * Applies CORS settings from {@link AppProperties} and registers the {@link AuditInterceptor}.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;
    private final AuditInterceptor auditInterceptor;

    /**
     * Configures CORS mappings for all API endpoints based on application properties.
     *
     * @param registry the CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        AppProperties.Cors cors = appProperties.getCors();

        String[] origins = cors.getAllowedOrigins() != null
                ? cors.getAllowedOrigins().split(",")
                : new String[]{"*"};

        String[] methods = cors.getAllowedMethods() != null
                ? cors.getAllowedMethods().split(",")
                : new String[]{"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

        String[] headers = cors.getAllowedHeaders() != null
                ? cors.getAllowedHeaders().split(",")
                : new String[]{"*"};

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .allowCredentials(cors.isAllowCredentials())
                .maxAge(cors.getMaxAge());
    }

    /**
     * Registers interceptors for request processing.
     * Adds the {@link AuditInterceptor} to log API access.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**");
    }
}
