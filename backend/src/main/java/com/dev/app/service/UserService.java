package com.dev.app.service;

import com.dev.app.dto.UserDto;
import com.dev.app.entity.User;
import com.dev.app.entity.UserRole;
import com.dev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestión de usuarios.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios activos.
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Obtiene usuarios paginados.
     */
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }

    /**
     * Busca usuarios por nombre o email.
     */
    public Page<User> searchUsers(String search, Pageable pageable) {
        return userRepository.searchByNameOrEmail(search, pageable);
    }

    /**
     * Obtiene un usuario por ID.
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findByUserId(userId);
    }

    /**
     * Obtiene usuarios por rol.
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Crea un nuevo usuario.
     */
    @Transactional
    public User createUser(UserDto dto) {
        log.info("👤 Creando usuario: {}", dto.getEmail());
        
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + dto.getEmail());
        }
        
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .name(dto.getName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .avatarUrl(dto.getAvatarUrl())
                .active(true)
                .build();
        
        return userRepository.save(user);
    }

    /**
     * Actualiza un usuario existente.
     */
    @Transactional
    public Optional<User> updateUser(String userId, UserDto dto) {
        log.info("📝 Actualizando usuario: {}", userId);
        
        return userRepository.findByUserId(userId).map(user -> {
            user.setName(dto.getName());
            user.setPhone(dto.getPhone());
            user.setRole(dto.getRole());
            
            if (dto.getAvatarUrl() != null) {
                user.setAvatarUrl(dto.getAvatarUrl());
            }
            
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            }
            
            return userRepository.save(user);
        });
    }

    /**
     * Actualiza el rol de un usuario.
     */
    @Transactional
    public Optional<User> updateUserRole(String userId, UserRole newRole) {
        log.info("🔄 Actualizando rol de usuario {} a {}", userId, newRole);
        
        return userRepository.findByUserId(userId).map(user -> {
            user.setRole(newRole);
            return userRepository.save(user);
        });
    }

    /**
     * Desactiva un usuario.
     */
    @Transactional
    public boolean deactivateUser(String userId) {
        log.info("🚫 Desactivando usuario: {}", userId);
        
        return userRepository.findByUserId(userId).map(user -> {
            user.setActive(false);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    /**
     * Reactiva un usuario.
     */
    @Transactional
    public boolean activateUser(String userId) {
        log.info("✅ Activando usuario: {}", userId);
        
        return userRepository.findByUserId(userId).map(user -> {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    /**
     * Actualiza el último login.
     */
    @Transactional
    public void updateLastLogin(String userId) {
        userRepository.findByUserId(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    /**
     * Cuenta usuarios por rol.
     */
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}
