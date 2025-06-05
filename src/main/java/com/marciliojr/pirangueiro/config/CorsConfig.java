package com.marciliojr.pirangueiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuração global de CORS para permitir acesso de frontends externos.
 * Esta configuração é especialmente útil quando a aplicação roda em Docker
 * e precisa ser acessada por SPAs (Single Page Applications).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configuração global de CORS usando WebMvcConfigurer.
     * Esta é a abordagem mais simples e recomendada para Spring Boot.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Permite qualquer origem
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache de preflight por 1 hora
    }

    /**
     * Bean de configuração CORS alternativo para casos mais complexos.
     * Este bean será usado automaticamente pelo Spring Security se presente.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite qualquer origem
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permite envio de credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Headers expostos para o cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization",
            "Content-Disposition"
        ));
        
        // Cache de preflight (em segundos)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Filtro CORS customizado para garantia adicional.
     * Este filtro é processado antes de outros filtros.
     */
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
} 