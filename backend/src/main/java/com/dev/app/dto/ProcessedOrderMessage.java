package com.dev.app.dto;

import com.dev.app.entity.Order;
import com.dev.app.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO que representa un mensaje procesado enviado a SQS.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrderMessage {
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("status")
    private OrderStatus status;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("itemCount")
    private Integer itemCount;
    
    @JsonProperty("processedAt")
    private Instant processedAt;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("version")
    private String version;
    
    /**
     * Método factory para crear ProcessedOrderMessage desde una entidad Order.
     * 
     * @param order Entidad Order
     * @return ProcessedOrderMessage
     */
    public static ProcessedOrderMessage fromOrder(Order order) {
        return ProcessedOrderMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .itemCount(order.getQuantity())
                .processedAt(order.getProcessedAt())
                .correlationId(order.getOrderId()) // Usar orderId como correlationId si no hay otro
                .version("1.0")
                .build();
    }
    
    /**
     * Método factory para crear ProcessedOrderMessage desde Order y correlationId.
     * 
     * @param order Entidad Order
     * @param correlationId ID de correlación del evento
     * @return ProcessedOrderMessage
     */
    public static ProcessedOrderMessage fromOrder(Order order, String correlationId) {
        return ProcessedOrderMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .itemCount(order.getQuantity())
                .processedAt(order.getProcessedAt())
                .correlationId(correlationId)
                .version("1.0")
                .build();
    }
    
}
