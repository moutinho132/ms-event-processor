package com.dev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA que representa una orden en el sistema.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_order_id", columnList = "order_id"),
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true, updatable = false, length = 36)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;
    
    @Column(name = "product_id", length = 36)
    private String productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "shipping_address", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String shippingAddress;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
}
