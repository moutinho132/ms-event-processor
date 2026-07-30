package com.dev.app.dto;

import com.dev.app.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para Usuario.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private String userId;
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String phone;
    
    @NotNull(message = "El rol es requerido")
    private UserRole role;
    
    private String avatarUrl;
    
    private Boolean active;
    
    private Instant createdAt;
    
    private Instant lastLoginAt;
    
    // Para creación/actualización de contraseña
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
