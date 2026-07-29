package com.dev.app.exception;

import lombok.Getter;

/**
 * Excepción base para errores de procesamiento de eventos.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Getter
public class EventProcessingException extends RuntimeException {
    
    private final String orderId;
    private final String eventType;
    
    /**
     * Constructor con mensaje, orderId y eventType.
     * 
     * @param message Mensaje de error
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     */
    public EventProcessingException(String message, String orderId, String eventType) {
        super(message);
        this.orderId = orderId;
        this.eventType = eventType;
    }
    
    /**
     * Constructor con mensaje, orderId, eventType y causa.
     * 
     * @param message Mensaje de error
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     * @param cause Causa de la excepción
     */
    public EventProcessingException(String message, String orderId, String eventType, Throwable cause) {
        super(message, cause);
        this.orderId = orderId;
        this.eventType = eventType;
    }
    
}
