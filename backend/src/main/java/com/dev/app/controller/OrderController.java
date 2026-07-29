package com.dev.app.controller;

import com.dev.app.dto.EventMetadataDto;
import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.OrderItemDto;
import com.dev.app.dto.ShippingAddressDto;
import com.dev.app.entity.Order;
import com.dev.app.entity.OrderAudit;
import com.dev.app.entity.OrderEventType;
import com.dev.app.entity.OrderStatus;
import com.dev.app.exception.EventProcessingException;
import com.dev.app.processor.OrderEventProcessor;
import com.dev.app.producer.SqsProducer;
import com.dev.app.repository.OrderAuditRepository;
import com.dev.app.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Controlador REST para gestión de órdenes.
 * 
 * Replica la misma funcionalidad que los scripts de shell:
 * - test-producer.sh: Enviar mensajes a Kafka
 * - check-orders.sh: Verificar base de datos
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gestión de órdenes - Equivalente a scripts de shell")
public class OrderController {
    
    private final OrderEventProcessor orderEventProcessor;
    private final OrderRepository orderRepository;
    private final OrderAuditRepository orderAuditRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // ==================== EQUIVALENTE A test-producer.sh ====================
    
    /**
     * Envía un mensaje de prueba a Kafka (como test-producer.sh).
     * 
     * Genera UUIDs automáticamente y envía el mensaje al tópico de Kafka.
     * El mensaje será procesado de manera asíncrona por los consumers.
     * 
     * @return Detalles del mensaje enviado
     */
    @PostMapping("/kafka/send-test")
    @Operation(
        summary = "Enviar mensaje de prueba a Kafka (equivalente a test-producer.sh)",
        description = "Genera y envía un mensaje de prueba con datos aleatorios al tópico orders-created"
    )
    @ApiResponse(responseCode = "200", description = "Mensaje enviado exitosamente")
    public ResponseEntity<Map<String, Object>> sendTestMessageToKafka() throws Exception {
        log.info("📤 Enviando mensaje de prueba a Kafka vía API REST");
        
        // Generar UUIDs automáticamente (como en test-producer.sh)
        String orderId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        
        // Crear mensaje JSON (igual que test-producer.sh)
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .customerId(customerId)
                .eventType(OrderEventType.CREATED)
                .totalAmount(new BigDecimal("150.50"))
                .currency("USD")
                .items(List.of(
                    OrderItemDto.builder()
                        .productId(productId)
                        .productName("Test Product")
                        .quantity(2)
                        .unitPrice(new BigDecimal("75.25"))
                        .build()
                ))
                .shippingAddress(ShippingAddressDto.builder()
                    .street("123 Main St")
                    .city("New York")
                    .state("NY")
                    .postalCode("10001")
                    .country("USA")
                    .build())
                .metadata(EventMetadataDto.builder()
                    .source("api-rest-test")
                    .correlationId(correlationId)
                    .timestamp(Instant.now())
                    .build())
                .build();
        
        // Enviar a Kafka (como hace test-producer.sh)
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send("orders-created", orderId, eventJson);
        
        log.info("✅ Mensaje enviado exitosamente a Kafka - Order ID: {}", orderId);
        
        // Retornar respuesta con detalles (como muestra test-producer.sh)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Mensaje enviado exitosamente a Kafka");
        response.put("topic", "orders-created");
        response.put("orderId", orderId);
        response.put("customerId", customerId);
        response.put("event", event);
        response.put("note", "El mensaje será procesado de manera asíncrona. Usa GET /api/v1/orders/" + orderId + " para verificar.");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Envía un mensaje custom a Kafka.
     * 
     * @param event Evento de orden a enviar
     * @return Detalles del mensaje enviado
     */
    @PostMapping("/kafka/send")
    @Operation(
        summary = "Enviar mensaje custom a Kafka",
        description = "Envía un mensaje custom al tópico correspondiente según el eventType"
    )
    @ApiResponse(responseCode = "200", description = "Mensaje enviado exitosamente")
    public ResponseEntity<Map<String, Object>> sendCustomMessageToKafka(@Valid @RequestBody OrderEvent event) throws Exception {
        log.info("📤 Enviando mensaje custom a Kafka - Order ID: {}, Type: {}", 
                 event.getOrderId(), event.getEventType());
        
        // Determinar tópico según eventType
        String topic = switch (event.getEventType()) {
            case CREATED -> "orders-created";
            case UPDATED -> "orders-updates";
            case CANCELLED -> "orders-cancelled";
        };
        
        // Enviar a Kafka
        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, event.getOrderId(), eventJson);
        
        log.info("✅ Mensaje enviado a tópico {} - Order ID: {}", topic, event.getOrderId());
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Mensaje enviado exitosamente a Kafka");
        response.put("topic", topic);
        response.put("orderId", event.getOrderId());
        response.put("eventType", event.getEventType());
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== EQUIVALENTE A check-orders.sh ====================
    
    /**
     * Verifica todas las órdenes en la base de datos (como check-orders.sh).
     * 
     * @return Lista de todas las órdenes
     */
    @GetMapping("/database/check")
    @Operation(
        summary = "Verificar órdenes en BD (equivalente a check-orders.sh)",
        description = "Lista todas las órdenes de la base de datos"
    )
    @ApiResponse(responseCode = "200", description = "Lista de órdenes")
    public ResponseEntity<Map<String, Object>> checkOrdersInDatabase() {
        log.info("🔍 Verificando órdenes en la base de datos");
        
        List<Order> orders = orderRepository.findAll();
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("count", orders.size());
        response.put("orders", orders);
        
        log.info("✅ Se encontraron {} órdenes", orders.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verifica una orden específica con su auditoría (como check-orders.sh con orderId).
     * 
     * @param orderId ID de la orden a verificar
     * @return Detalles de la orden y su auditoría
     */
    @GetMapping("/database/check/{orderId}")
    @Operation(
        summary = "Verificar orden específica con auditoría",
        description = "Obtiene la orden y su historial de auditoría"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<Map<String, Object>> checkSpecificOrder(
            @Parameter(description = "ID de la orden") @PathVariable String orderId) {
        
        log.info("🔍 Verificando orden: {}", orderId);
        
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        
        if (orderOpt.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "NOT_FOUND");
            response.put("message", "Orden no encontrada");
            response.put("orderId", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Order order = orderOpt.get();
        List<OrderAudit> auditLog = orderAuditRepository.findByOrderIdOrderByProcessedAtDesc(orderId);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("order", order);
        response.put("auditLog", auditLog);
        response.put("auditCount", auditLog.size());
        
        log.info("✅ Orden {} encontrada con {} registros de auditoría", orderId, auditLog.size());
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== PROCESAMIENTO DIRECTO (SIN KAFKA) ====================
    
    /**
     * Procesa un evento de orden directamente (sin Kafka).
     * 
     * Este endpoint procesa el evento de manera síncrona y retorna el resultado.
     * Es útil cuando no quieres esperar el procesamiento asíncrono de Kafka.
     * 
     * @param event Evento de orden a procesar
     * @return Orden procesada
     */
    @PostMapping("/process")
    @Operation(
        summary = "Procesar evento de orden directamente (sin Kafka)",
        description = "Procesa el evento de manera síncrona y retorna la orden procesada"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden procesada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    public ResponseEntity<Map<String, Object>> processOrderDirectly(@Valid @RequestBody OrderEvent event) {
        log.info("⚡ Procesando orden directamente (sin Kafka) - Order ID: {}", event.getOrderId());
        
        long startTime = System.currentTimeMillis();
        
        orderEventProcessor.processEvent(event);
        
        Order order = orderRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new EventProcessingException(
                    "Orden no encontrada después del procesamiento",
                    event.getOrderId(),
                    event.getEventType().name()
                ));
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Orden procesada exitosamente");
        response.put("processingTimeMs", processingTime);
        response.put("order", order);
        
        log.info("✅ Orden {} procesada en {}ms", event.getOrderId(), processingTime);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una orden de prueba rápidamente.
     * 
     * @return Orden creada
     */
    @PostMapping("/create-test")
    @Operation(
        summary = "Crear orden de prueba rápidamente",
        description = "Crea una orden con datos de prueba y la procesa directamente"
    )
    @ApiResponse(responseCode = "201", description = "Orden creada exitosamente")
    public ResponseEntity<Map<String, Object>> createTestOrder() throws Exception {
        log.info("⚡ Creando orden de prueba");
        
        // Generar datos de prueba
        String orderId = UUID.randomUUID().toString();
        
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .eventType(OrderEventType.CREATED)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .items(List.of(
                    OrderItemDto.builder()
                        .productId(UUID.randomUUID().toString())
                        .productName("Test Product")
                        .quantity(1)
                        .unitPrice(new BigDecimal("100.00"))
                        .build()
                ))
                .metadata(EventMetadataDto.builder()
                    .source("api-rest-quick-test")
                    .correlationId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .build())
                .build();
        
        // Procesar directamente
        orderEventProcessor.processEvent(event);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EventProcessingException(
                    "Orden no encontrada después de la creación",
                    orderId,
                    "CREATED"
                ));
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Orden de prueba creada exitosamente");
        response.put("order", order);
        
        log.info("✅ Orden de prueba {} creada", orderId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // ==================== OPERACIONES CRUD BÁSICAS ====================
    
    /**
     * Obtiene una orden por su ID.
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Obtener orden por ID")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista todas las órdenes con filtros opcionales.
     */
    @GetMapping
    @Operation(summary = "Listar órdenes")
    public ResponseEntity<List<Order>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerId) {
        
        List<Order> orders;
        
        if (customerId != null && status != null) {
            orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        } else if (customerId != null) {
            orders = orderRepository.findByCustomerId(customerId);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Cancela una orden y envía el evento a Kafka.
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancelar orden y enviar a Kafka")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden cancelada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "400", description = "La orden no puede ser cancelada")
    })
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) String reason) {
        
        log.info("❌ Cancelando orden: {}", orderId);
        
        try {
            Order order = orderEventProcessor.cancelOrderAndSendToKafka(orderId, reason);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Orden cancelada exitosamente");
            response.put("order", order);
            response.put("kafkaEvent", "Enviado a orders-cancelled topic");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("orderId", orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Actualiza el estado de una orden.
     */
    @PostMapping("/{orderId}/status")
    @Operation(summary = "Actualizar estado de la orden")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Transición de estado inválida")
    })
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        
        log.info("📝 Actualizando estado de orden {} a {}", orderId, status);
        
        try {
            Order order = orderEventProcessor.transitionOrderStatus(orderId, status);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Estado actualizado exitosamente");
            response.put("order", order);
            
            if (status == OrderStatus.DELIVERED) {
                response.put("snsNotification", "Email enviado al cliente");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Agrega items a una orden existente.
     */
    @PostMapping("/{orderId}/items")
    @Operation(summary = "Agregar items a una orden")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Items agregados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<Map<String, Object>> addItemsToOrder(
            @PathVariable String orderId,
            @RequestBody String itemsJson) {
        
        log.info("📦 Agregando items a orden: {}", orderId);
        
        try {
            Order order = orderEventProcessor.addItemsToOrder(orderId, itemsJson);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Items agregados exitosamente");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Obtiene las transiciones de estado válidas para una orden.
     */
    @GetMapping("/{orderId}/valid-transitions")
    @Operation(summary = "Obtener transiciones de estado válidas")
    public ResponseEntity<Map<String, Object>> getValidTransitions(@PathVariable String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(order -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("currentStatus", order.getStatus());
                    response.put("validTransitions", getValidTransitionsForStatus(order.getStatus()));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtiene las transiciones válidas para un estado.
     */
    private java.util.List<OrderStatus> getValidTransitionsForStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> java.util.List.of(OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.CANCELLED);
            case CONFIRMED -> java.util.List.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED);
            case PROCESSING -> java.util.List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case SHIPPED -> java.util.List.of(OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED, REFUNDED -> java.util.List.of();
        };
    }
    
}
