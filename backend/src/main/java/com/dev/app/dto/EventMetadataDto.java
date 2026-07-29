package com.dev.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

/**
 * DTO que representa los metadatos de un evento.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventMetadataDto {
    
    @NotBlank(message = "source es requerido")
    @JsonProperty("source")
    private String source;
    
    @NotBlank(message = "correlationId es requerido")
    @JsonProperty("correlationId")
    private String correlationId;
    
    @NotNull(message = "timestamp es requerido")
    @JsonProperty("timestamp")
    private Instant timestamp;
    
}
