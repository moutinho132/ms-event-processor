# Guía de Pruebas - MS-Event-Processor

## 🚀 Iniciar la Aplicación

### 1. Iniciar Infraestructura
```bash
docker compose up -d
```

### 2. Iniciar la Aplicación
```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## 📊 Verificar Estado

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Verificar Servicios
```bash
docker ps
```

## 🧪 Enviar Mensaje de Prueba

### Opción 1: Usar Script de Prueba
```bash
./test-producer.sh
```

Este script enviará un mensaje de prueba al tópico `orders-created` con:
- Order ID generado automáticamente
- Customer ID generado automáticamente
- Total: $150.50 USD
- 2 items de producto

### Opción 2: Enviar Manualmente

**Paso 1: Crear el mensaje JSON**
```bash
cat > message.json <<EOF
{
  "orderId": "$(uuidgen | tr '[:upper:]' '[:lower:]')",
  "customerId": "$(uuidgen | tr '[:upper:]' '[:lower:]')",
  "eventType": "CREATED",
  "totalAmount": 100.00,
  "currency": "USD",
  "items": [
    {
      "productId": "$(uuidgen | tr '[:upper:]' '[:lower:]')",
      "productName": "Test Product",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ],
  "metadata": {
    "source": "manual-test",
    "correlationId": "$(uuidgen | tr '[:upper:]' '[:lower:]')",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  }
}
EOF
```

**Paso 2: Enviar a Kafka**
```bash
cat message.json | docker exec -i ms-event-processor-kafka \
  kafka-console-producer.sh --bootstrap-server localhost:9092 --topic orders-created
```

## 📋 Verificar Resultados

### Ver Logs de la Aplicación
```bash
# Si usas el comando mvn spring-boot:run, los logs aparecen en la consola
# O puedes ver los logs del contenedor si está en Docker
```

### Verificar Base de Datos
```bash
# Ver todas las órdenes
./check-orders.sh

# Ver orden específica
./check-orders.sh <order_id>
```

### Verificar SQS
```bash
# Listar mensajes en la cola
aws --endpoint-url=http://localhost:4566 sqs receive-message \
  --queue-url http://sqs.eu-west-1.localhost.localstack.cloud:4566/000000000000/ordenes-procesadas-queue \
  --max-number-of-messages 10
```

## 🎯 Escenarios de Prueba

### 1. Crear Orden
```bash
# Enviar mensaje con eventType: "CREATED"
./test-producer.sh
```

### 2. Actualizar Orden
Modificar el mensaje JSON y cambiar:
- `"eventType": "UPDATED"`
- Usar el mismo `orderId` de una orden existente
- Cambiar `totalAmount` u otros campos

### 3. Cancelar Orden
Modificar el mensaje JSON y cambiar:
- `"eventType": "CANCELLED"`
- Usar el `orderId` de una orden existente

## 📊 Monitoreo

### Métricas
```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🐛 Troubleshooting

### La aplicación no inicia
1. Verificar que los contenedores estén corriendo: `docker ps`
2. Verificar logs: `docker logs ms-event-processor-kafka`
3. Verificar conectividad: `curl http://localhost:8080/actuator/health`

### No se procesan mensajes
1. Verificar que Kafka esté funcionando: `docker logs ms-event-processor-kafka`
2. Verificar que los consumers estén activos en los logs de la aplicación
3. Verificar el tópico existe:
   ```bash
   docker exec ms-event-processor-kafka kafka-topics.sh --list --bootstrap-server localhost:9092
   ```

### Error en base de datos
1. Verificar conexión: `docker exec -it ms-event-processor-postgres psql -U postgres -d orderdb -c "\dt"`
2. Verificar logs: `docker logs ms-event-processor-postgres`

## 🛑 Detener Todo

### Detener Aplicación
```bash
# Ctrl+C en la terminal donde ejecutas mvn spring-boot:run
```

### Detener Infraestructura
```bash
docker compose down
```

### Limpiar Todo (incluyendo volúmenes)
```bash
docker compose down -v
```
