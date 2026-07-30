package com.dev.app.service;

import com.dev.app.dto.WarehouseDto;
import com.dev.app.entity.Local;
import com.dev.app.entity.Warehouse;
import com.dev.app.repository.LocalRepository;
import com.dev.app.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Almacenes.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final LocalRepository localRepository;

    /**
     * Crea un nuevo almacén.
     */
    @Transactional
    public WarehouseDto create(WarehouseDto dto) {
        log.info("📦 Creando nuevo almacén: {}", dto.getName());
        
        Local local = localRepository.findById(dto.getLocalId())
                .orElseThrow(() -> new RuntimeException("Local no encontrado: " + dto.getLocalId()));
        
        Warehouse warehouse = Warehouse.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .capacity(dto.getCapacity())
                .temperatureControlled(dto.getTemperatureControlled() != null ? dto.getTemperatureControlled() : false)
                .minTemperature(dto.getMinTemperature())
                .maxTemperature(dto.getMaxTemperature())
                .local(local)
                .active(true)
                .build();
        
        warehouse = warehouseRepository.save(warehouse);
        log.info("✅ Almacén creado exitosamente: {}", warehouse.getWarehouseId());
        
        return toDto(warehouse);
    }

    /**
     * Actualiza un almacén existente.
     */
    @Transactional
    public WarehouseDto update(String warehouseId, WarehouseDto dto) {
        log.info("🔄 Actualizando almacén: {}", warehouseId);
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado: " + warehouseId));
        
        warehouse.setName(dto.getName());
        warehouse.setCode(dto.getCode());
        warehouse.setDescription(dto.getDescription());
        warehouse.setLocation(dto.getLocation());
        warehouse.setCapacity(dto.getCapacity());
        warehouse.setTemperatureControlled(dto.getTemperatureControlled());
        warehouse.setMinTemperature(dto.getMinTemperature());
        warehouse.setMaxTemperature(dto.getMaxTemperature());
        
        if (dto.getLocalId() != null && !dto.getLocalId().equals(warehouse.getLocal().getLocalId())) {
            Local local = localRepository.findById(dto.getLocalId())
                    .orElseThrow(() -> new RuntimeException("Local no encontrado: " + dto.getLocalId()));
            warehouse.setLocal(local);
        }
        
        warehouse = warehouseRepository.save(warehouse);
        log.info("✅ Almacén actualizado exitosamente: {}", warehouse.getWarehouseId());
        
        return toDto(warehouse);
    }

    /**
     * Obtiene un almacén por ID.
     */
    @Transactional(readOnly = true)
    public WarehouseDto getById(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado: " + warehouseId));
        
        return toDto(warehouse);
    }

    /**
     * Obtiene todos los almacenes activos.
     */
    @Transactional(readOnly = true)
    public List<WarehouseDto> getAll() {
        return warehouseRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene almacenes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<WarehouseDto> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return warehouseRepository.findByActiveTrue(pageable).map(this::toDto);
    }

    /**
     * Obtiene almacenes por local.
     */
    @Transactional(readOnly = true)
    public List<WarehouseDto> getByLocal(String localId) {
        return warehouseRepository.findByLocalLocalIdAndActiveTrue(localId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca almacenes por nombre.
     */
    @Transactional(readOnly = true)
    public List<WarehouseDto> searchByName(String name) {
        return warehouseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene almacenes con control de temperatura.
     */
    @Transactional(readOnly = true)
    public List<WarehouseDto> getTemperatureControlled() {
        return warehouseRepository.findByTemperatureControlledTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Desactiva un almacén.
     */
    @Transactional
    public void deactivate(String warehouseId) {
        log.info("🔴 Desactivando almacén: {}", warehouseId);
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado: " + warehouseId));
        
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
        
        log.info("✅ Almacén desactivado exitosamente");
    }

    /**
     * Activa un almacén.
     */
    @Transactional
    public void activate(String warehouseId) {
        log.info("🟢 Activando almacén: {}", warehouseId);
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado: " + warehouseId));
        
        warehouse.setActive(true);
        warehouseRepository.save(warehouse);
        
        log.info("✅ Almacén activado exitosamente");
    }

    /**
     * Elimina un almacén (soft delete).
     */
    @Transactional
    public void delete(String warehouseId) {
        log.info("🗑️ Eliminando almacén: {}", warehouseId);
        deactivate(warehouseId);
    }

    /**
     * Convierte entidad a DTO.
     */
    private WarehouseDto toDto(Warehouse warehouse) {
        return WarehouseDto.builder()
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .code(warehouse.getCode())
                .description(warehouse.getDescription())
                .location(warehouse.getLocation())
                .capacity(warehouse.getCapacity())
                .temperatureControlled(warehouse.getTemperatureControlled())
                .minTemperature(warehouse.getMinTemperature())
                .maxTemperature(warehouse.getMaxTemperature())
                .localId(warehouse.getLocal().getLocalId())
                .localName(warehouse.getLocal().getName())
                .active(warehouse.getActive())
                .createdAt(warehouse.getCreatedAt() != null ? warehouse.getCreatedAt().toString() : null)
                .build();
    }
}
