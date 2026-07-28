package com.dev.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuración de Retry con backoff exponencial.
 * 
 * Proporciona un RetryTemplate configurado para reintentar operaciones
 * que fallan de manera transitoria.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Configuration
@EnableRetry
public class RetryConfig {
    
    /**
     * Configura RetryTemplate con backoff exponencial.
     * 
     * Configuración:
     * - Máximo de reintentos: 3
     * - Intervalo inicial: 1000ms
     * - Multiplicador: 2.0
     * - Intervalo máximo: 10000ms
     * 
     * @return RetryTemplate configurado
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configurar política de reintentos
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Configurar backoff exponencial
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
    
}
