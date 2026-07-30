package com.dev.app.controller;

import com.dev.app.dto.UserDto;
import com.dev.app.entity.User;
import com.dev.app.entity.UserRole;
import com.dev.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de usuarios.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para gestión de usuarios")
public class UserController {

    private final UserService userService;

    /**
     * Lista todos los usuarios activos.
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios activos")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllActiveUsers());
    }

    /**
     * Lista usuarios paginados.
     */
    @GetMapping("/page")
    @Operation(summary = "Listar usuarios paginados", description = "Obtiene usuarios con paginación")
    public ResponseEntity<Page<User>> getUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    /**
     * Busca usuarios por nombre o email.
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre o email")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.searchUsers(query, pageable));
    }

    /**
     * Obtiene un usuario por ID.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Obtener usuario", description = "Obtiene un usuario por su ID")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene usuarios por rol.
     */
    @GetMapping("/role/{role}")
    @Operation(summary = "Usuarios por rol", description = "Obtiene usuarios filtrados por rol")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Crea un nuevo usuario.
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) {
        log.info("👤 Creando usuario: {}", dto.getEmail());
        
        try {
            User user = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Actualiza un usuario.
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserDto dto) {
        
        return userService.updateUser(userId, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza el rol de un usuario.
     */
    @PatchMapping("/{userId}/role")
    @Operation(summary = "Actualizar rol", description = "Actualiza el rol de un usuario")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestParam UserRole role) {
        
        return userService.updateUserRole(userId, role)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Desactiva un usuario.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Desactivar usuario", description = "Desactiva un usuario del sistema")
    public ResponseEntity<Map<String, Object>> deactivateUser(@PathVariable String userId) {
        boolean deleted = userService.deactivateUser(userId);
        
        Map<String, Object> response = new LinkedHashMap<>();
        if (deleted) {
            response.put("status", "SUCCESS");
            response.put("message", "Usuario desactivado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "ERROR");
            response.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Reactiva un usuario.
     */
    @PatchMapping("/{userId}/activate")
    @Operation(summary = "Activar usuario", description = "Reactiva un usuario desactivado")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable String userId) {
        boolean activated = userService.activateUser(userId);
        
        Map<String, Object> response = new LinkedHashMap<>();
        if (activated) {
            response.put("status", "SUCCESS");
            response.put("message", "Usuario activado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "ERROR");
            response.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Estadísticas de usuarios.
     */
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas de usuarios", description = "Obtiene estadísticas por rol")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalAdmins", userService.countByRole(UserRole.ADMIN));
        stats.put("totalSupervisors", userService.countByRole(UserRole.SUPERVISOR));
        stats.put("totalCustomers", userService.countByRole(UserRole.CUSTOMER));
        return ResponseEntity.ok(stats);
    }
}
