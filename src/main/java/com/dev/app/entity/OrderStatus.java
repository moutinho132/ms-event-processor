package com.dev.app.entity;

/**
 * Enum que representa los posibles estados de una orden.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
public enum OrderStatus {
    
    /**
     * Orden creada, pendiente de confirmación
     */
    PENDING,
    
    /**
     * Orden confirmada y lista para procesamiento
     */
    CONFIRMED,
    
    /**
     * Orden en proceso de preparación
     */
    PROCESSING,
    
    /**
     * Orden enviada al cliente
     */
    SHIPPED,
    
    /**
     * Orden entregada exitosamente
     */
    DELIVERED,
    
    /**
     * Orden cancelada
     */
    CANCELLED,
    
    /**
     * Orden reembolsada
     */
    REFUNDED
    
}
