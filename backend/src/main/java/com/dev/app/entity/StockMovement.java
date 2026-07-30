package com.dev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA que representa un movimiento de stock.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_stock_product", columnList = "product_id"),
    @Index(name = "idx_stock_date", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "movement_id", nullable = false, unique = true, updatable = false, length = 36)
    private String movementId;
    
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "previous_stock")
    private Integer previousStock;
    
    @Column(name = "new_stock")
    private Integer newStock;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal unitPrice;
    
    @Column(name = "reference_id", length = 100)
    private String referenceId; // Order ID, Purchase ID, etc.
    
    @Column(name = "reference_type", length = 50)
    private String referenceType; // ORDER, PURCHASE, ADJUSTMENT
    
    @Column(length = 500)
    private String notes;
    
    @Column(name = "created_by", length = 36)
    private String createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    public enum MovementType {
        IN,      // Entrada de stock
        OUT,     // Salida de stock
        SALE,    // Venta
        RETURN,  // Devolución
        ADJUSTMENT // Ajuste de inventario
    }
}
