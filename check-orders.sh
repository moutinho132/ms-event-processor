#!/bin/bash

# Script para verificar órdenes en la base de datos
# Uso: ./check-orders.sh [order_id]

POSTGRES_CONTAINER="ms-event-processor-postgres"

echo "=========================================="
echo "Verificando órdenes en la base de datos"
echo "=========================================="
echo ""

if [ -z "$1" ]; then
    # Sin parámetro, mostrar todas las órdenes
    echo "Todas las órdenes:"
    docker exec -i $POSTGRES_CONTAINER psql -U postgres -d orderdb <<EOF
SELECT 
    order_id, 
    customer_id, 
    status, 
    total_amount, 
    currency, 
    created_at,
    processed_at
FROM orders 
ORDER BY created_at DESC 
LIMIT 10;
EOF
else
    # Con parámetro, buscar orden específica
    ORDER_ID=$1
    echo "Buscando orden: $ORDER_ID"
    echo ""
    
    docker exec -i $POSTGRES_CONTAINER psql -U postgres -d orderdb <<EOF
SELECT * FROM orders WHERE order_id = '$ORDER_ID';
EOF

    echo ""
    echo "Auditoría de la orden:"
    
    docker exec -i $POSTGRES_CONTAINER psql -U postgres -d orderdb <<EOF
SELECT 
    event_type, 
    old_status, 
    new_status, 
    processed_at, 
    processing_time_ms, 
    success 
FROM order_audit_log 
WHERE order_id = '$ORDER_ID' 
ORDER BY processed_at DESC;
EOF
fi

echo ""
echo "=========================================="
