package com.dev.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para Warehouse (Almacén).
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDto {

    private String warehouseId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String code;

    private String description;

    private String location;

    private Integer capacity;

    private Boolean temperatureControlled;

    private Double minTemperature;

    private Double maxTemperature;

    @NotNull(message = "El local es obligatorio")
    private String localId;

    private String localName;

    private Boolean active;

    private Integer totalProducts;

    private String createdAt;
}
