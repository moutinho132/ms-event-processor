package com.dev.app.processor;

import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.ProcessedOrderMessage;
import com.dev.app.dto.SqsMessage;
import com.dev.app.entity.Order;
import com.dev.app.entity.OrderAudit;
import com.dev.app.entity.OrderEventType;
import com.dev.app.entity.OrderStatus;
import com.dev.app.exception.EventProcessingException;
import com.dev.app.producer.SqsProducer;
import com.dev.app.repository.OrderAuditRepository;
import com.dev.app.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Procesador principal de eventos de orden.
 * 
 * Orquesta la validación, procesamiento, persistencia y reenvío de eventos de orden.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProcessor {
    
    private final OrderEventValidator validator;
    private final OrderRepository orderRepository;
    private final OrderAuditRepository orderAuditRepository;
    private final SqsProducer sqsProducer;
    private final ObjectMapper objectMapper;
    
    /**
     * Procesa un evento de orden creada.
     * 
     * @param eventJson JSON del evento
     */
    @Transactional
    public void processCreatedEvent(String eventJson) {
        log.debug("Procesando evento de orden creada");
        
        try {
            OrderEvent event = objectMapper.readValue(eventJson, OrderEvent.class);
            processEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializando evento de orden creada: {}", e.getMessage(), e);
            throw new EventProcessingException("Error deserializando evento", "UNKNOWN", "CREATED", e);
        }
    }
    
    /**
     * Procesa un evento de actualización de orden.
     * 
     * @param eventJson JSON del evento
     */
    @Transactional
    public void processUpdatedEvent(String eventJson) {
        log.debug("Procesando evento de actualización de orden");
        
        try {
            OrderEvent event = objectMapper.readValue(eventJson, OrderEvent.class);
            processEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializando evento de actualización: {}", e.getMessage(), e);
            throw new EventProcessingException("Error deserializando evento", "UNKNOWN", "UPDATED", e);
        }
    }
    
    /**
     * Procesa un evento de cancelación de orden.
     * 
     * @param eventJson JSON del evento
     */
    @Transactional
    public void processCancelledEvent(String eventJson) {
        log.debug("Procesando evento de cancelación de orden");
        
        try {
            OrderEvent event = objectMapper.readValue(eventJson, OrderEvent.class);
            processEvent(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializando evento de cancelación: {}", e.getMessage(), e);
            throw new EventProcessingException("Error deserializando evento", "UNKNOWN", "CANCELLED", e);
        }
    }
    
    /**
     * Procesa un evento de orden.
     * 
     * @param event Evento a procesar
     */
    @Transactional
    public void processEvent(OrderEvent event) {
        long startTime = System.currentTimeMillis();
        
        log.info("Procesando evento de orden {} - Tipo: {}", event.getOrderId(), event.getEventType());
        
        try {
            // Validar el evento
            validator.validate(event);
            
            // Procesar según el tipo de evento
            Order order = switch (event.getEventType()) {
                case CREATED -> handleOrderCreated(event);
                case UPDATED -> handleOrderUpdated(event);
                case CANCELLED -> handleOrderCancelled(event);
            };
            
            // Reenviar mensaje a SQS
            sqsProducer.sendMessage(SqsMessage.fromOrder(order));
            
            // Registrar auditoría exitosa
            long processingTime = System.currentTimeMillis() - startTime;
            saveAuditLog(event, order, processingTime, true, null);
            
            log.info("Evento de orden {} procesado exitosamente en {}ms", 
                    event.getOrderId(), processingTime);
            
        } catch (Exception e) {
            // Registrar auditoría de error
            long processingTime = System.currentTimeMillis() - startTime;
            saveAuditLog(event, null, processingTime, false, e.getMessage());
            
            log.error("Error procesando evento de orden {}: {}", event.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Maneja un evento de orden creada.
     * 
     * @param event Evento de orden creada
     * @return Orden creada
     */
    private Order handleOrderCreated(OrderEvent event) {
        log.debug("Creando nueva orden: {}", event.getOrderId());
        
        // Verificar idempotencia - si ya existe, retornar la existente
        if (orderRepository.existsByOrderId(event.getOrderId())) {
            log.info("Orden {} ya existe, retornando orden existente (idempotencia)", event.getOrderId());
            return orderRepository.findByOrderId(event.getOrderId())
                    .orElseThrow(() -> new EventProcessingException(
                        "Error recuperando orden existente", 
                        event.getOrderId(), 
                        "CREATED"
                    ));
        }
        
        // Crear nueva orden
        Order order = Order.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .totalAmount(event.getTotalAmount())
                .currency(event.getCurrency())
                .status(OrderStatus.PENDING)
                .processedAt(Instant.now())
                .build();
        
        // Si hay items, usar el primero para campos simples
        if (event.getItems() != null && !event.getItems().isEmpty()) {
            var firstItem = event.getItems().get(0);
            order.setProductId(firstItem.getProductId());
            order.setProductName(firstItem.getProductName());
            order.setQuantity(firstItem.getQuantity());
            order.setUnitPrice(firstItem.getUnitPrice());
        }
        
        // Si hay dirección de envío, guardarla como JSON
        if (event.getShippingAddress() != null) {
            try {
                order.setShippingAddress(objectMapper.writeValueAsString(event.getShippingAddress()));
            } catch (JsonProcessingException e) {
                log.warn("Error serializando dirección de envío: {}", e.getMessage());
            }
        }
        
        // Guardar orden
        order = orderRepository.save(order);
        
        log.info("Orden {} creada exitosamente con status PENDING", event.getOrderId());
        
        return order;
    }
    
    /**
     * Maneja un evento de actualización de orden.
     * 
     * @param event Evento de actualización
     * @return Orden actualizada
     */
    private Order handleOrderUpdated(OrderEvent event) {
        log.debug("Actualizando orden: {}", event.getOrderId());
        
        // Buscar orden existente
        Order order = orderRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new EventProcessingException(
                    String.format("Orden %s no encontrada para actualización", event.getOrderId()),
                    event.getOrderId(),
                    "UPDATED"
                ));
        
        // Actualizar campos
        if (event.getTotalAmount() != null) {
            order.setTotalAmount(event.getTotalAmount());
        }
        
        if (event.getCurrency() != null) {
            order.setCurrency(event.getCurrency());
        }
        
        // Actualizar items si están presentes
        if (event.getItems() != null && !event.getItems().isEmpty()) {
            var firstItem = event.getItems().get(0);
            order.setProductId(firstItem.getProductId());
            order.setProductName(firstItem.getProductName());
            order.setQuantity(firstItem.getQuantity());
            order.setUnitPrice(firstItem.getUnitPrice());
        }
        
        // Actualizar dirección de envío si está presente
        if (event.getShippingAddress() != null) {
            try {
                order.setShippingAddress(objectMapper.writeValueAsString(event.getShippingAddress()));
            } catch (JsonProcessingException e) {
                log.warn("Error serializando dirección de envío: {}", e.getMessage());
            }
        }
        
        // Actualizar timestamp de procesamiento
        order.setProcessedAt(Instant.now());
        
        // Guardar orden actualizada
        order = orderRepository.save(order);
        
        log.info("Orden {} actualizada exitosamente", event.getOrderId());
        
        return order;
    }
    
    /**
     * Maneja un evento de cancelación de orden.
     * 
     * @param event Evento de cancelación
     * @return Orden cancelada
     */
    private Order handleOrderCancelled(OrderEvent event) {
        log.debug("Cancelando orden: {}", event.getOrderId());
        
        // Buscar orden existente
        Order order = orderRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new EventProcessingException(
                    String.format("Orden %s no encontrada para cancelación", event.getOrderId()),
                    event.getOrderId(),
                    "CANCELLED"
                ));
        
        // Actualizar status a CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setProcessedAt(Instant.now());
        
        // Guardar orden cancelada
        order = orderRepository.save(order);
        
        log.info("Orden {} cancelada exitosamente", event.getOrderId());
        
        return order;
    }
    
    /**
     * Guarda un registro de auditoría.
     * 
     * @param event Evento procesado
     * @param order Orden procesada (puede ser null en caso de error)
     * @param processingTimeMs Tiempo de procesamiento en milisegundos
     * @param success Indica si el procesamiento fue exitoso
     * @param errorMessage Mensaje de error (null si fue exitoso)
     */
    private void saveAuditLog(OrderEvent event, Order order, long processingTimeMs, 
                              boolean success, String errorMessage) {
        try {
            OrderAudit audit = OrderAudit.builder()
                    .orderId(event.getOrderId())
                    .eventType(event.getEventType())
                    .oldStatus(order != null ? order.getStatus().name() : null)
                    .newStatus(order != null ? order.getStatus().name() : null)
                    .correlationId(event.getMetadata() != null ? event.getMetadata().getCorrelationId() : null)
                    .processingTimeMs(processingTimeMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();
            
            orderAuditRepository.save(audit);
            
            log.debug("Registro de auditoría guardado para orden {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error guardando registro de auditoría: {}", e.getMessage(), e);
        }
    }
    
}
