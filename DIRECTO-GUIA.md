# 🎯 MS-Event-Processor - Guía Completa para Directo

## 📌 Descripción del Proyecto

**MS-Event-Processor** es un microservicio **event-driven** construido con **Java 21** y **Spring Boot 3.x** que demuestra patrones modernos de arquitectura de microservicios.

### ¿Qué hace?

1. **Consume eventos** de órdenes desde **Apache Kafka** (3 tópicos diferentes)
2. **Procesa** las órdenes aplicando lógica de negocio
3. **Persiste** los datos en **PostgreSQL**
4. **Reenvía** mensajes procesados a **AWS SQS**
5. **Proporciona API REST** para prueba y verificación

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                    Apache Kafka (KRaft Mode)                    │
│   orders-created | orders-updates | orders-cancelled            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                   MS-Event-Processor                             │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ Kafka Consumers  │  │ OrderProcessor   │  │  REST API     │ │
│  │  (3 listeners)   │  │  (Business Logic)│  │  (Testing)    │ │
│  └────────┬─────────┘  └────────┬─────────┘  └───────┬───────┘ │
│           │                     │                     │         │
│           └─────────────────────┼─────────────────────┘         │
│                                 ▼                                │
│           ┌──────────────────────────────────────┐              │
│           │   OrderRepository + SqsProducer       │              │
│           └──────────┬──────────────┬────────────┘              │
└──────────────────────┼──────────────┼───────────────────────────┘
                       ▼              ▼
             ┌─────────────────┐   ┌─────────────────────┐
             │   PostgreSQL    │   │    AWS SQS          │
             │   (Database)    │   │ (Message Queue)     │
             └─────────────────┘   └─────────────────────┘
```

---

## 🔧 Stack Tecnológico

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Java | 21 LTS | Runtime con **Virtual Threads** |
| Spring Boot | 3.3.5 | Framework principal |
| Spring Kafka | 3.x | Consumo de eventos Kafka |
| Spring Data JPA | 3.x | Persistencia en PostgreSQL |
| Spring Security | 3.x | Autenticación JWT |
| Apache Kafka | 3.7.0 | Message Broker (KRaft mode) |
| PostgreSQL | 15 | Base de datos relacional |
| AWS SQS | - | Cola de mensajes de salida |
| LocalStack | 3.7 | Simulación local de AWS |
| Docker | - | Containerización |
| Testcontainers | 1.20.3 | Testing de integración |

---

## 🚀 Inicio Rápido

### 1. Clonar y levantar infraestructura

```bash
# Iniciar PostgreSQL, Kafka y LocalStack
docker compose up -d

# Verificar que todo esté corriendo
docker ps
```

### 2. Compilar y ejecutar

```bash
# Compilar proyecto
mvn clean compile

# Ejecutar tests unitarios
mvn test

# Ejecutar aplicación
mvn spring-boot:run
```

### 3. Verificar que funciona

```bash
# Health check
curl http://localhost:8080/actuator/health
```

---

## 📡 Dos Formas de Usar el Sistema

### 🎯 OPCIÓN A: Usando API REST (Recomendado para demo)

Los endpoints REST replican **exactamente** la funcionalidad de los scripts de shell.

#### 1. Enviar mensaje de prueba a Kafka

**Equivalente a:** `./test-producer.sh`

```bash
# Enviar mensaje de prueba con datos aleatorios
curl -X POST http://localhost:8080/api/v1/orders/kafka/send-test

# Respuesta:
{
  "status": "SUCCESS",
  "message": "Mensaje enviado exitosamente a Kafka",
  "topic": "orders-created",
  "orderId": "3d034ee8-7da0-498a-bed6-f52704591b0c",
  "customerId": "1d3fecb1-830c-4c0f-b81a-0f7bba1dd3e1",
  "event": { ... },
  "note": "El mensaje será procesado de manera asíncrona..."
}
```

#### 2. Enviar mensaje custom a Kafka

```bash
curl -X POST http://localhost:8080/api/v1/orders/kafka/send \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "my-custom-order-id",
    "customerId": "customer-123",
    "eventType": "CREATED",
    "totalAmount": 250.00,
    "currency": "USD",
    "items": [{
      "productId": "product-456",
      "productName": "Premium Widget",
      "quantity": 3,
      "unitPrice": 83.33
    }],
    "metadata": {
      "source": "manual-test",
      "correlationId": "corr-123",
      "timestamp": "2026-07-28T22:00:00Z"
    }
  }'
