package com.dev.app.processor;

import com.dev.app.config.BaseIntegrationTest;
import com.dev.app.dto.EventMetadataDto;
import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.OrderItemDto;
import com.dev.app.dto.ShippingAddressDto;
import com.dev.app.entity.Order;
import com.dev.app.entity.OrderEventType;
import com.dev.app.entity.OrderStatus;
import com.dev.app.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para el procesamiento de eventos de orden.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
class OrderProcessingIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private SqsClient sqsClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String queueUrl;
    
    @BeforeEach
    void setUp() {
        // Crear cola SQS para tests
        String queueName = "test-ordenes-procesadas-queue";
        try {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .build();
            sqsClient.createQueue(createQueueRequest);
            queueUrl = String.format("http://localhost:4566/000000000000/%s", queueName);
        } catch (Exception e) {
            // La cola ya existe
            queueUrl = String.format("http://localhost:4566/000000000000/%s", queueName);
        }
        
        // Limpiar base de datos
        orderRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Debe procesar evento CREATED exitosamente")
    void shouldProcessCreatedEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CREATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        // When
        kafkaTemplate.send("orders-created", orderId, eventJson);
        
        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Order savedOrder = orderRepository.findByOrderId(orderId).orElse(null);
                    assertThat(savedOrder).isNotNull();
                    assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(savedOrder.getCustomerId()).isEqualTo(event.getCustomerId());
                });
    }
    
    @Test
    @DisplayName("Debe procesar evento UPDATED exitosamente")
    void shouldProcessUpdatedEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        
        // Crear orden inicial
        OrderEvent createEvent = createOrderEvent(orderId, OrderEventType.CREATED);
        String createEventJson = objectMapper.writeValueAsString(createEvent);
        kafkaTemplate.send("orders-created", orderId, createEventJson);
        
        // Esperar a que se procese
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> orderRepository.findByOrderId(orderId).isPresent());
        
        // When - Enviar evento de actualización
        OrderEvent updateEvent = createOrderEvent(orderId, OrderEventType.UPDATED);
        updateEvent.setTotalAmount(new BigDecimal("200.00"));
        String updateEventJson = objectMapper.writeValueAsString(updateEvent);
        kafkaTemplate.send("orders-updates", orderId, updateEventJson);
        
        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Order updatedOrder = orderRepository.findByOrderId(orderId).orElse(null);
                    assertThat(updatedOrder).isNotNull();
                    assertThat(updatedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
                });
    }
    
    @Test
    @DisplayName("Debe procesar evento CANCELLED exitosamente")
    void shouldProcessCancelledEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        
        // Crear orden inicial
        OrderEvent createEvent = createOrderEvent(orderId, OrderEventType.CREATED);
        String createEventJson = objectMapper.writeValueAsString(createEvent);
        kafkaTemplate.send("orders-created", orderId, createEventJson);
        
        // Esperar a que se procese
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> orderRepository.findByOrderId(orderId).isPresent());
        
        // When - Enviar evento de cancelación
        OrderEvent cancelEvent = createOrderEvent(orderId, OrderEventType.CANCELLED);
        String cancelEventJson = objectMapper.writeValueAsString(cancelEvent);
        kafkaTemplate.send("orders-cancelled", orderId, cancelEventJson);
        
        // Then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Order cancelledOrder = orderRepository.findByOrderId(orderId).orElse(null);
                    assertThat(cancelledOrder).isNotNull();
                    assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                    assertThat(cancelledOrder.getCancelledAt()).isNotNull();
                });
    }
    
    @Test
    @DisplayName("Debe ser idempotente - procesar el mismo evento CREATED dos veces sin duplicar")
    void shouldBeIdempotentForCreatedEvent() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CREATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        // When - Enviar el mismo evento dos veces
        kafkaTemplate.send("orders-created", orderId, eventJson);
        kafkaTemplate.send("orders-created", orderId, eventJson);
        
        // Then - Solo debe existir una orden
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<Order> orders = orderRepository.findAll().stream()
                            .filter(o -> o.getOrderId().equals(orderId))
                            .toList();
                    assertThat(orders).hasSize(1);
                });
    }
    
    // Helper methods
    
    private OrderEvent createOrderEvent(String orderId, OrderEventType eventType) {
        return OrderEvent.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID().toString())
                .eventType(eventType)
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
                .shippingAddress(ShippingAddressDto.builder()
                    .street("Test Street")
                    .city("Test City")
                    .state("Test State")
                    .postalCode("12345")
                    .country("Test Country")
                    .build())
                .metadata(EventMetadataDto.builder()
                    .source("test-service")
                    .correlationId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .build())
                .build();
    }
    
}
