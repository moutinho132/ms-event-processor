package com.dev.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Proveedor de tokens JWT para validación y extracción de claims.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final String issuer;
    
    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
        this.issuer = issuer;
    }
    
    /**
     * Valida un token JWT.
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Firma JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vacío o null: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error validando token JWT: {}", e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * Extrae el username del token JWT.
     * 
     * @param token Token JWT
     * @return Username extraído
     */
    public String getUsername(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extrayendo username del token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae los roles del usuario del token JWT.
     * 
     * @param token Token JWT
     * @return Lista de authorities
     */
    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            String roles = claims.get("roles", String.class);
            
            if (roles == null || roles.isEmpty()) {
                return Collections.emptyList();
            }
            
            return List.of(new SimpleGrantedAuthority(roles));
            
        } catch (Exception e) {
            log.error("Error extrayendo roles del token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Extrae todos los claims del token JWT.
     * 
     * @param token Token JWT
     * @return Claims del token
     */
    public Claims getClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            log.error("Error extrayendo claims del token: {}", e.getMessage());
            return null;
        }
    }
    
}
