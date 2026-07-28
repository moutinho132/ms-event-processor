package com.dev.app.util;

import com.dev.app.exception.RetryExhaustedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utilidad para ejecutar operaciones con retry y backoff exponencial.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RetryHelper {
    
    /**
     * Ejecuta una operación con retry y backoff exponencial.
     * 
     * @param operation Operación a ejecutar
     * @param maxRetries Número máximo de reintentos
     * @param initialIntervalMs Intervalo inicial en milisegundos
     * @param multiplier Multiplicador del backoff
     * @param maxIntervalMs Intervalo máximo en milisegundos
     * @param operationName Nombre de la operación para logging
     * @throws RetryExhaustedException cuando se agotan los reintentos
     */
    public void executeWithRetry(RetryableOperation operation, int maxRetries, 
                                  long initialIntervalMs, double multiplier, 
                                  long maxIntervalMs, String operationName) {
        int attempt = 0;
        long currentInterval = initialIntervalMs;
        Exception lastException = null;
        
        while (attempt <= maxRetries) {
            try {
                operation.execute();
                if (attempt > 0) {
                    log.info("Operación '{}' completada exitosamente en intento {}", operationName, attempt + 1);
                }
                return;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt > maxRetries) {
                    log.error("Operación '{}' falló después de {} intentos. Último error: {}", 
                             operationName, maxRetries + 1, e.getMessage());
                    throw new RetryExhaustedException(
                        "UNKNOWN",
                        operationName,
                        maxRetries + 1,
                        e
                    );
                }
                
                log.warn("Intento {} de {} para operación '{}' falló: {}. Reintentando en {}ms...", 
                        attempt, maxRetries + 1, operationName, e.getMessage(), currentInterval);
                
                try {
                    Thread.sleep(currentInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrumpido para operación '{}'", operationName);
                    throw new RetryExhaustedException(
                        "UNKNOWN",
                        operationName,
                        attempt,
                        ie
                    );
                }
                
                // Calcular siguiente intervalo con backoff exponencial
                currentInterval = Math.min((long) (currentInterval * multiplier), maxIntervalMs);
            }
        }
        
        // Este punto no debería alcanzarse, pero por seguridad
        throw new RetryExhaustedException(
            "UNKNOWN",
            operationName,
            maxRetries + 1,
            lastException
        );
    }
    
    /**
     * Interfaz funcional para operaciones que pueden fallar.
     */
    @FunctionalInterface
    public interface RetryableOperation {
        void execute() throws Exception;
    }
    
}
