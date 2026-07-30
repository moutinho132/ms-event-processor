package com.dev.app.entity;

/**
 * Roles disponibles para los usuarios del sistema.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
public enum UserRole {
    ADMIN("Administrador", "Acceso total al sistema"),
    SUPERVISOR("Supervisor", "Gestión de productos y stock"),
    CUSTOMER("Cliente", "Usuario final de compras");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
