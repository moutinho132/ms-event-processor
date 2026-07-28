package com.dev.app.processor;

import com.dev.app.dto.EventMetadataDto;
import com.dev.app.dto.OrderEvent;
import com.dev.app.dto.OrderItemDto;
import com.dev.app.entity.OrderEventType;
import com.dev.app.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para OrderEventValidator.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
class OrderEventValidatorTest {
    
    private OrderEventValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new OrderEventValidator();
    }
    
    @Test
    @DisplayName("Debe validar evento exitosamente cuando todos los campos son válidos")
    void shouldValidateEventSuccessfullyWhenAllFieldsAreValid() {
        // Given
        OrderEvent event = createValidOrderEvent();
        
        // When/Then - No debe lanzar excepción
        assertThatCode(() -> validator.validate(event))
                .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando orderId es null")
    void shouldThrowValidationExceptionWhenOrderIdIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setOrderId(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .hasFieldOrPropertyWithValue("orderId", null)
                .extracting("validationErrors")
                .asList()
                .contains("orderId es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando orderId está vacío")
    void shouldThrowValidationExceptionWhenOrderIdIsBlank() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setOrderId("");
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("orderId es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando customerId es null")
    void shouldThrowValidationExceptionWhenCustomerIdIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setCustomerId(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("customerId es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando eventType es null")
    void shouldThrowValidationExceptionWhenEventTypeIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setEventType(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("eventType es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando totalAmount es null")
    void shouldThrowValidationExceptionWhenTotalAmountIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setTotalAmount(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("totalAmount es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando totalAmount es negativo")
    void shouldThrowValidationExceptionWhenTotalAmountIsNegative() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setTotalAmount(new BigDecimal("-10.00"));
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("totalAmount debe ser mayor o igual a 0");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando currency es null")
    void shouldThrowValidationExceptionWhenCurrencyIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setCurrency(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("currency es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando currency no tiene 3 caracteres")
    void shouldThrowValidationExceptionWhenCurrencyIsInvalid() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setCurrency("US");
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("currency debe tener 3 caracteres (ISO 4217)");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando metadata es null")
    void shouldThrowValidationExceptionWhenMetadataIsNull() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setMetadata(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("metadata es requerido");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando item quantity es menor a 1")
    void shouldThrowValidationExceptionWhenItemQuantityIsLessThanOne() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.getItems().get(0).setQuantity(0);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("items[0].quantity debe ser al menos 1");
    }
    
    @Test
    @DisplayName("Debe lanzar ValidationException cuando item unitPrice es negativo")
    void shouldThrowValidationExceptionWhenItemUnitPriceIsNegative() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.getItems().get(0).setUnitPrice(new BigDecimal("-10.00"));
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .contains("items[0].unitPrice debe ser mayor o igual a 0");
    }
    
    @Test
    @DisplayName("Debe acumular múltiples errores de validación")
    void shouldAccumulateMultipleValidationErrors() {
        // Given
        OrderEvent event = createValidOrderEvent();
        event.setOrderId(null);
        event.setCustomerId(null);
        
        // When/Then
        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(ValidationException.class)
                .extracting("validationErrors")
                .asList()
                .hasSize(2)
                .contains("orderId es requerido", "customerId es requerido");
    }
    
    // Helper methods
    
    private OrderEvent createValidOrderEvent() {
        return OrderEvent.builder()
                .orderId(UUID.randomUUID().toString())
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
                    .source("test-service")
                    .correlationId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .build())
                .build();
    }
    
}
