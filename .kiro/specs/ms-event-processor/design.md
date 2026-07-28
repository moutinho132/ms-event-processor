# Documento de Diseño: MS-Event-Processor

## Descripción General

MS-Event-Processor es un microservicio event-driven construido con Java 21 y Spring Boot 3.x que consume eventos de órdenes desde múltiples tópicos Kafka, aplica lógica de negocio, persiste datos de órdenes en PostgreSQL y reenvía mensajes procesados a AWS SQS para consumo downstream.

### Stack Tecnológico

| Componente | Tecnología | Versión |
|-----------|------------|---------| 
| Lenguaje | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.x / 3.4.x |
| Mensajería (Entrada) | Apache Kafka + Spring Kafka | 7.5.0 |
| Mensajería (Salida) | AWS SQS + Spring Cloud AWS | 3.x |
| Base de Datos | PostgreSQL + Spring Data JPA | 15+ |
| Seguridad | Spring Security + JWT | - |
| Testing Local | LocalStack + Docker Compose | Latest |

---

## Arquitectura

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           MS-Event-Processor                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐│
│  │                         Capa de Consumers Kafka                              ││
│  │  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐            ││
│  │  │ OrderCreatedCons.│ │OrderUpdatedCons. │ │OrderCancelledCons│            ││
│  │  │  (orders-created)│ │(orders-updates)  │ │(orders-cancelled)│            ││
│  │  └────────┬─────────┘ └────────┬─────────┘ └────────┬─────────┘            ││
│  │           └────────────────────┼────────────────────┘                       ││
│  │                                ▼                                            ││
│  └─────────────────────────────────────────────────────────────────────────────┘│
│                                   │                                             │
│  ┌─────────────────────────────────────────────────────────────────────────────┐│
│  │                       Capa de Procesamiento de Eventos                       ││
│  │  ┌─────────────────────────────▼─────────────────────────────┐             ││
│  │  │              OrderEventProcessor                           │             ││
│  │  │  - Validar payload del evento                              │             ││
│  │  │  - Aplicar lógica de negocio según tipo de evento          │             ││
│  │  │  - Orquestar persistencia y reenvío                        │             ││
│  │  └─────────────────────────────┬─────────────────────────────┘             ││
│  └─────────────────────────────────────────────────────────────────────────────┘│
│          ┌────────────────────────┼────────────────────────┐                   │
│          ▼                        ▼                        ▼                   │
│  ┌───────────────┐      ┌──────────────────┐      ┌──────────────────┐        │
│  │   Seguridad   │      │  Acceso a Datos  │      │   Mensajería     │        │
│  │ ┌───────────┐ │      │ ┌──────────────┐ │      │ ┌──────────────┐ │        │
│  │ │JWT Auth.  │ │      │ │OrderRepo     │ │      │ │ SQS Producer │ │        │
│  │ └───────────┘ │      │ └──────────────┘ │      │ └──────────────┘ │        │
│  └───────────────┘      └────────┬─────────┘      └────────┬─────────┘        │
└───────────────────────────────────┼─────────────────────────┼──────────────────┘
                                    │                         │
                                    ▼                         ▼
                          ┌─────────────────┐      ┌─────────────────────┐
                          │   PostgreSQL    │      │    AWS SQS          │
                          │  (tablas orden) │      │ ordenes-procesadas  │
                          └─────────────────┘      │      -queue         │
                                    ▲              └─────────────────────┘
                                    │
                          ┌─────────────────┐
                          │ Apache Kafka    │
                          │ (Modo KRaft)    │
                          │ - orders-created│
                          │ - orders-updates│
                          │ - orders-cancel │
                          └─────────────────┘
```

### Flujo de Datos

1. Los consumidores Kafka escuchan sus respectivos tópicos (orders-created, orders-updates, orders-cancelled)
2. Los mensajes se deserializan a objetos OrderEvent
3. La autenticación JWT valida solicitudes API (cuando aplica)
4. OrderEventProcessor valida y aplica lógica de negocio
5. OrderEntity se persiste en PostgreSQL vía OrderRepository
6. El mensaje procesado se reenvía a SQS vía SQS Producer
7. Los errores se registran y reintentan según la política de retry

---

## Estructura de Paquetes

```
com.dev.app/
├── Application.java                    # Clase principal Spring Boot
├── config/
│   ├── KafkaConfig.java               # Configuración consumer/producer Kafka
│   ├── SqsConfig.java                 # Configuración cliente SQS
│   ├── SecurityConfig.java            # Configuración Spring Security
│   ├── VirtualThreadConfig.java       # Configuración executor hilos virtuales
│   └── RetryConfig.java               # Configuración templates de retry
├── consumer/
│   ├── OrderCreatedConsumer.java      # Consumer para tópico orders-created
│   ├── OrderUpdatedConsumer.java      # Consumer para tópico orders-updates
│   └── OrderCancelledConsumer.java    # Consumer para tópico orders-cancelled
├── producer/
│   └── SqsProducer.java               # Productor de mensajes SQS
├── processor/
│   ├── OrderEventProcessor.java       # Orquestador principal de procesamiento
│   └── OrderEventValidator.java       # Validación de payload de eventos
├── entity/
│   └── Order.java                     # Entidad JPA para órdenes
├── repository/
│   └── OrderRepository.java           # Repositorio Spring Data JPA
├── dto/
│   ├── OrderEvent.java                # DTO evento desde Kafka
│   └── SqsMessage.java                # DTO mensaje para SQS
├── security/
│   ├── JwtAuthenticator.java          # Lógica de validación JWT
│   └── JwtAuthenticationFilter.java   # Filtro Spring Security
├── exception/
│   ├── EventProcessingException.java  # Excepción base
│   ├── ValidationException.java       # Excepción validación payload
│   └── RetryExhaustedException.java   # Excepción máx reintentos alcanzado
└── util/
    └── RetryHelper.java               # Utilidad retry con backoff exponencial
