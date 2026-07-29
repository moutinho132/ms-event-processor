package com.dev.app.repository;

import com.dev.app.entity.OrderAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad OrderAudit.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface OrderAuditRepository extends JpaRepository<OrderAudit, Long> {
    
    /**
     * Busca todos los registros de auditoría para una orden específica.
     * 
     * @param orderId ID de la orden
     * @return Lista de registros de auditoría
     */
    List<OrderAudit> findByOrderId(String orderId);
    
    /**
     * Busca registros de auditoría por orderId ordenados por fecha de procesamiento descendente.
     * 
     * @param orderId ID de la orden
     * @return Lista de registros de auditoría ordenados
     */
    List<OrderAudit> findByOrderIdOrderByProcessedAtDesc(String orderId);
    
}
