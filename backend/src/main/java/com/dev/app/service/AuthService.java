package com.dev.app.service;

import com.dev.app.dto.AuthRequest;
import com.dev.app.dto.AuthResponse;
import com.dev.app.dto.LoginRequest;
import com.dev.app.entity.Customer;
import com.dev.app.entity.CustomerRole;
import com.dev.app.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Servicio de autenticación y gestión de usuarios.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.issuer}")
    private String jwtIssuer;
    
    @Value("${spring.security.jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Registra un nuevo usuario.
     */
    @Transactional
    public AuthResponse register(AuthRequest request) {
        log.info("📝 Registrando nuevo usuario: {}", request.getEmail());
        
        // Verificar si ya existe
        if (customerRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .status("ERROR")
                    .message("El email ya está registrado")
                    .build();
        }
        
        // Crear cliente
        Customer customer = Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(CustomerRole.CUSTOMER)
                .active(true)
                .build();
        
        customer = customerRepository.save(customer);
        
        // Generar token
        String token = generateToken(customer);
        
        log.info("✅ Usuario registrado exitosamente: {}", customer.getCustomerId());
        
        return AuthResponse.builder()
                .status("SUCCESS")
                .message("Usuario registrado exitosamente")
                .token(token)
                .expiresIn(jwtExpiration / 1000)
                .customer(AuthResponse.CustomerDto.builder()
                        .customerId(customer.getCustomerId())
                        .name(customer.getName())
                        .email(customer.getEmail())
                        .phone(customer.getPhone())
                        .role(customer.getRole().name())
                        .build())
                .build();
    }

    /**
     * Inicia sesión con email y contraseña.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("🔐 Iniciando sesión: {}", request.getEmail());
        
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (customer == null) {
            return AuthResponse.builder()
                    .status("ERROR")
                    .message("Credenciales inválidas")
                    .build();
        }
        
        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            return AuthResponse.builder()
                    .status("ERROR")
                    .message("Credenciales inválidas")
                    .build();
        }
        
        if (!customer.getActive()) {
            return AuthResponse.builder()
                    .status("ERROR")
                    .message("La cuenta está desactivada")
                    .build();
        }
        
        // Generar token
        String token = generateToken(customer);
        
        log.info("✅ Sesión iniciada exitosamente: {}", customer.getCustomerId());
        
        return AuthResponse.builder()
                .status("SUCCESS")
                .message("Sesión iniciada exitosamente")
                .token(token)
                .expiresIn(jwtExpiration / 1000)
                .customer(AuthResponse.CustomerDto.builder()
                        .customerId(customer.getCustomerId())
                        .name(customer.getName())
                        .email(customer.getEmail())
                        .phone(customer.getPhone())
                        .role(customer.getRole().name())
                        .build())
                .build();
    }

    /**
     * Genera un token JWT para el usuario.
     */
    private String generateToken(Customer customer) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .subject(customer.getEmail())
                .claim("userId", customer.getCustomerId())
                .claim("name", customer.getName())
                .claim("roles", customer.getRole().name())
                .issuer(jwtIssuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }
}
