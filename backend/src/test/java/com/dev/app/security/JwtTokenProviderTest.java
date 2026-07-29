package com.dev.app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para JwtTokenProvider.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
class JwtTokenProviderTest {
    
    private static final String SECRET = "test-secret-key-for-testing-must-be-at-least-32-characters-long";
    private static final String ISSUER = "test-issuer";
    
    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ISSUER);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    @DisplayName("Debe validar token válido exitosamente")
    void shouldValidateValidTokenSuccessfully() {
        // Given
        String token = Jwts.builder()
                .subject("test-user")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact();
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Debe rechazar token expirado")
    void shouldRejectExpiredToken() {
        // Given
        String token = Jwts.builder()
                .subject("test-user")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(secretKey)
                .compact();
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Debe rechazar token con firma inválida")
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-must-be-at-least-32-chars".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("test-user")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(wrongKey)
                .compact();
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Debe rechazar token malformado")
    void shouldRejectMalformedToken() {
        // Given
        String token = "malformed.token.here";
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Debe extraer username del token")
    void shouldExtractUsernameFromToken() {
        // Given
        String expectedUsername = "test-user";
        String token = Jwts.builder()
                .subject(expectedUsername)
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact();
        
        // When
        String username = jwtTokenProvider.getUsername(token);
        
        // Then
        assertThat(username).isEqualTo(expectedUsername);
    }
    
    @Test
    @DisplayName("Debe extraer roles del token")
    void shouldExtractRolesFromToken() {
        // Given
        String token = Jwts.builder()
                .subject("test-user")
                .issuer(ISSUER)
                .claim("roles", "ROLE_USER")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact();
        
        // When
        List<SimpleGrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
        
        // Then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.get(0).getAuthority()).isEqualTo("ROLE_USER");
    }
    
    @Test
    @DisplayName("Debe retornar lista vacía si no hay roles")
    void shouldReturnEmptyListWhenNoRoles() {
        // Given
        String token = Jwts.builder()
                .subject("test-user")
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(secretKey)
                .compact();
        
        // When
        List<SimpleGrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
        
        // Then
        assertThat(authorities).isEmpty();
    }
    
}
