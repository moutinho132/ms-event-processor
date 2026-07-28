# 🚀 Comandos Rápidos - MS-Event-Processor

## Iniciar Todo

```bash
# 1. Levantar infraestructura
docker compose up -d

# 2. Ejecutar aplicación
mvn spring-boot:run

# 3. Verificar
curl http://localhost:8080/actuator/health
```

## Probar con API REST

### Enviar mensaje de prueba a Kafka

```bash
curl -X POST http://localhost:8080/api/v1/orders/kafka/send-test
```

### Verificar en base de datos

```bash
# Todas las órdenes
curl http://localhost:8080/api/v1/orders/database/check

# Orden específica (usar orderId del paso anterior)
curl http://localhost:8080/api/v1/orders/database/check/{ORDER_ID}
```

### Crear orden directamente (sin Kafka)

```bash
curl -X POST http://localhost:8080/api/v1/orders/create-test
```

## Probar con Scripts de Shell

```bash
# Enviar mensaje de prueba
./test-producer.sh

# Verificar base de datos
./check-orders.sh
./check-orders.sh {ORDER_ID}
```

## Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## Detener Todo

```bash
# Detener aplicación: Ctrl+C

# Detener infraestructura
docker compose down
```

## Endpoints Principales

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/v1/orders/kafka/send-test` | POST | Enviar mensaje de prueba a Kafka |
| `/api/v1/orders/database/check` | GET | Ver todas las órdenes |
| `/api/v1/orders/database/check/{id}` | GET | Ver orden + auditoría |
| `/api/v1/orders/process` | POST | Procesar orden directamente |
| `/api/v1/orders/create-test` | POST | Crear orden de prueba |
| `/actuator/health` | GET | Health check |

## Ver Documentación Completa

```bash
cat DIRECTO-GUIA.md
```
