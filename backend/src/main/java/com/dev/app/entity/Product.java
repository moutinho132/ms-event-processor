package com.dev.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA que representa un producto del sistema.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_product_id", columnList = "product_id"),
    @Index(name = "idx_product_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false, unique = true, updatable = false, length = 36)
    private String productId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @Column(length = 3)
    private String currency = "USD";
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
    
}
