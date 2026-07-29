package com.dev.app.repository;

import com.dev.app.entity.Order;
import com.dev.app.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Order.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Busca una orden por su orderId único.
     * 
     * @param orderId ID único de la orden
     * @return Optional conteniendo la orden si existe
     */
    Optional<Order> findByOrderId(String orderId);
    
    /**
     * Busca todas las órdenes de un cliente específico.
     * 
     * @param customerId ID del cliente
     * @return Lista de órdenes del cliente
     */
    List<Order> findByCustomerId(String customerId);
    
    /**
     * Busca todas las órdenes con un estado específico.
     * 
     * @param status Estado de la orden
     * @return Lista de órdenes con el estado especificado
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Verifica si existe una orden con el orderId especificado.
     * 
     * @param orderId ID único de la orden
     * @return true si existe, false en caso contrario
     */
    boolean existsByOrderId(String orderId);
    
    /**
     * Busca órdenes por customerId y status.
     * 
     * @param customerId ID del cliente
     * @param status Estado de la orden
     * @return Lista de órdenes que coinciden con los criterios
     */
    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
    
}
