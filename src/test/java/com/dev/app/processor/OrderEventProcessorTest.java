package com.dev.app.processor;

import com.dev.app.dto.EventMetadataDto;
import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.OrderItemDto;
import com.dev.app.dto.SqsMessage;
import com.dev.app.entity.Order;
import com.dev.app.entity.OrderEventType;
import com.dev.app.entity.OrderStatus;
import com.dev.app.exception.EventProcessingException;
import com.dev.app.exception.ValidationException;
import com.dev.app.producer.SqsProducer;
import com.dev.app.repository.OrderAuditRepository;
import com.dev.app.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para OrderEventProcessor.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OrderEventProcessorTest {
    
    @Mock
    private OrderEventValidator validator;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderAuditRepository orderAuditRepository;
    
    @Mock
    private SqsProducer sqsProducer;
    
    private ObjectMapper objectMapper;
    
    private OrderEventProcessor processor;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        processor = new OrderEventProcessor(validator, orderRepository, orderAuditRepository, sqsProducer, objectMapper);
    }
    
    @Test
    @DisplayName("Debe procesar evento CREATED exitosamente")
    void shouldProcessCreatedEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CREATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        Order savedOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .customerId(event.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(event.getTotalAmount())
                .currency(event.getCurrency())
                .processedAt(Instant.now())
                .build();
        
        when(orderRepository.existsByOrderId(orderId)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        doNothing().when(sqsProducer).sendMessage(any(SqsMessage.class));
        
        // When
        processor.processCreatedEvent(eventJson);
        
        // Then
        verify(validator).validate(event);
        verify(orderRepository).existsByOrderId(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(sqsProducer).sendMessage(any(SqsMessage.class));
        verify(orderAuditRepository).save(any());
    }
    
    @Test
    @DisplayName("Debe ser idempotente - retornar orden existente si ya existe")
    void shouldBeIdempotentForExistingOrder() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CREATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        Order existingOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();
        
        when(orderRepository.existsByOrderId(orderId)).thenReturn(true);
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        doNothing().when(sqsProducer).sendMessage(any(SqsMessage.class));
        
        // When
        processor.processCreatedEvent(eventJson);
        
        // Then
        verify(orderRepository).existsByOrderId(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(sqsProducer).sendMessage(any(SqsMessage.class));
    }
    
    @Test
    @DisplayName("Debe procesar evento UPDATED exitosamente")
    void shouldProcessUpdatedEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.UPDATED);
        event.setTotalAmount(new BigDecimal("200.00"));
        String eventJson = objectMapper.writeValueAsString(event);
        
        Order existingOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        
        Order updatedOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("200.00"))
                .processedAt(Instant.now())
                .build();
        
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        doNothing().when(sqsProducer).sendMessage(any(SqsMessage.class));
        
        // When
        processor.processUpdatedEvent(eventJson);
        
        // Then
        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(sqsProducer).sendMessage(any(SqsMessage.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción si orden no existe para UPDATE")
    void shouldThrowExceptionWhenOrderNotFoundForUpdate() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.UPDATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> processor.processUpdatedEvent(eventJson))
                .isInstanceOf(EventProcessingException.class)
                .hasMessageContaining("no encontrada para actualización");
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(sqsProducer, never()).sendMessage(any(SqsMessage.class));
    }
    
    @Test
    @DisplayName("Debe procesar evento CANCELLED exitosamente")
    void shouldProcessCancelledEventSuccessfully() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CANCELLED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        Order existingOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();
        
        Order cancelledOrder = Order.builder()
                .id(1L)
                .orderId(orderId)
                .status(OrderStatus.CANCELLED)
                .cancelledAt(Instant.now())
                .processedAt(Instant.now())
                .build();
        
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);
        doNothing().when(sqsProducer).sendMessage(any(SqsMessage.class));
        
        // When
        processor.processCancelledEvent(eventJson);
        
        // Then
        verify(orderRepository).findByOrderId(orderId);
        verify(orderRepository).save(any(Order.class));
        verify(sqsProducer).sendMessage(any(SqsMessage.class));
    }
    
    @Test
    @DisplayName("Debe lanzar excepción si orden no existe para CANCEL")
    void shouldThrowExceptionWhenOrderNotFoundForCancel() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CANCELLED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> processor.processCancelledEvent(eventJson))
                .isInstanceOf(EventProcessingException.class)
                .hasMessageContaining("no encontrada para cancelación");
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(sqsProducer, never()).sendMessage(any(SqsMessage.class));
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException si la validación falla")
    void shouldThrowValidationExceptionWhenValidationFails() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderEvent event = createOrderEvent(orderId, OrderEventType.CREATED);
        String eventJson = objectMapper.writeValueAsString(event);
        
        doThrow(new ValidationException(orderId, "CREATED", List.of("orderId es requerido")))
                .when(validator).validate(event);
        
        // When/Then
        assertThatThrownBy(() -> processor.processCreatedEvent(eventJson))
                .isInstanceOf(ValidationException.class);
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(sqsProducer, never()).sendMessage(any(SqsMessage.class));
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
                .metadata(EventMetadataDto.builder()
                    .source("test-service")
                    .correlationId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .build())
                .build();
    }
    
}