```

#### 3. Verificar órdenes en la base de datos

**Equivalente a:** `./check-orders.sh`

```bash
# Ver todas las órdenes
curl http://localhost:8080/api/v1/orders/database/check

# Ver orden específica con auditoría
curl http://localhost:8080/api/v1/orders/database/check/{orderId}
```

#### 4. Procesar orden directamente (sin Kafka)

```bash
# Procesamiento síncrono - no usa Kafka
curl -X POST http://localhost:8080/api/v1/orders/process \
  -H "Content-Type: application/json" \
  -d '{ ... evento de orden ... }'

# Crear orden de prueba rápidamente
curl -X POST http://localhost:8080/api/v1/orders/create-test
```

---

### 🎯 OPCIÓN B: Usando Scripts de Shell

#### Enviar mensaje de prueba

```bash
./test-producer.sh
```

#### Verificar base de datos

```bash
# Ver todas las órdenes
./check-orders.sh

# Ver orden específica
./check-orders.sh 3d034ee8-7da0-498a-bed6-f52704591b0c
```

---

## 📚 Endpoints Disponibles

### API REST Completa

| Endpoint | Método | Descripción | Equivalente Shell |
|----------|--------|-------------|-------------------|
| `/api/v1/orders/kafka/send-test` | POST | Enviar mensaje de prueba a Kafka | `test-producer.sh` |
| `/api/v1/orders/kafka/send` | POST | Enviar mensaje custom a Kafka | - |
| `/api/v1/orders/database/check` | GET | Ver todas las órdenes en BD | `check-orders.sh` |
| `/api/v1/orders/database/check/{id}` | GET | Ver orden específica + auditoría | `check-orders.sh {id}` |
| `/api/v1/orders/process` | POST | Procesar orden directamente | - |
| `/api/v1/orders/create-test` | POST | Crear orden de prueba | - |
| `/api/v1/orders/{orderId}` | GET | Obtener orden por ID | - |
| `/api/v1/orders` | GET | Listar órdenes (con filtros) | - |
| `/api/v1/orders/{orderId}/cancel` | POST | Cancelar orden | - |

### Actuator

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de la aplicación |
| `/actuator/info` | Información de la aplicación |
| `/actuator/metrics` | Métricas |
| `/actuator/prometheus` | Métricas Prometheus |

### Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## 🎬 Flujo de Procesamiento

### Vía Kafka (Asíncrono)

```
1. Producer → Kafka Topic (orders-created)
2. Kafka Consumer escucha el mensaje
3. OrderEventValidator valida el payload
4. OrderEventProcessor procesa:
   - Crea/Actualiza Order en PostgreSQL
   - Registra en OrderAuditLog
   - Envía mensaje a SQS
5. Consumer confirma (ack) el mensaje
```

### Vía API REST (Síncrono)

```
1. Cliente → POST /api/v1/orders/process
2. OrderEventValidator valida el payload
3. OrderEventProcessor procesa directamente
4. Retorna Order procesada inmediatamente
```

---

## 🧪 Escenarios de Prueba

### Escenario 1: Crear Orden

**Vía API:**
```bash
curl -X POST http://localhost:8080/api/v1/orders/kafka/send-test
```

**Vía Shell:**
```bash
./test-producer.sh
```

**Verificar:**
```bash
curl http://localhost:8080/api/v1/orders/database/check/{orderId}
```

### Escenario 2: Actualizar Orden

```bash
curl -X POST http://localhost:8080/api/v1/orders/kafka/send \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "{existing-order-id}",
    "customerId": "customer-123",
    "eventType": "UPDATED",
    "totalAmount": 300.00,
    "currency": "USD",
    "metadata": {
      "source": "test",
      "correlationId": "corr-456",
      "timestamp": "2026-07-28T22:00:00Z"
    }
  }'
```

### Escenario 3: Cancelar Orden

**Vía Kafka:**
```bash
# Cambiar eventType a CANCELLED
curl -X POST http://localhost:8080/api/v1/orders/kafka/send \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "{existing-order-id}",
    "eventType": "CANCELLED",
    ...
  }'
