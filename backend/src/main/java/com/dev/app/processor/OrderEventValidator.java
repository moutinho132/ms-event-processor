package com.dev.app.processor;

import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.OrderItemDto;
import com.dev.app.entity.OrderEventType;
import com.dev.app.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador de eventos de orden.
 * 
 * Valida la estructura y contenido de los eventos de orden recibidos desde Kafka.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class OrderEventValidator {
    
    /**
     * Valida un evento de orden.
     * 
     * @param event Evento a validar
     * @throws ValidationException si la validación falla
     */
    public void validate(OrderEvent event) {
        log.debug("Validando evento de orden: {}", event.getOrderId());
        
        List<String> errors = new ArrayList<>();
        
        // Validar orderId
        if (event.getOrderId() == null || event.getOrderId().isBlank()) {
            errors.add("orderId es requerido");
        }
        
        // Validar customerId
        if (event.getCustomerId() == null || event.getCustomerId().isBlank()) {
            errors.add("customerId es requerido");
        }
        
        // Validar eventType
        if (event.getEventType() == null) {
            errors.add("eventType es requerido");
        } else {
            try {
                OrderEventType.valueOf(event.getEventType().name());
            } catch (IllegalArgumentException e) {
                errors.add("eventType debe ser uno de: CREATED, UPDATED, CANCELLED");
            }
        }
        
        // Validar totalAmount
        if (event.getTotalAmount() == null) {
            errors.add("totalAmount es requerido");
        } else if (event.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            errors.add("totalAmount debe ser mayor o igual a 0");
        }
        
        // Validar currency
        if (event.getCurrency() == null || event.getCurrency().isBlank()) {
            errors.add("currency es requerido");
        } else if (event.getCurrency().length() != 3) {
            errors.add("currency debe tener 3 caracteres (ISO 4217)");
        }
        
        // Validar items si están presentes
        if (event.getItems() != null && !event.getItems().isEmpty()) {
            for (int i = 0; i < event.getItems().size(); i++) {
                OrderItemDto item = event.getItems().get(i);
                if (item.getQuantity() != null && item.getQuantity() < 1) {
                    errors.add(String.format("items[%d].quantity debe ser al menos 1", i));
                }
                if (item.getUnitPrice() != null && item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                    errors.add(String.format("items[%d].unitPrice debe ser mayor o igual a 0", i));
                }
            }
        }
        
        // Validar metadata
        if (event.getMetadata() == null) {
            errors.add("metadata es requerido");
        } else {
            if (event.getMetadata().getSource() == null || event.getMetadata().getSource().isBlank()) {
                errors.add("metadata.source es requerido");
            }
            if (event.getMetadata().getCorrelationId() == null || event.getMetadata().getCorrelationId().isBlank()) {
                errors.add("metadata.correlationId es requerido");
            }
            if (event.getMetadata().getTimestamp() == null) {
                errors.add("metadata.timestamp es requerido");
            }
        }
        
        // Si hay errores, lanzar excepción
        if (!errors.isEmpty()) {
            log.error("Error de validación para orden {}: {}", event.getOrderId(), errors);
            throw new ValidationException(
                event.getOrderId(),
                event.getEventType() != null ? event.getEventType().name() : "UNKNOWN",
                "Error de validación del evento",
                errors
            );
        }
        
        log.debug("Evento de orden {} validado exitosamente", event.getOrderId());
    }
    
}
