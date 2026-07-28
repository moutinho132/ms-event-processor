package com.dev.app.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando se agotan todos los reintentos de una operación.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Getter
public class RetryExhaustedException extends EventProcessingException {
    
    private final int retryCount;
    
    /**
     * Constructor con orderId, eventType y número de reintentos.
     * 
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     * @param retryCount Número de reintentos realizados
     */
    public RetryExhaustedException(String orderId, String eventType, int retryCount) {
        super(String.format("Se agotaron %d reintentos para procesar el evento", retryCount), 
              orderId, eventType);
        this.retryCount = retryCount;
    }
    
    /**
     * Constructor con orderId, eventType, retryCount y causa.
     * 
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     * @param retryCount Número de reintentos realizados
     * @param cause Causa de la excepción
     */
    public RetryExhaustedException(String orderId, String eventType, int retryCount, Throwable cause) {
        super(String.format("Se agotaron %d reintentos para procesar el evento", retryCount), 
              orderId, eventType, cause);
        this.retryCount = retryCount;
    }
    
}
