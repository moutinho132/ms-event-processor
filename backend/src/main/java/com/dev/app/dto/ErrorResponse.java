package com.dev.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

/**
 * DTO que representa una respuesta de error de la API.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    /**
     * Factory method para crear ErrorResponse.
     * 
     * @param errorCode Código de error
     * @param message Mensaje de error
     * @return ErrorResponse
     */
    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Factory method para crear ErrorResponse con detalles.
     * 
     * @param errorCode Código de error
     * @param message Mensaje de error
     * @param details Detalles adicionales
     * @return ErrorResponse
     */
    public static ErrorResponse of(String errorCode, String message, String details) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
    
}
