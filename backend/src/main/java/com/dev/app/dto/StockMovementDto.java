package com.dev.app.dto;

import com.dev.app.entity.StockMovement.MovementType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO para movimiento de stock.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto {
    
    private String movementId;
    
    @NotBlank(message = "El producto es requerido")
    private String productId;
    
    @NotNull(message = "El tipo de movimiento es requerido")
    private MovementType type;
    
    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;
    
    private Integer previousStock;
    private Integer newStock;
    
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal unitPrice;
    
    private String referenceId;
    private String referenceType;
    
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
    
    private String createdBy;
    private Instant createdAt;
    
    // Datos adicionales del producto para la respuesta
    private String productName;
}
