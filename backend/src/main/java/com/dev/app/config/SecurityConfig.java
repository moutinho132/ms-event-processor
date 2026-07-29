package com.dev.app.config;

import com.dev.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 * 
 * Configura la seguridad de la aplicación con autenticación JWT stateless.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * @param http HttpSecurity configurador
     * @return SecurityFilterChain configurada
     * @throws Exception en caso de error de configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para APIs stateless
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar sesión stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar autorización de endpoints
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a actuator endpoints sin autenticación
                .requestMatchers("/actuator/**").permitAll()
                // Permitir acceso a health checks
                .requestMatchers("/health").permitAll()
                // Permitir acceso a Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Permitir acceso a API de órdenes (para demo/testing)
                .requestMatchers("/api/**").permitAll()
                // Requerir autenticación para todos los demás endpoints
                .anyRequest().authenticated()
            )
            
            // Agregar filtro JWT antes del filtro de autenticación por username/password
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
}
