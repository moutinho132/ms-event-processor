package com.dev.app.controller;

import com.dev.app.dto.WarehouseDto;
import com.dev.app.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Almacenes.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Almacenes", description = "API para gestión de almacenes")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @Operation(summary = "Crear un nuevo almacén", description = "Crea un nuevo almacén en un local")
    public ResponseEntity<WarehouseDto> create(@Valid @RequestBody WarehouseDto dto) {
        log.info("📦 POST /api/v1/warehouses - Creando almacén: {}", dto.getName());
        WarehouseDto created = warehouseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{warehouseId}")
    @Operation(summary = "Actualizar un almacén", description = "Actualiza los datos de un almacén existente")
    public ResponseEntity<WarehouseDto> update(
            @Parameter(description = "ID del almacén") @PathVariable String warehouseId,
            @Valid @RequestBody WarehouseDto dto) {
        log.info("🔄 PUT /api/v1/warehouses/{} - Actualizando almacén", warehouseId);
        WarehouseDto updated = warehouseService.update(warehouseId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{warehouseId}")
    @Operation(summary = "Obtener almacén por ID", description = "Obtiene los detalles de un almacén específico")
    public ResponseEntity<WarehouseDto> getById(
            @Parameter(description = "ID del almacén") @PathVariable String warehouseId) {
        log.info("📍 GET /api/v1/warehouses/{} - Obteniendo almacén", warehouseId);
        WarehouseDto warehouse = warehouseService.getById(warehouseId);
        return ResponseEntity.ok(warehouse);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los almacenes", description = "Obtiene todos los almacenes activos")
    public ResponseEntity<List<WarehouseDto>> getAll() {
        log.info("📍 GET /api/v1/warehouses - Obteniendo todos los almacenes");
        List<WarehouseDto> warehouses = warehouseService.getAll();
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/page")
    @Operation(summary = "Obtener almacenes con paginación", description = "Obtiene almacenes con paginación")
    public ResponseEntity<Page<WarehouseDto>> getPage(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        log.info("📍 GET /api/v1/warehouses/page - Obteniendo página {} de {}", page, size);
        Page<WarehouseDto> warehouses = warehouseService.getPage(page, size);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/local/{localId}")
    @Operation(summary = "Obtener almacenes por local", description = "Obtiene todos los almacenes de un local específico")
    public ResponseEntity<List<WarehouseDto>> getByLocal(
            @Parameter(description = "ID del local") @PathVariable String localId) {
        log.info("📍 GET /api/v1/warehouses/local/{} - Obteniendo almacenes", localId);
        List<WarehouseDto> warehouses = warehouseService.getByLocal(localId);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar almacenes por nombre", description = "Busca almacenes por nombre (búsqueda parcial)")
    public ResponseEntity<List<WarehouseDto>> searchByName(
            @Parameter(description = "Nombre a buscar") @RequestParam String name) {
        log.info("🔍 GET /api/v1/warehouses/search - Buscando: {}", name);
        List<WarehouseDto> warehouses = warehouseService.searchByName(name);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/temperature-controlled")
    @Operation(summary = "Obtener almacenes con control de temperatura", description = "Obtiene almacenes con sistema de control de temperatura")
    public ResponseEntity<List<WarehouseDto>> getTemperatureControlled() {
        log.info("🌡️ GET /api/v1/warehouses/temperature-controlled - Obteniendo almacenes");
        List<WarehouseDto> warehouses = warehouseService.getTemperatureControlled();
        return ResponseEntity.ok(warehouses);
    }

    @PostMapping("/{warehouseId}/deactivate")
    @Operation(summary = "Desactivar un almacén", description = "Desactiva un almacén (soft delete)")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "ID del almacén") @PathVariable String warehouseId) {
        log.info("🔴 POST /api/v1/warehouses/{}/deactivate - Desactivando almacén", warehouseId);
        warehouseService.deactivate(warehouseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{warehouseId}/activate")
    @Operation(summary = "Activar un almacén", description = "Activa un almacén desactivado")
    public ResponseEntity<Void> activate(
            @Parameter(description = "ID del almacén") @PathVariable String warehouseId) {
        log.info("🟢 POST /api/v1/warehouses/{}/activate - Activando almacén", warehouseId);
        warehouseService.activate(warehouseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{warehouseId}")
    @Operation(summary = "Eliminar un almacén", description = "Elimina un almacén (soft delete)")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del almacén") @PathVariable String warehouseId) {
        log.info("🗑️ DELETE /api/v1/warehouses/{} - Eliminando almacén", warehouseId);
        warehouseService.delete(warehouseId);
        return ResponseEntity.noContent().build();
    }
}
