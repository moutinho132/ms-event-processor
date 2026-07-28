package com.dev.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que representa un item dentro de una orden.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDto {
    
    @NotBlank(message = "productId es requerido")
    @JsonProperty("productId")
    private String productId;
    
    @NotBlank(message = "productName es requerido")
    @JsonProperty("productName")
    private String productName;
    
    @NotNull(message = "quantity es requerido")
    @Min(value = 1, message = "quantity debe ser al menos 1")
    @JsonProperty("quantity")
    private Integer quantity;
    
    @NotNull(message = "unitPrice es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice debe ser mayor o igual a 0")
    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;
    
}
