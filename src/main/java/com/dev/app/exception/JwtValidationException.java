package com.dev.app.exception;

/**
 * Excepción lanzada cuando la validación de un token JWT falla.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
public class JwtValidationException extends RuntimeException {
    
    /**
     * Constructor con mensaje.
     * 
     * @param message Mensaje de error
     */
    public JwtValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje de error
     * @param cause Causa de la excepción
     */
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
