package com.dev.app.controller;

import com.dev.app.dto.LocalDto;
import com.dev.app.service.LocalService;
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
 * Controlador REST para gestión de Locales.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/locals")
@RequiredArgsConstructor
@Tag(name = "Locales", description = "API para gestión de locales/tiendas físicas")
public class LocalController {

    private final LocalService localService;

    @PostMapping
    @Operation(summary = "Crear un nuevo local", description = "Crea un nuevo local/tienda física")
    public ResponseEntity<LocalDto> create(@Valid @RequestBody LocalDto dto) {
        log.info("🏪 POST /api/v1/locals - Creando local: {}", dto.getName());
        LocalDto created = localService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{localId}")
    @Operation(summary = "Actualizar un local", description = "Actualiza los datos de un local existente")
    public ResponseEntity<LocalDto> update(
            @Parameter(description = "ID del local") @PathVariable String localId,
            @Valid @RequestBody LocalDto dto) {
        log.info("🔄 PUT /api/v1/locals/{} - Actualizando local", localId);
        LocalDto updated = localService.update(localId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{localId}")
    @Operation(summary = "Obtener local por ID", description = "Obtiene los detalles de un local específico")
    public ResponseEntity<LocalDto> getById(
            @Parameter(description = "ID del local") @PathVariable String localId) {
        log.info("📍 GET /api/v1/locals/{} - Obteniendo local", localId);
        LocalDto local = localService.getById(localId);
        return ResponseEntity.ok(local);
    }

    @GetMapping
    @Operation(summary = "Obtener todos los locales", description = "Obtiene todos los locales activos")
    public ResponseEntity<List<LocalDto>> getAll() {
        log.info("📍 GET /api/v1/locals - Obteniendo todos los locales");
        List<LocalDto> locals = localService.getAll();
        return ResponseEntity.ok(locals);
    }

    @GetMapping("/page")
    @Operation(summary = "Obtener locales con paginación", description = "Obtiene locales con paginación")
    public ResponseEntity<Page<LocalDto>> getPage(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        log.info("📍 GET /api/v1/locals/page - Obteniendo página {} de {}", page, size);
        Page<LocalDto> locals = localService.getPage(page, size);
        return ResponseEntity.ok(locals);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar locales por nombre", description = "Busca locales por nombre (búsqueda parcial)")
    public ResponseEntity<List<LocalDto>> searchByName(
            @Parameter(description = "Nombre a buscar") @RequestParam String name) {
        log.info("🔍 GET /api/v1/locals/search - Buscando: {}", name);
        List<LocalDto> locals = localService.searchByName(name);
        return ResponseEntity.ok(locals);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Obtener locales por ciudad", description = "Obtiene locales de una ciudad específica")
    public ResponseEntity<List<LocalDto>> getByCity(
            @Parameter(description = "Nombre de la ciudad") @PathVariable String city) {
        log.info("📍 GET /api/v1/locals/city/{} - Obteniendo locales", city);
        List<LocalDto> locals = localService.getByCity(city);
        return ResponseEntity.ok(locals);
    }

    @PostMapping("/{localId}/deactivate")
    @Operation(summary = "Desactivar un local", description = "Desactiva un local (soft delete)")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "ID del local") @PathVariable String localId) {
        log.info("🔴 POST /api/v1/locals/{}/deactivate - Desactivando local", localId);
        localService.deactivate(localId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{localId}/activate")
    @Operation(summary = "Activar un local", description = "Activa un local desactivado")
    public ResponseEntity<Void> activate(
            @Parameter(description = "ID del local") @PathVariable String localId) {
        log.info("🟢 POST /api/v1/locals/{}/activate - Activando local", localId);
        localService.activate(localId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{localId}")
    @Operation(summary = "Eliminar un local", description = "Elimina un local (soft delete)")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del local") @PathVariable String localId) {
        log.info("🗑️ DELETE /api/v1/locals/{} - Eliminando local", localId);
        localService.delete(localId);
        return ResponseEntity.noContent().build();
    }
}
