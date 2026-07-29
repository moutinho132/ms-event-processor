package com.dev.app.entity;

/**
 * Enum que representa los tipos de eventos de orden.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
public enum OrderEventType {
    
    /**
     * Evento de creación de una nueva orden
     */
    CREATED,
    
    /**
     * Evento de actualización de una orden existente
     */
    UPDATED,
    
    /**
     * Evento de cancelación de una orden
     */
    CANCELLED
    
}