```

---

## Modelos de Datos

### 5.1 Schema de Evento Kafka

**OrderEvent (Mensaje Kafka Entrante)**

```json
{
  "orderId": "string (UUID)",
  "customerId": "string (UUID)",
  "eventType": "CREATED | UPDATED | CANCELLED",
  "totalAmount": "decimal",
  "currency": "string (ISO 4217)",
  "items": [
    {
      "productId": "string (UUID)",
      "productName": "string",
      "quantity": "integer",
      "unitPrice": "decimal"
    }
  ],
  "shippingAddress": {
    "street": "string",
    "city": "string",
    "state": "string",
    "postalCode": "string",
    "country": "string"
  },
  "metadata": {
    "source": "string",
    "correlationId": "string (UUID)",
    "timestamp": "ISO 8601 datetime"
  }
}
```

**Modelo Java:**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String customerId;
    private OrderEventType eventType;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItem> items;
    private ShippingAddress shippingAddress;
    private EventMetadata metadata;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    private String source;
    private String correlationId;
    private Instant timestamp;
}
```

### 5.2 Schema de Mensaje SQS

**ProcessedOrderMessage (Mensaje SQS Saliente)**

```json
{
  "messageId": "string (UUID)",
  "orderId": "string (UUID)",
  "customerId": "string (UUID)",
  "status": "PENDING | CONFIRMED | CANCELLED",
  "totalAmount": "decimal",
  "currency": "string",
  "itemCount": "integer",
  "processedAt": "ISO 8601 datetime",
  "correlationId": "string (UUID)",
  "version": "1.0"
}
```

### 5.3 Schema de Base de Datos

**Entidad Order (JPA)**

```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    
    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String orderId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "items", columnDefinition = "jsonb")
    @Convert(converter = OrderItemListConverter.class)
    private List<OrderItem> items;
    
    @Column(name = "shipping_address", columnDefinition = "jsonb")
    @Convert(converter = ShippingAddressConverter.class)
    private ShippingAddress shippingAddress;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    
    @Version
    @Column(name = "version")
    private Long version;
}
```

**DDL (PostgreSQL)**

```sql
CREATE TABLE orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    items JSONB,
    shipping_address JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    version BIGINT,
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at);

CREATE TABLE order_audit_log (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(36),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processing_time_ms BIGINT,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(2000),
    
    CONSTRAINT chk_event_type CHECK (event_type IN ('CREATED', 'UPDATED', 'CANCELLED'))
);

CREATE INDEX idx_audit_order ON order_audit_log(order_id);
CREATE INDEX idx_audit_processed ON order_audit_log(processed_at);
```

---

## Gestión de Configuración

### 6.1 Configuración de Aplicación

**application.yml**

```yaml
spring:
  application:
    name: ms-event-processor
  
  threads:
    virtual:
      enabled: true
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:order-processor-group}
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false
      max-poll-records: 100
    listener:
      ack-mode: manual
    properties:
      spring.json.trusted.packages: "com.dev.app.model"
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:orderdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
```

---

## Estrategia de Testing

### Pirámide de Testing

| Tipo | Framework | Alcance |
|------|-----------|---------|
| **Tests Unitarios** | JUnit 5, Mockito, AssertJ | Componentes individuales aislados |
| **Tests de Integración** | Spring Boot Test, Testcontainers | Flujos end-to-end con contenedores reales |
| **Tests Basados en Propiedades** | jqwik o similar | Propiedades de correctitud universales |

**Cobertura de Tests:**
- 10 tests basados en propiedades (opcionales, marcados con `*`)
- 6 clases de test unitario cubriendo componentes core
- 5 clases de test de integración con Testcontainers
- Objetivo: 80% de cobertura de código (RNF-3)

**Testcontainers Utilizados:**
- Kafka (modo KRaft)
- PostgreSQL (postgres:15-alpine)
- LocalStack (servicio SQS)

---

## Propiedades de Correctitud

### Propiedad 1: Idempotencia del Procesamiento de Mensajes
- CUANDO el mismo evento de orden se procesa múltiples veces, EL MS-Event-Processor DEBERÁ producir el mismo resultado sin efectos secundarios duplicados

### Propiedad 2: Ordenamiento de Mensajes Dentro de Partición
- CUANDO se publican mensajes en la misma partición Kafka, EL Kafka_Consumer DEBERÁ procesarlos en el orden en que fueron publicados

### Propiedad 3: Sin Pérdida de Mensajes en Condiciones Normales
- CUANDO un mensaje se consume exitosamente de Kafka, EL MS-Event-Processor DEBERÁ procesarlo exitosamente o moverlo a una cola dead-letter

### Propiedad 4: Degradación Graceful
- CUANDO una dependencia downstream falla, EL MS-Event-Processor DEBERÁ continuar procesando mensajes para dependencias no afectadas

### Propiedad 5: Consistencia de Configuración
- CUANDO la aplicación inicia, EL MS-Event-Processor DEBERÁ validar todas las propiedades de configuración requeridas antes de aceptar solicitudes
