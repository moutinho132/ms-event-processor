package com.dev.app.repository;

import com.dev.app.entity.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de Locales.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface LocalRepository extends JpaRepository<Local, String> {

    /**
     * Busca locales activos.
     */
    List<Local> findByActiveTrue();

    /**
     * Busca locales por ciudad.
     */
    List<Local> findByCity(String city);

    /**
     * Busca locales por país.
     */
    List<Local> findByCountry(String country);

    /**
     * Busca locales activos por ciudad.
     */
    List<Local> findByCityAndActiveTrue(String city);

    /**
     * Busca locales por nombre (búsqueda parcial, case insensitive).
     */
    @Query("SELECT l FROM Local l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Local> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca locales con paginación.
     */
    Page<Local> findByActiveTrue(Pageable pageable);

    /**
     * Cuenta locales por ciudad.
     */
    long countByCity(String city);

    /**
     * Cuenta locales activos.
     */
    long countByActiveTrue();
}
