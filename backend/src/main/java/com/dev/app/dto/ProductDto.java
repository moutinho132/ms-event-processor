package com.dev.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para Producto.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private String productId;
    
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String name;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;
    
    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal price;
    
    private String currency;
    
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
    
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String category;
    
    @Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
    private String imageUrl;
    
    private Boolean active;
}
