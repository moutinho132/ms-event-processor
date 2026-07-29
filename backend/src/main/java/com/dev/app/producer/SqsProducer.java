package com.dev.app.producer;

import com.dev.app.dto.SqsMessage;
import com.dev.app.util.RetryHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * Productor de mensajes para AWS SQS.
 * 
 * Envía mensajes procesados a la cola SQS con soporte para retry
 * y backoff exponencial.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqsProducer {
    
    private final SqsClient sqsClient;
    private final RetryHelper retryHelper;
    private final ObjectMapper objectMapper;
    
    @Value("${sqs.queue.name}")
    private String queueName;
    
    @Value("${sqs.queue.dlq-name}")
    private String dlqName;
    
    @Value("${retry.max-attempts:3}")
    private int maxRetries;
    
    @Value("${retry.initial-interval:1000}")
    private long initialInterval;
    
    @Value("${retry.multiplier:2.0}")
    private double multiplier;
    
    @Value("${retry.max-interval:10000}")
    private long maxInterval;
    
    /**
     * Envía un mensaje a la cola SQS con retry.
     * 
     * @param message Mensaje a enviar
     */
    public void sendMessage(SqsMessage message) {
        log.debug("Enviando mensaje a SQS - OrderId: {}", message.getOrderId());
        
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            
            retryHelper.executeWithRetry(
                () -> sendMessageToSqs(messageBody, message.getOrderId()),
                maxRetries,
                initialInterval,
                multiplier,
                maxInterval,
                "SQS_SEND"
            );
            
            log.info("Mensaje enviado exitosamente a SQS - OrderId: {}", message.getOrderId());
            
        } catch (JsonProcessingException e) {
            log.error("Error serializando mensaje SQS: {}", e.getMessage(), e);
            throw new RuntimeException("Error serializando mensaje SQS", e);
        }
    }
    
    /**
     * Envía el mensaje a SQS.
     * 
     * @param messageBody Cuerpo del mensaje en JSON
     * @param orderId ID de la orden para logging
     */
    private void sendMessageToSqs(String messageBody, String orderId) {
        try {
            // Obtener el endpoint configurado
            String sqsEndpoint = System.getenv("AWS_SQS_ENDPOINT");
            if (sqsEndpoint == null || sqsEndpoint.isEmpty()) {
                sqsEndpoint = "http://localhost:4566";
            }
            
            // Construir URL de la cola para LocalStack
            // Formato: http://sqs.{region}.localhost.localstack.cloud:4566/000000000000/{queueName}
            // o simplemente: {endpoint}/000000000000/{queueName}
            String queueUrl = String.format("%s/000000000000/%s", sqsEndpoint, queueName);
            
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();
            
            SendMessageResponse response = sqsClient.sendMessage(request);
            
            log.debug("Mensaje enviado a SQS - MessageId: {}, OrderId: {}", 
                     response.messageId(), orderId);
            
        } catch (Exception e) {
            log.error("Error enviando mensaje a SQS - OrderId: {}, Error: {}", 
                     orderId, e.getMessage());
            throw new RuntimeException("Error enviando mensaje a SQS", e);
        }
    }
    
    /**
     * Envía un mensaje a la cola dead-letter.
     * 
     * @param message Mensaje original
     * @param errorMessage Mensaje de error
     */
    public void sendToDeadLetterQueue(SqsMessage message, String errorMessage) {
        log.error("Enviando mensaje a DLQ - OrderId: {}, Error: {}", 
                 message.getOrderId(), errorMessage);
        
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            
            String sqsEndpoint = System.getenv("AWS_SQS_ENDPOINT");
            if (sqsEndpoint == null || sqsEndpoint.isEmpty()) {
                sqsEndpoint = "http://localhost:4566";
            }
            
            String dlqUrl = String.format("%s/000000000000/%s", sqsEndpoint, dlqName);
            
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(dlqUrl)
                    .messageBody(messageBody)
                    .build();
            
            sqsClient.sendMessage(request);
            
            log.info("Mensaje enviado a DLQ exitosamente - OrderId: {}", message.getOrderId());
            
        } catch (Exception e) {
            log.error("Error enviando mensaje a DLQ - OrderId: {}, Error: {}", 
                     message.getOrderId(), e.getMessage(), e);
        }
    }
    
}
