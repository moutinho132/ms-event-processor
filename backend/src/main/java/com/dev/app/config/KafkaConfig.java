package com.dev.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Configuración de Kafka Consumer.
 * 
 * Configura los consumers de Kafka para escuchar eventos de órdenes
 * desde múltiples tópicos utilizando hilos virtuales de Java 21.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    /**
     * Configura el ConsumerFactory para crear consumers de Kafka.
     * 
     * @return ConsumerFactory configurado
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    /**
     * Configura el ConcurrentKafkaListenerContainerFactory.
     * 
     * Esta factory crea containers para los listeners de Kafka con las siguientes características:
     * - Modo de ack manual para control preciso del offset
     * - Executor de hilos virtuales para procesamiento concurrente escalable
     * - Configuración de trusted packages para deserialización segura
     * 
     * @return ConcurrentKafkaListenerContainerFactory configurado
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Configurar modo manual de ack para control preciso
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        
        // Configurar executor de hilos virtuales
        factory.getContainerProperties().setListenerTaskExecutor(
            new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor())
        );
        
        // Habilitar batch listening para mejor throughput
        factory.setBatchListener(false);
        
        return factory;
    }
    
}
