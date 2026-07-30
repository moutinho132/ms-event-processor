package com.dev.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Local.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDto {

    private String localId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    private String phone;

    private String email;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private Double latitude;

    private Double longitude;

    private Boolean active;

    private Integer totalWarehouses;

    private String createdAt;
}
