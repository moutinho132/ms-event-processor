package com.dev.app.consumer;

import com.dev.app.processor.OrderEventProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Consumer Kafka para eventos de cancelación de órdenes.
 * 
 * Escucha el tópico 'orders-cancelled' y procesa eventos de cancelación de órdenes.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelledConsumer {
    
    private final OrderEventProcessor orderEventProcessor;
    private final ObjectMapper objectMapper;
    
    /**
     * Consume mensajes del tópico orders-cancelled.
     * 
     * @param record Registro de Kafka recibido
     * @param acknowledgment Objeto para ack manual
     */
    @KafkaListener(topics = "${app.kafka.topics.orders-cancelled:orders-cancelled}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        String key = record.key();
        String value = record.value();
        
        log.info("Recibido mensaje del tópico {} - Key: {}, Partition: {}, Offset: {}", 
                topic, key, record.partition(), record.offset());
        log.debug("Payload: {}", value);
        
        try {
            // Procesar el evento
            orderEventProcessor.processCancelledEvent(value);
            
            // Confirmar el mensaje procesado exitosamente
            acknowledgment.acknowledge();
            
            log.info("Mensaje procesado exitosamente - Topic: {}, Key: {}", topic, key);
            
        } catch (Exception e) {
            log.error("Error procesando mensaje del tópico {} - Key: {}. Error: {}", 
                     topic, key, e.getMessage(), e);
            
            throw new RuntimeException("Error procesando evento de cancelación de orden", e);
        }
    }
    
}
