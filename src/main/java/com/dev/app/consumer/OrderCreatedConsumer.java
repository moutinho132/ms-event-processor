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
 * Consumer Kafka para eventos de órdenes creadas.
 * 
 * Escucha el tópico 'orders-created' y procesa eventos de nuevas órdenes.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreatedConsumer {
    
    private final OrderEventProcessor orderEventProcessor;
    private final ObjectMapper objectMapper;
    
    /**
     * Consume mensajes del tópico orders-created.
     * 
     * @param record Registro de Kafka recibido
     * @param acknowledgment Objeto para ack manual
     */
    @KafkaListener(topics = "${app.kafka.topics.orders-created:orders-created}", 
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
            orderEventProcessor.processCreatedEvent(value);
            
            // Confirmar el mensaje procesado exitosamente
            acknowledgment.acknowledge();
            
            log.info("Mensaje procesado exitosamente - Topic: {}, Key: {}", topic, key);
            
        } catch (Exception e) {
            log.error("Error procesando mensaje del tópico {} - Key: {}. Error: {}", 
                     topic, key, e.getMessage(), e);
            
            // No hacer ack para que el mensaje sea reintentado o movido a DLQ
            // El manejo de errores se hace en el processor
            throw new RuntimeException("Error procesando evento de orden creada", e);
        }
    }
    
}