```

**Vía API directa:**
```bash
curl -X POST http://localhost:8080/api/v1/orders/{orderId}/cancel
```

---

## 📊 Monitoreo y Observabilidad

### Logs Estructurados

```bash
# Ver logs de la aplicación
docker logs -f ms-event-processor-app

# O si ejecutas localmente
mvn spring-boot:run
```

### Métricas

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas detalladas
curl http://localhost:8080/actuator/metrics

# Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Base de Datos

```bash
# Conectar a PostgreSQL
docker exec -it ms-event-processor-postgres psql -U postgres -d orderdb

# Ver todas las órdenes
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;

# Ver auditoría
SELECT * FROM order_audit_log ORDER BY processed_at DESC LIMIT 10;
```

---

## 🔍 Verificación Paso a Paso

### Paso 1: Enviar mensaje

```bash
curl -X POST http://localhost:8080/api/v1/orders/kafka/send-test
```

**Guardar el `orderId` de la respuesta.**

### Paso 2: Esperar procesamiento

Esperar 2-3 segundos para que el consumer procese el mensaje.

### Paso 3: Verificar en base de datos

```bash
curl http://localhost:8080/api/v1/orders/database/check/{orderId}
```

### Paso 4: Verificar auditoría

La respuesta del paso 3 incluirá el historial de auditoría.

---

## 🎓 Conceptos Demostrados

### 1. Event-Driven Architecture
- Desacoplamiento entre productores y consumidores
- Procesamiento asíncrono
- Resiliencia ante fallos

### 2. Java 21 Virtual Threads
- Miles de hilos concurrentes con mínimo overhead
- Configuración en `VirtualThreadConfig.java`

### 3. Idempotencia
- Procesar el mismo mensaje múltiples veces = mismo resultado
- Implementado en `OrderEventProcessor`

### 4. CQRS Pattern
- Separación de comandos (Kafka) y consultas (REST API)
- Base de datos optimizada para lectura

### 5. Audit Trail
- Registro completo de todas las operaciones
- Tabla `order_audit_log`

### 6. Retry con Backoff Exponencial
- Reintentos automáticos ante fallos transitorios
- Implementado en `RetryHelper`

### 7. Dead Letter Queue
- Mensajes que fallan después de máximos reintentos
- Cola `ordenes-procesadas-dlq`

---

## 🛠️ Troubleshooting

### Kafka no conecta

```bash
# Verificar que Kafka está corriendo
docker ps | grep kafka

# Ver logs de Kafka
docker logs ms-event-processor-kafka

# Crear tópicos manualmente
docker exec ms-event-processor-kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --create --topic orders-created \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1
```

### Base de datos vacía

```bash
# Verificar tablas
docker exec -it ms-event-processor-postgres \
  psql -U postgres -d orderdb -c "\dt"

# La aplicación crea las tablas automáticamente (ddl-auto: update)
```

### SQS no recibe mensajes

```bash
# Verificar que LocalStack está corriendo
docker ps | grep localstack

# Listar colas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Crear cola manualmente
aws --endpoint-url=http://localhost:4566 \
  sqs create-queue --queue-name ordenes-procesadas-queue
```

---

## 📝 Scripts de Referencia

### test-producer.sh

```bash
#!/bin/bash
# Genera UUIDs automáticamente
ORDER_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
# Crea JSON con datos de prueba
# Envía a Kafka via docker exec
```

### check-orders.sh

```bash
#!/bin/bash
# Sin parámetro: muestra todas las órdenes
# Con parámetro: muestra orden específica + auditoría
```

---

## 🎯 Puntos Clave para el Directo

1. **Dos formas de usar**: Scripts de shell vs API REST (hacen lo mismo)
2. **Procesamiento dual**: Asíncrono (Kafka) vs Síncrono (REST)
3. **Stack moderno**: Java 21 + Virtual Threads + Spring Boot 3
4. **Arquitectura real**: Kafka + PostgreSQL + SQS (no es un ejemplo simplificado)
5. **Observabilidad completa**: Logs, métricas, auditoría
6. **Testing robusto**: 39 tests unitarios + tests de integración con Testcontainers

---

## 🔗 Recursos

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **README.md**: Documentación técnica
- **TESTING.md**: Guía de pruebas

---

**¡Listo para el directo! 🚀**
