package com.dev.app.controller;

import com.dev.app.dto.AuthRequest;
import com.dev.app.dto.AuthResponse;
import com.dev.app.dto.LoginRequest;
import com.dev.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario.
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario y retorna un token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya registrado")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        log.info("📤 Solicitud de registro: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Inicia sesión.
     */
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Sesión iniciada exitosamente")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("📤 Solicitud de login: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.status(401).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de prueba para verificar autenticación.
     */
    @GetMapping("/me")
    @Operation(
        summary = "Obtener usuario actual",
        description = "Retorna información del usuario autenticado"
    )
    public ResponseEntity<?> getCurrentUser() {
        // El usuario se obtiene del SecurityContext
        return ResponseEntity.ok(java.util.Map.of(
                "status", "SUCCESS",
                "message", "Usuario autenticado correctamente"
        ));
    }
}
