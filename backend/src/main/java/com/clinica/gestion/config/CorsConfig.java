package com.clinica.gestion.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend (Vercel) llame a la API (Railway).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Siempre se aceptan los dominios de Vercel y localhost, ademas de FRONTEND_URL.
        List<String> origenes = new ArrayList<>(List.of(frontendUrl.split(",")));
        origenes.add("https://*.vercel.app");
        origenes.add("http://localhost:*");
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenes.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
