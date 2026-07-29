#!/bin/bash

# Script para enviar mensaje de prueba a Kafka
# Uso: ./test-producer.sh

KAFKA_CONTAINER="ms-event-processor-kafka"
BOOTSTRAP_SERVER="localhost:9092"

# Generar UUIDs para el mensaje
ORDER_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
CUSTOMER_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
PRODUCT_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
CORRELATION_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')

# Obtener timestamp actual
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Crear mensaje JSON
MESSAGE=$(cat <<EOF
{
  "orderId": "$ORDER_ID",
  "customerId": "$CUSTOMER_ID",
  "eventType": "CREATED",
  "totalAmount": 150.50,
  "currency": "USD",
  "items": [
    {
      "productId": "$PRODUCT_ID",
      "productName": "Test Product",
      "quantity": 2,
      "unitPrice": 75.25
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  },
  "metadata": {
    "source": "test-script",
    "correlationId": "$CORRELATION_ID",
    "timestamp": "$TIMESTAMP"
  }
}
EOF
)

echo "=========================================="
echo "Enviando mensaje de prueba a Kafka"
echo "=========================================="
echo ""
echo "Tópico: orders-created"
echo "Order ID: $ORDER_ID"
echo "Customer ID: $CUSTOMER_ID"
echo ""
echo "Mensaje JSON:"
echo "$MESSAGE" | jq .
echo ""
echo "=========================================="

# Enviar mensaje a Kafka usando kafka-console-producer
echo "$MESSAGE" | docker exec -i $KAFKA_CONTAINER /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server $BOOTSTRAP_SERVER --topic orders-created

if [ $? -eq 0 ]; then
    echo "✅ Mensaje enviado exitosamente!"
    echo ""
    echo "Espera unos segundos y verifica los logs de la aplicación para ver el procesamiento."
    echo ""
    echo "Para ver los logs:"
    echo "  docker logs -f ms-event-processor-app"
    echo ""
    echo "Para verificar en la base de datos:"
    echo "  docker exec -it ms-event-processor-postgres psql -U postgres -d orderdb -c \"SELECT * FROM orders WHERE order_id = '$ORDER_ID';\""
else
    echo "❌ Error enviando mensaje"
fi
