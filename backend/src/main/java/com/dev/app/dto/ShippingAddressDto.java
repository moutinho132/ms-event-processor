package com.dev.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO que representa una dirección de envío.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingAddressDto {
    
    @NotBlank(message = "street es requerido")
    @JsonProperty("street")
    private String street;
    
    @NotBlank(message = "city es requerido")
    @JsonProperty("city")
    private String city;
    
    @NotBlank(message = "state es requerido")
    @JsonProperty("state")
    private String state;
    
    @NotBlank(message = "postalCode es requerido")
    @JsonProperty("postalCode")
    private String postalCode;
    
    @NotBlank(message = "country es requerido")
    @JsonProperty("country")
    private String country;
    
}
