package com.dev.app.repository;

import com.dev.app.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Almacenes.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

    /**
     * Busca almacenes activos.
     */
    List<Warehouse> findByActiveTrue();

    /**
     * Busca almacenes por local.
     */
    List<Warehouse> findByLocalLocalId(String localId);

    /**
     * Busca almacenes activos por local.
     */
    List<Warehouse> findByLocalLocalIdAndActiveTrue(String localId);

    /**
     * Busca almacén por código único.
     */
    Optional<Warehouse> findByCode(String code);

    /**
     * Busca almacenes por nombre (búsqueda parcial, case insensitive).
     */
    @Query("SELECT w FROM Warehouse w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Warehouse> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca almacenes con control de temperatura.
     */
    List<Warehouse> findByTemperatureControlledTrue();

    /**
     * Busca almacenes con paginación.
     */
    Page<Warehouse> findByActiveTrue(Pageable pageable);

    /**
     * Busca almacenes por local con paginación.
     */
    Page<Warehouse> findByLocalLocalId(String localId, Pageable pageable);

    /**
     * Cuenta almacenes por local.
     */
    long countByLocalLocalId(String localId);

    /**
     * Cuenta almacenes activos.
     */
    long countByActiveTrue();
}
