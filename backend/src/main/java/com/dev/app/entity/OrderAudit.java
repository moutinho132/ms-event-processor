package com.dev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entidad JPA que representa el log de auditoría de órdenes.
 * Registra todos los eventos procesados para trazabilidad.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Entity
@Table(name = "order_audit_log", indexes = {
    @Index(name = "idx_audit_order", columnList = "order_id"),
    @Index(name = "idx_audit_processed", columnList = "processed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private OrderEventType eventType;
    
    @Column(name = "old_status", length = 20)
    private String oldStatus;
    
    @Column(name = "new_status", nullable = false, length = 20)
    private String newStatus;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Column(name = "processed_at", nullable = false)
    @CreationTimestamp
    private Instant processedAt;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "success", nullable = false)
    private Boolean success;
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
}
