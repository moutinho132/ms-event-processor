package com.dev.app.exception;

import com.dev.app.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones.
 * 
 * Captura todas las excepciones de la aplicación y retorna respuestas
 * HTTP apropiadas con mensajes de error estructurados.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja ValidationException.
     * 
     * @param ex Excepción de validación
     * @return ResponseEntity con error 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Error de validación - OrderId: {}, EventType: {}, Errors: {}", 
                 ex.getOrderId(), ex.getEventType(), ex.getValidationErrors());
        
        ErrorResponse error = ErrorResponse.of(
            "VALIDATION_ERROR",
            ex.getMessage(),
            String.join(", ", ex.getValidationErrors())
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    
    /**
     * Maneja EventProcessingException.
     * 
     * @param ex Excepción de procesamiento de evento
     * @return ResponseEntity con error 500 Internal Server Error
     */
    @ExceptionHandler(EventProcessingException.class)
    public ResponseEntity<ErrorResponse> handleEventProcessingException(EventProcessingException ex) {
        log.error("Error de procesamiento de evento - OrderId: {}, EventType: {}, Message: {}", 
                 ex.getOrderId(), ex.getEventType(), ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            "EVENT_PROCESSING_ERROR",
            ex.getMessage(),
            String.format("OrderId: %s, EventType: %s", ex.getOrderId(), ex.getEventType())
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
    
    /**
     * Maneja RetryExhaustedException.
     * 
     * @param ex Excepción de reintentos agotados
     * @return ResponseEntity con error 503 Service Unavailable
     */
    @ExceptionHandler(RetryExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleRetryExhaustedException(RetryExhaustedException ex) {
        log.error("Reintentos agotados - OrderId: {}, EventType: {}, RetryCount: {}", 
                 ex.getOrderId(), ex.getEventType(), ex.getRetryCount(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            "RETRY_EXHAUSTED",
            ex.getMessage(),
            String.format("Reintentos realizados: %d", ex.getRetryCount())
        );
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }
    
    /**
     * Maneja JwtValidationException.
     * 
     * @param ex Excepción de validación JWT
     * @return ResponseEntity con error 401 Unauthorized
     */
    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ErrorResponse> handleJwtValidationException(JwtValidationException ex) {
        log.error("Error de validación JWT: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            "JWT_VALIDATION_ERROR",
            ex.getMessage(),
            "Token JWT inválido o expirado"
        );
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
    
    /**
     * Maneja Exception genérica.
     * 
     * @param ex Excepción genérica
     * @return ResponseEntity con error 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            "INTERNAL_ERROR",
            "Error interno del servidor",
            ex.getMessage()
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
    
}
