package com.dev.app.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Configuración de hilos virtuales de Java 21.
 * 
 * Esta configuración habilita el uso de hilos virtuales para todas las
 * operaciones asíncronas del microservicio, permitiendo alto throughput
 * con mínimo uso de recursos.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig {
    
    /**
     * Configura un TaskExecutor basado en hilos virtuales.
     * 
     * Los hilos virtuales son hilos ligeros gestionados por la JVM que permiten
     * manejar miles de operaciones concurrentes con un overhead mínimo.
     * 
     * @return AsyncTaskExecutor configurado con hilos virtuales
     */
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
    
    /**
     * Configura Tomcat para usar hilos virtuales en el manejo de solicitudes HTTP.
     * 
     * Esto permite que cada solicitud HTTP sea manejada por un hilo virtual,
     * mejorando significativamente la capacidad de manejar solicitudes concurrentes.
     * 
     * @return TomcatProtocolHandlerCustomizer configurado con hilos virtuales
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadExecutor() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
    
}
