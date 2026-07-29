package com.dev.app.dto;

import com.dev.app.entity.OrderEventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa un evento de orden recibido desde Kafka.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {
    
    @NotBlank(message = "orderId es requerido")
    @JsonProperty("orderId")
    private String orderId;
    
    @NotBlank(message = "customerId es requerido")
    @JsonProperty("customerId")
    private String customerId;
    
    @NotNull(message = "eventType es requerido")
    @JsonProperty("eventType")
    private OrderEventType eventType;
    
    @NotNull(message = "totalAmount es requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "totalAmount debe ser mayor o igual a 0")
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "currency es requerido")
    @Size(min = 3, max = 3, message = "currency debe tener 3 caracteres (ISO 4217)")
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("items")
    private List<OrderItemDto> items;
    
    @JsonProperty("shippingAddress")
    private ShippingAddressDto shippingAddress;
    
    @JsonProperty("reason")
    private String reason;
    
    @NotNull(message = "metadata es requerido")
    @JsonProperty("metadata")
    private EventMetadataDto metadata;
    
}
