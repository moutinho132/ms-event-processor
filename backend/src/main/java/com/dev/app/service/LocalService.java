package com.dev.app.service;

import com.dev.app.dto.LocalDto;
import com.dev.app.entity.Local;
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
 * Servicio para gestión de Locales.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalService {

    private final LocalRepository localRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * Crea un nuevo local.
     */
    @Transactional
    public LocalDto create(LocalDto dto) {
        log.info("🏪 Creando nuevo local: {}", dto.getName());
        
        Local local = Local.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .active(true)
                .build();
        
        local = localRepository.save(local);
        log.info("✅ Local creado exitosamente: {}", local.getLocalId());
        
        return toDto(local);
    }

    /**
     * Actualiza un local existente.
     */
    @Transactional
    public LocalDto update(String localId, LocalDto dto) {
        log.info("🔄 Actualizando local: {}", localId);
        
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RuntimeException("Local no encontrado: " + localId));
        
        local.setName(dto.getName());
        local.setAddress(dto.getAddress());
        local.setPhone(dto.getPhone());
        local.setEmail(dto.getEmail());
        local.setCity(dto.getCity());
        local.setState(dto.getState());
        local.setCountry(dto.getCountry());
        local.setPostalCode(dto.getPostalCode());
        local.setLatitude(dto.getLatitude());
        local.setLongitude(dto.getLongitude());
        
        local = localRepository.save(local);
        log.info("✅ Local actualizado exitosamente: {}", local.getLocalId());
        
        return toDto(local);
    }

    /**
     * Obtiene un local por ID.
     */
    @Transactional(readOnly = true)
    public LocalDto getById(String localId) {
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RuntimeException("Local no encontrado: " + localId));
        
        return toDto(local);
    }

    /**
     * Obtiene todos los locales activos.
     */
    @Transactional(readOnly = true)
    public List<LocalDto> getAll() {
        return localRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene locales con paginación.
     */
    @Transactional(readOnly = true)
    public Page<LocalDto> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return localRepository.findByActiveTrue(pageable).map(this::toDto);
    }

    /**
     * Busca locales por nombre.
     */
    @Transactional(readOnly = true)
    public List<LocalDto> searchByName(String name) {
        return localRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene locales por ciudad.
     */
    @Transactional(readOnly = true)
    public List<LocalDto> getByCity(String city) {
        return localRepository.findByCityAndActiveTrue(city).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Desactiva un local.
     */
    @Transactional
    public void deactivate(String localId) {
        log.info("🔴 Desactivando local: {}", localId);
        
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RuntimeException("Local no encontrado: " + localId));
        
        local.setActive(false);
        localRepository.save(local);
        
        log.info("✅ Local desactivado exitosamente");
    }

    /**
     * Activa un local.
     */
    @Transactional
    public void activate(String localId) {
        log.info("🟢 Activando local: {}", localId);
        
        Local local = localRepository.findById(localId)
                .orElseThrow(() -> new RuntimeException("Local no encontrado: " + localId));
        
        local.setActive(true);
        localRepository.save(local);
        
        log.info("✅ Local activado exitosamente");
    }

    /**
     * Elimina un local (soft delete).
     */
    @Transactional
    public void delete(String localId) {
        log.info("🗑️ Eliminando local: {}", localId);
        deactivate(localId);
    }

    /**
     * Convierte entidad a DTO.
     */
    private LocalDto toDto(Local local) {
        int totalWarehouses = (int) warehouseRepository.countByLocalLocalId(local.getLocalId());
        
        return LocalDto.builder()
                .localId(local.getLocalId())
                .name(local.getName())
                .address(local.getAddress())
                .phone(local.getPhone())
                .email(local.getEmail())
                .city(local.getCity())
                .state(local.getState())
                .country(local.getCountry())
                .postalCode(local.getPostalCode())
                .latitude(local.getLatitude())
                .longitude(local.getLongitude())
                .active(local.getActive())
                .totalWarehouses(totalWarehouses)
                .createdAt(local.getCreatedAt() != null ? local.getCreatedAt().toString() : null)
                .build();
    }
}
