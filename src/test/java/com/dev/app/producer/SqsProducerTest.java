package com.dev.app.producer;

import com.dev.app.dto.SqsMessage;
import com.dev.app.entity.OrderStatus;
import com.dev.app.util.RetryHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SqsProducer.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SqsProducerTest {
    
    @Mock
    private SqsClient sqsClient;
    
    private RetryHelper retryHelper;
    
    private ObjectMapper objectMapper;
    
    private SqsProducer sqsProducer;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        retryHelper = new RetryHelper();
        sqsProducer = new SqsProducer(sqsClient, retryHelper, objectMapper);
        
        // Set fields manually since we're not using Spring context
        ReflectionTestUtils.setField(sqsProducer, "queueName", "test-queue");
        ReflectionTestUtils.setField(sqsProducer, "dlqName", "test-dlq");
        ReflectionTestUtils.setField(sqsProducer, "maxRetries", 2);
        ReflectionTestUtils.setField(sqsProducer, "initialInterval", 100L);
        ReflectionTestUtils.setField(sqsProducer, "multiplier", 2.0);
        ReflectionTestUtils.setField(sqsProducer, "maxInterval", 500L);
    }
    
    @Test
    @DisplayName("Debe enviar mensaje a SQS exitosamente")
    void shouldSendMessageToSqsSuccessfully() {
        // Given
        SqsMessage message = createValidSqsMessage();
        
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(UUID.randomUUID().toString())
                .build();
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(response);
        
        // When
        sqsProducer.sendMessage(message);
        
        // Then
        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }
    
    @Test
    @DisplayName("Debe enviar mensaje a DLQ exitosamente")
    void shouldSendMessageToDeadLetterQueueSuccessfully() {
        // Given
        SqsMessage message = createValidSqsMessage();
        String errorMessage = "Test error";
        
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(UUID.randomUUID().toString())
                .build();
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(response);
        
        // When
        sqsProducer.sendToDeadLetterQueue(message, errorMessage);
        
        // Then
        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }
    
    // Helper methods
    
    private SqsMessage createValidSqsMessage() {
        return SqsMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .orderId(UUID.randomUUID().toString())
                .customerId(UUID.randomUUID().toString())
                .productId(UUID.randomUUID().toString())
                .quantity(1)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .processedAt(Instant.now())
                .source("test-service")
                .build();
    }
    
}
