package com.dev.app.dto;

import com.dev.app.entity.Order;
import com.dev.app.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO que representa un mensaje para enviar a SQS.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqsMessage {
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private OrderStatus status;
    
    @JsonProperty("processedAt")
    private Instant processedAt;
    
    @JsonProperty("source")
    private String source;
    
    /**
     * Método factory para crear SqsMessage desde una entidad Order.
     * 
     * @param order Entidad Order
     * @return SqsMessage
     */
    public static SqsMessage fromOrder(Order order) {
        return SqsMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .processedAt(order.getProcessedAt())
                .source("ms-event-processor")
                .build();
    }
    
    /**
     * Método factory para crear SqsMessage desde Order con source personalizado.
     * 
     * @param order Entidad Order
     * @param source Origen del mensaje
     * @return SqsMessage
     */
    public static SqsMessage fromOrder(Order order, String source) {
        return SqsMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .processedAt(order.getProcessedAt())
                .source(source)
                .build();
    }
    
}
