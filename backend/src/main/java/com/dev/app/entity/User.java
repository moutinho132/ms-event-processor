package com.dev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entidad JPA que representa un usuario del sistema.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_user_id", columnList = "user_id"),
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true, updatable = false, length = 36)
    private String userId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    
}
