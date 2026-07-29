-- Schema inicial para MS-Event-Processor
-- Base de datos PostgreSQL

-- Tabla de órdenes
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36),
    product_name VARCHAR(255),
    quantity INTEGER,
    unit_price DECIMAL(19, 4),
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    shipping_address JSONB,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'))
);

-- Índices para la tabla orders
CREATE INDEX IF NOT EXISTS idx_order_order_id ON orders(order_id);
CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_created ON orders(created_at);

-- Tabla de items de orden
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL
);

-- Índice para la tabla order_items
CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_items(order_id);

-- Tabla de auditoría de órdenes
CREATE TABLE IF NOT EXISTS order_audit_log (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(36),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_time_ms BIGINT,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(2000),
    
    CONSTRAINT chk_event_type CHECK (event_type IN ('CREATED', 'UPDATED', 'CANCELLED'))
);

-- Índices para la tabla order_audit_log
CREATE INDEX IF NOT EXISTS idx_audit_order ON order_audit_log(order_id);
CREATE INDEX IF NOT EXISTS idx_audit_processed ON order_audit_log(processed_at);

-- Comentarios en tablas
COMMENT ON TABLE orders IS 'Tabla de órdenes procesadas por el microservicio';
COMMENT ON TABLE order_items IS 'Tabla de items asociados a cada orden';
COMMENT ON TABLE order_audit_log IS 'Tabla de auditoría para trazabilidad de eventos procesados';
