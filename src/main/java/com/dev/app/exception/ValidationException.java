package com.dev.app.exception;

import lombok.Getter;

import java.util.List;

/**
 * Excepción lanzada cuando la validación de un evento falla.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Getter
public class ValidationException extends EventProcessingException {
    
    private final List<String> validationErrors;
    
    /**
     * Constructor con orderId, eventType y lista de errores de validación.
     * 
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     * @param validationErrors Lista de errores de validación
     */
    public ValidationException(String orderId, String eventType, List<String> validationErrors) {
        super("Error de validación del evento", orderId, eventType);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Constructor con orderId, eventType, mensaje y errores de validación.
     * 
     * @param orderId ID de la orden
     * @param eventType Tipo de evento
     * @param message Mensaje de error
     * @param validationErrors Lista de errores de validación
     */
    public ValidationException(String orderId, String eventType, String message, List<String> validationErrors) {
        super(message, orderId, eventType);
        this.validationErrors = validationErrors;
    }
    
}
