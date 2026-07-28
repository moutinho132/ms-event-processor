# Plan de Implementación: MS-Event-Processor

## Descripción General

Este plan de implementación proporciona un desglose exhaustivo de tareas para construir el microservicio MS-EventProcessor - un sistema event-driven construido con Java 21 y Spring Boot 3.x que consume eventos de órdenes desde tópicos Kafka, aplica lógica de negocio, persiste datos en PostgreSQL y reenvía mensajes procesados a AWS SQS.

Las tareas están organizadas siguiendo un enfoque de desarrollo incremental, comenzando con la configuración del proyecto, construyendo modelos de dominio core, implementando infraestructura de mensajería y finalmente agregando testing, CI/CD y documentación.

---

## Tareas

### 1. Configuración del Proyecto e Infraestructura

- [ ] 1.1 Crear estructura de proyecto Maven con pom.xml
  - Definir Java 21 con padre Spring Boot 3.3.x o 3.4.x
  - Agregar dependencias: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-kafka, spring-cloud-aws-starter-sqs, jjwt-api, lombok, driver postgresql
  - Configurar plugin Maven compiler para Java 21
  - Agregar dependencias de test: spring-boot-starter-test, testcontainers (Kafka, PostgreSQL, LocalStack), junit-jupiter, assertj, awaitility
  - Crear estructura de paquete base: com.dev.app
  - Crear clase principal Application.java con @SpringBootApplication
  - _Requerimientos: RT-1, RT-2_

- [ ] 1.2 Agregar scripts Maven Wrapper
  - Incluir scripts mvnw y mvnw.cmd para builds consistentes
  - Agregar .mvn/wrapper/maven-wrapper.jar y maven-wrapper.properties
  - Configurar wrapper para Maven 3.9.x
  - _Requerimientos: RT-1_

- [ ] 1.3 Crear archivo de configuración application.yml
  - Configurar spring.application.name: ms-event-processor
  - Habilitar hilos virtuales: spring.threads.virtual.enabled: true
  - Configurar datasource PostgreSQL con fallbacks de variables de entorno
  - Configurar JPA con dialect Hibernate para PostgreSQL
  - Configurar consumer Kafka con bootstrap-servers, group-id, deserializers
  - Configurar endpoint SQS de AWS y settings de región
  - Configurar settings JWT (secret, issuer, expiration)
  - Configurar endpoints actuator para health y metrics
  - Configurar niveles de logging
  - _Requerimientos: 6.1, 6.2, 6.3, RT-1_

- [ ] 1.4 Crear application-test.yml para perfil de testing
  - Configurar base de datos H2 in-memory para tests
  - Configurar bootstrap servers Kafka de test vía variable de entorno
  - Configurar endpoint SQS LocalStack (http://localhost:4566)
  - Setear credenciales de test para AWS
  - Setear secret key JWT de test
  - _Requerimientos: 7.1, 7.2_

### 2. Entidades de Dominio y Enums

- [ ] 2.1 Crear enum OrderStatus
  - Definir valores: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
  - _Requerimientos: 2.4, 2.6_

- [ ] 2.2 Crear enum OrderEventType
  - Definir valores: CREATED, UPDATED, CANCELLED
  - _Requerimientos: 1.4, 2.3_

- [ ] 2.3 Crear entidad Order
  - Definir entidad JPA con @Entity, @Table(name = "orders")
  - Agregar campos: id (PK), orderId (unique), customerId, productId, quantity, unitPrice, totalAmount, status, shippingAddress, notes, createdAt, updatedAt, processedAt, version
  - Agregar índices en order_id, customer_id, status, created_at
  - Usar @CreationTimestamp y @UpdateTimestamp para campos de auditoría
  - Agregar @Version para locking optimista
  - _Requerimientos: 4.1, 4.2, 4.3, 4.4_

- [ ] 2.4 Crear entidad OrderItem
  - Definir campos: productId, productName, quantity, unitPrice
  - Implementar como embeddable o entidad separada según diseño
  - _Requerimientos: 2.4_

- [ ] 2.5 Crear entidad OrderAudit
  - Definir entidad JPA con @Table(name = "order_audit_log")
  - Agregar campos: id, orderId, eventType, oldStatus, newStatus, correlationId, processedAt, processingTimeMs, success, errorMessage
  - Agregar índices en order_id, processed_at
  - _Requerimientos: 10.1, 10.2_

- [ ]* 2.6 Escribir test de propiedad para serialización de entidad Order
  - **Propiedad 1: Serialización Round-Trip de Evento de Orden**
  - Testear que cualquier Order válida puede serializarse/deserializarse correctamente
  - **Valida: Requerimientos 1.4**

### 3. DTOs y Objetos de Transferencia de Datos

- [ ] 3.1 Crear DTO OrderEvent
  - Definir campos matching schema de mensaje Kafka: orderId, customerId, eventType, totalAmount, currency, items, shippingAddress, metadata
  - Agregar anotaciones Jackson para serialización JSON
  - Agregar anotaciones de validación (@NotNull, @NotBlank donde aplique)
  - _Requerimientos: 1.4, 2.1_

- [ ] 3.2 Crear DTO OrderItem
  - Definir campos: productId, productName, quantity, unitPrice
  - Agregar validación para quantity >= 1 y unitPrice >= 0
  - _Requerimientos: 2.1_

- [ ] 3.3 Crear DTO ShippingAddress
  - Definir campos: street, city, state, postalCode, country
  - _Requerimientos: 2.1_

- [ ] 3.4 Crear DTO EventMetadata
  - Definir campos: source, correlationId, timestamp
  - _Requerimientos: 2.1_

- [ ] 3.5 Crear DTO ProcessedOrderMessage
  - Definir campos para output SQS: messageId, orderId, customerId, status, totalAmount, currency, itemCount, processedAt, correlationId, version
  - Agregar método factory fromOrder(Order entity)
  - Agregar método toJson() para serialización
  - _Requerimientos: 3.2_

- [ ] 3.6 Crear DTO SqsMessage
  - Definir campos: orderId, customerId, productId, quantity, totalAmount, status, processedAt, source
  - Agregar método factory estático fromOrder()
  - Agregar método de serialización toJson()
  - _Requerimientos: 3.2_

- [ ] 3.7 Crear DTO ErrorResponse
  - Definir campos: errorCode, message, details, timestamp
  - Agregar patrón builder para fácil construcción
  - _Requerimientos: 9.1_

### 4. Capa de Repositorios

- [ ] 4.1 Crear interfaz OrderRepository
  - Extender JpaRepository<Order, Long>
  - Agregar método: Optional<Order> findByOrderId(String orderId)
  - Agregar método: List<Order> findByCustomerId(String customerId)
  - Agregar método: List<Order> findByStatus(OrderStatus status)
  - Agregar método: boolean existsByOrderId(String orderId)
  - _Requerimientos: 4.1, 4.2, 4.3, 4.4_

- [ ] 4.2 Crear interfaz OrderAuditRepository
  - Extender JpaRepository<OrderAudit, Long>
  - Agregar método: List<OrderAudit> findByOrderId(String orderId)
  - _Requerimientos: 4.1, 10.1_

- [ ]* 4.3 Escribir test de propiedad para lookup de repositorio Order
  - **Propiedad 6: Lookup de Repositorio Order**
  - Testear que cualquier Order persistido puede recuperarse por orderId
  - **Valida: Requerimientos 4.3**

### 5. Clases de Configuración

- [ ] 5.1 Crear VirtualThreadConfig
  - Definir @Bean para TaskExecutor con executor de hilo virtual
  - Definir @Bean para TomcatProtocolHandlerCustomizer con hilos virtuales
  - Configurar Executors.newVirtualThreadPerTaskExecutor()
  - _Requerimientos: 6.1, 6.2, 6.3_

- [ ] 5.2 Crear KafkaConfig
  - Configurar ConcurrentKafkaListenerContainerFactory
  - Setear executor de hilo virtual para listener task executor
  - Configurar modo manual ack
  - Configurar JsonDeserializer con trusted packages
  - _Requerimientos: 1.1, 1.2, 1.3, 1.4_

- [ ] 5.3 Crear SqsConfig
  - Configurar bean SqsClient con endpoint override para LocalStack
  - Configurar credentials provider con variables de entorno
  - Setear región desde configuración
  - _Requerimientos: 3.1, 3.2, 3.3_

- [ ] 5.4 Crear RetryConfig
  - Habilitar soporte @Retryable
  - Configurar RetryTemplate con backoff exponencial
  - Setear max attempts: 3, initial interval: 1000ms, multiplier: 2.0
  - _Requerimientos: 3.3, 9.3_

### 6. Consumers Kafka

- [ ] 6.1 Crear OrderCreatedConsumer
  - Implementar @KafkaListener para tópico orders-created
  - Inyectar dependencia OrderEventProcessor
  - Deserializar mensaje a OrderEvent
  - Setear eventType a CREATED
  - Agregar logging para mensaje recibido y status de procesamiento
  - _Requerimientos: 1.1, 1.4, 1.5_

- [ ] 6.2 Crear OrderUpdatedConsumer
  - Implementar @KafkaListener para tópico orders-updates
  - Inyectar dependencia OrderEventProcessor
  - Deserializar mensaje a OrderEvent
  - Setear eventType a UPDATED
  - Agregar logging para mensaje recibido y status de procesamiento
  - _Requerimientos: 1.2, 1.4, 1.5_

- [ ] 6.3 Crear OrderCancelledConsumer
  - Implementar @KafkaListener para tópico orders-cancelled
  - Inyectar dependencia OrderEventProcessor
  - Deserializar mensaje a OrderEvent
  - Setear eventType a CANCELLED
  - Agregar logging para mensaje recibido y status de procesamiento
  - _Requerimientos: 1.3, 1.4, 1.5_

### 7. Capa de Procesamiento de Eventos

- [ ] 7.1 Crear OrderEventValidator
  - Implementar método validate(OrderEvent)
  - Validar campos requeridos: orderId, customerId, eventType
  - Validar que eventType sea uno de CREATED, UPDATED, CANCELLED
  - Validar totalAmount >= 0 si está presente
  - Validar quantity >= 1 para items si están presentes
  - Lanzar ValidationException con lista de errores detallada en caso de falla
  - _Requerimientos: 2.1, 2.2_

- [ ] 7.2 Crear OrderEventProcessor
  - Implementar método process(OrderEvent) con @Transactional
  - Inyectar OrderEventValidator, OrderRepository, SqsProducer
  - Validar evento usando validator
  - Enrutar a handler apropiado según eventType (CREATED, UPDATED, CANCELLED)
  - Persistir orden en base de datos
  - Reenviar mensaje procesado a SQS
  - Agregar logging estructurado con orderId y eventType
  - _Requerimientos: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 7.3 Implementar método handleOrderCreated
  - Verificar si la orden ya existe (idempotencia)
  - Crear nueva entidad Order desde datos del evento
  - Setear status a PENDING inicialmente
  - Calcular totalAmount desde quantity y unitPrice
  - Setear timestamps createdAt y processedAt
  - Guardar en repositorio
  - _Requerimientos: 2.4_

- [ ] 7.4 Implementar método handleOrderUpdated
  - Buscar orden existente por orderId
  - Lanzar EventProcessingException si no se encuentra
  - Actualizar campos desde evento (quantity, unitPrice, etc.)
  - Recalcular totalAmount si es necesario
  - Actualizar timestamp updatedAt
  - Guardar entidad actualizada
  - _Requerimientos: 2.5_

- [ ] 7.5 Implementar método handleOrderCancelled
  - Buscar orden existente por orderId
  - Lanzar EventProcessingException si no se encuentra
  - Actualizar status a CANCELLED
  - Actualizar timestamp updatedAt
  - Guardar entidad actualizada
  - _Requerimientos: 2.6_

- [ ]* 7.6 Escribir test de propiedad para routing de tipo de evento
  - **Propiedad 3: Correctitud de Routing de Tipo de Evento**
  - Testear que cualquier OrderEvent enruta a la lógica de negocio correcta
  - **Valida: Requerimientos 2.3, 2.4, 2.5, 2.6**

- [ ]* 7.7 Escribir test de propiedad para procesamiento idempotente
  - **Propiedad 10: Procesamiento Idempotente**
  - Testear que procesar el mismo evento múltiples veces produce el mismo resultado
  - **Valida: Requerimientos (Propiedad de Correctitud 1)**

### 8. Productor SQS

- [ ] 8.1 Crear SqsProducer
  - Inyectar SqsClient y RetryHelper
  - Implementar método send(SqsMessage)
  - Construir SendMessageRequest con queue URL y message body
  - Agregar messageGroupId para ordenamiento FIFO
  - Implementar lógica de retry con backoff exponencial (hasta 3 reintentos)
  - Registrar message ID en envío exitoso a nivel DEBUG
  - _Requerimientos: 3.1, 3.2, 3.3_

- [ ] 8.2 Implementar manejo de cola dead-letter
  - Crear método sendToDeadLetterQueue
  - Registrar falla con contexto completo a nivel ERROR
  - Incluir mensaje original y detalles del error
  - _Requerimientos: 3.4_

- [ ] 8.3 Crear clase utilitaria RetryHelper
  - Implementar executeWithRetry(RetryCallback, maxRetries, operation)
  - Implementar backoff exponencial con intervalo inicial configurable, multiplier, max interval
  - Manejar InterruptedException apropiadamente (restaurar flag de interrupción)
  - Lanzar RetryExhaustedException cuando se alcanzan máx reintentos
  - _Requerimientos: 3.3, 9.3_

- [ ]* 8.4 Escribir test de propiedad para comportamiento de retry
  - **Propiedad 5: Correctitud de Comportamiento de Retry**
  - Testear que operaciones fallidas reintentan exactamente las veces configuradas
  - **Valida: Requerimientos 3.3, 9.3**

- [ ]* 8.5 Escribir test de propiedad para completitud de mensaje SQS
  - **Propiedad 4: Completitud del Body del Mensaje SQS**
  - Testear que todos los campos requeridos están incluidos en el mensaje SQS
  - **Valida: Requerimientos 3.2**

### 9. Capa de Seguridad

- [ ] 9.1 Crear SecurityConfig
  - Configurar SecurityFilterChain con sesión stateless
  - Deshabilitar CSRF para API stateless
  - Permitir todas las solicitudes a /actuator/health y /actuator/info
  - Requerir autenticación para todas las demás solicitudes
  - Agregar JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter
  - _Requerimientos: 5.1, 5.2, 5.3, 5.4_

- [ ] 9.2 Crear JwtTokenProvider
  - Inicializar JwtParser con signing key desde configuración
  - Implementar validateToken(String token) retornando boolean
  - Implementar getUserPrincipal(String token) extrayendo claims
  - Manejar ExpiredJwtException, MalformedJwtException, SignatureException
  - Extraer roles de claims y convertir a GrantedAuthority
  - _Requerimientos: 5.1, 5.3, 5.4, 5.5_

- [ ] 9.3 Crear JwtAuthenticationFilter
  - Extender OncePerRequestFilter
  - Extraer token JWT del header Authorization (prefijo Bearer)
  - Validar token usando JwtTokenProvider
  - Setear SecurityContext con Authentication en caso de éxito
  - Retornar HTTP 401 en token faltante o inválido
  - Agregar debug logging para eventos de autenticación
  - _Requerimientos: 5.1, 5.2, 5.3, 5.4_

- [ ] 9.4 Crear UserPrincipal
  - Implementar interfaz UserDetails o principal custom
  - Incluir userId, username, authorities
  - Agregar patrón builder para construcción
  - _Requerimientos: 5.5_

- [ ]* 9.5 Escribir test de propiedad para validación JWT
  - **Propiedad 7: Correctitud de Validación JWT**
  - Testear que tokens válidos pasan y tokens inválidos fallan
  - **Valida: Requerimientos 5.1, 5.2, 5.3, 5.4**

- [ ]* 9.6 Escribir test de propiedad para extracción de roles JWT
  - **Propiedad 8: Extracción de Roles JWT**
  - Testear que los roles se extraen correctamente y se setean en security context
  - **Valida: Requerimientos 5.5**

### 10. Manejo de Errores

- [ ] 10.1 Crear clases de excepción
  - Crear clase base EventProcessingException con campos orderId y eventType
  - Crear ValidationException extendiendo EventProcessingException con lista de errores de validación
  - Crear RetryExhaustedException extendiendo EventProcessingException con retry count
  - Crear JwtValidationException para errores específicos de JWT
  - _Requerimientos: 9.1, 9.5_

- [ ] 10.2 Crear GlobalExceptionHandler
  - Anotar con @RestControllerAdvice
  - Manejar ValidationException -> HTTP 400 con detalles de error
  - Manejar EventProcessingException -> HTTP 500
  - Manejar RetryExhaustedException -> HTTP 503 Service Unavailable
  - Manejar JwtValidationException -> HTTP 401 Unauthorized
  - Registrar todos los errores en niveles apropiados
  - _Requerimientos: 9.1, 9.5_

- [ ] 10.3 Implementar manejo graceful de errores en consumers
  - Envolver procesamiento de consumer en try-catch
  - Registrar detalles de error y continuar procesando en caso de falla
  - No re-lanzar excepciones para evitar detención del consumer
  - _Requerimientos: 9.1_

- [ ]* 10.4 Escribir test de propiedad para continuación de procesamiento
  - **Propiedad 9: Continuación de Procesamiento Bajo Error**
  - Testear que las excepciones no detienen el procesamiento de mensajes subsiguientes
  - **Valida: Requerimientos 9.1**

### 11. Docker Compose para Desarrollo Local

- [ ] 11.1 Crear docker-compose.yml
  - Definir servicio LocalStack con SQS en puerto 4566
  - Definir servicio Kafka en modo KRaft en puerto 9092
  - Definir servicio PostgreSQL en puerto 5432
  - Configurar variables de entorno para cada servicio
  - Setear health checks para readiness de servicios
  - _Requerimientos: 7.1, 7.2, 7.3, 7.4_

- [ ] 11.2 Configurar Kafka en modo KRaft
  - Setear KAFKA_NODE_ID y KAFKA_PROCESS_ROLES
  - Configurar KAFKA_CONTROLLER_QUORUM_VOTERS
  - Setear KAFKA_LISTENERS y KAFKA_ADVERTISED_LISTENERS
  - Configurar CLUSTER_ID
  - _Requerimientos: 7.2, 7.4_

- [ ] 11.3 Configurar inicialización de LocalStack
  - Crear script init para crear cola ordenes-procesadas-queue
  - Crear cola dead-letter: ordenes-procesadas-dlq
  - Setear environment AWS_DEFAULT_REGION y SERVICES
  - _Requerimientos: 7.1, 7.3_

- [ ] 11.4 Crear sección de README para desarrollo local
  - Documentar comando docker-compose up -d
  - Documentar cómo verificar que los servicios están corriendo
  - Documentar cómo crear cola SQS manualmente si es necesario
  - _Requerimientos: 7.1, 7.2_

### 12. Pipeline CI/CD GitHub Actions

- [ ] 12.1 Crear .github/workflows/ci.yml
  - Definir triggers de workflow: push a main/develop, PR a main
  - Configurar runner ubuntu-latest
  - _Requerimientos: 8.1, 8.2_

- [ ] 12.2 Configurar servicios del job CI
  - Configurar servicio LocalStack con SQS en puerto 4566
  - Configurar servicio Kafka en modo KRaft en puerto 9092
  - Setear variables de entorno para cada servicio
  - _Requerimientos: 8.3_

- [ ] 12.3 Configurar setup de Java 21
  - Usar actions/setup-java@v4 con distribución Temurin
  - Habilitar cache de Maven
  - _Requerimientos: RT-4_

- [ ] 12.4 Agregar paso de inicialización de LocalStack
  - Ejecutar comando AWS CLI para crear cola ordenes-procesadas-queue
  - Setear AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_DEFAULT_REGION
  - _Requerimientos: 8.4_

- [ ] 12.5 Agregar paso de build y test
  - Ejecutar ./mvnw clean verify
  - Setear variables de entorno: AWS_SQS_ENDPOINT, SPRING_KAFKA_BOOTSTRAP_SERVERS
  - _Requerimientos: 8.5_

- [ ] 12.6 Agregar upload de reportes de test en caso de falla
  - Usar actions/upload-artifact@v4
  - Subir directorio target/surefire-reports/
  - _Requerimientos: 8.6_

- [ ] 12.7 Verificar status de éxito del pipeline CI
  - Asegurar que el workflow reporta éxito en builds que pasan
  - _Requerimientos: 8.7_

### 13. Tests de Integración con Testcontainers

- [ ] 13.1 Crear clase base de test con Testcontainers
  - Anotar con @SpringBootTest, @Testcontainers
  - Definir KafkaContainer con modo KRaft
  - Definir PostgreSQLContainer con postgres:15-alpine
  - Definir LocalStackContainer con servicio SQS
  - Configurar @DynamicPropertySource para propiedades de contenedores
  - _Requerimientos: 1.1, 1.2, 1.3, 4.1_

- [ ] 13.2 Crear OrderProcessingIntegrationTest
  - Testear evento de orden creada end-to-end (Kafka -> DB -> SQS)
  - Testear evento de orden actualizada end-to-end
  - Testear evento de orden cancelada end-to-end
  - Usar Awaitility para aserciones async
  - Verificar datos persistidos correctamente en base de datos
  - Verificar mensaje reenviado a SQS
  - _Requerimientos: 1.1, 1.2, 1.3, 2.4, 2.5, 2.6, 3.1_

- [ ] 13.3 Crear KafkaConsumerIntegrationTest
  - Testear que el consumer recibe mensajes dentro del timeout
  - Testear que el consumer maneja errores de deserialización
  - Testear que el consumer continúa después de error
  - _Requerimientos: 1.1, 1.2, 1.3, 1.5_

- [ ] 13.4 Crear SqsProducerIntegrationTest
  - Testear mensaje enviado a SQS exitosamente
  - Testear mecanismo de retry en falla
  - Testear cola dead-letter en máx reintentos
  - _Requerimientos: 3.1, 3.3, 3.4_

- [ ] 13.5 Crear SecurityIntegrationTest
  - Testear token JWT válido aceptado
  - Testear token JWT faltante rechazado con 401
  - Testear token JWT expirado rechazado con 401
  - Testear token JWT inválido rechazado con 401
  - Testear roles extraídos correctamente
  - _Requerimientos: 5.1, 5.2, 5.3, 5.4, 5.5_

### 14. Tests Unitarios

- [ ] 14.1 Crear OrderEventProcessorTest
  - Testear process() con evento CREATED crea nueva orden
  - Testear process() con evento UPDATED actualiza orden existente
  - Testear process() con evento CANCELLED cancela orden
  - Testear process() lanza excepción en falla de validación
  - Testear process() lanza excepción en orden no encontrada para update/cancel
  - Mokear dependencias: validator, repository, sqsProducer
  - _Requerimientos: 2.3, 2.4, 2.5, 2.6_

- [ ] 14.2 Crear OrderEventValidatorTest
  - Testear evento válido pasa validación
  - Testear orderId faltante falla validación
  - Testear customerId faltante falla validación
  - Testear eventType inválido falla validación
  - Testear totalAmount negativo falla validación
  - Testear quantity inválido falla validación
  - _Requerimientos: 2.1, 2.2_

- [ ] 14.3 Crear SqsProducerTest
  - Testear send() envía mensaje exitosamente
  - Testear send() reintenta en falla
  - Testear send() lanza RetryExhaustedException después de máx reintentos
  - Mokear SqsClient
  - _Requerimientos: 3.1, 3.3, 3.4_

- [ ] 14.4 Crear JwtTokenProviderTest
  - Testear validateToken() retorna true para token válido
  - Testear validateToken() retorna false para token expirado
  - Testear validateToken() retorna false para firma inválida
  - Testear validateToken() retorna false para token malformado
  - Testear getUserPrincipal() extrae claims correctamente
  - _Requerimientos: 5.1, 5.3, 5.4, 5.5_

- [ ] 14.5 Crear JwtAuthenticationFilterTest
  - Testear filter autentica token válido
  - Testear filter rechaza token faltante con 401
  - Testear filter rechaza token inválido con 401
  - Mokear HttpServletRequest, HttpServletResponse, FilterChain
  - _Requerimientos: 5.1, 5.2, 5.3, 5.4_

- [ ] 14.6 Crear GlobalExceptionHandlerTest
  - Testear ValidationException retorna 400
  - Testear EventProcessingException retorna 500
  - Testear RetryExhaustedException retorna 503
  - Testear JwtValidationException retorna 401
  - _Requerimientos: 9.1, 9.5_

### 15. Checkpoint - Verificar Funcionalidad Core

- [ ] 15. Checkpoint - Asegurar que todos los tests pasan
  - Ejecutar ./mvnw clean verify localmente
  - Verificar que todos los tests unitarios pasan
  - Verificar que todos los tests de integración pasan con Testcontainers
  - Verificar que la cobertura de código alcanza el umbral de 80%
  - Preguntar al usuario si surgen dudas.

### 16. Actualizaciones de Documentación

- [ ] 16.1 Actualizar README.md con descripción del proyecto
  - Agregar descripción y propósito del proyecto
  - Agregar referencia al diagrama de arquitectura
  - Listar stack tecnológico
  - _Requerimientos: N/A_

- [ ] 16.2 Documentar setup de desarrollo local
  - Documentar prerequisitos (Java 21, Docker, Maven)
  - Documentar setup de docker-compose
  - Documentar variables de entorno
  - Documentar cómo ejecutar la aplicación localmente
  - _Requerimientos: 7.1, 7.2_

- [ ] 16.3 Documentar API y configuración
  - Documentar tópicos Kafka y formatos de mensaje
  - Documentar cola SQS y formato de mensaje
  - Documentar requerimientos de autenticación JWT
  - Documentar endpoints actuator disponibles
  - _Requerimientos: 1.1, 1.2, 1.3, 3.1, 5.1_

- [ ] 16.4 Documentar enfoque de testing
  - Documentar cómo ejecutar tests unitarios
  - Documentar cómo ejecutar tests de integración
  - Documentar uso de Testcontainers
  - _Requerimientos: RNF-3_

- [ ] 16.5 Documentar pipeline CI/CD
  - Documentar workflow de GitHub Actions
  - Documentar secrets y variables de entorno requeridas
  - _Requerimientos: 8.1_

### 17. Checkpoint Final

- [ ] 17. Checkpoint - Verificación final
  - Asegurar que todos los tests pasan: ./mvnw clean verify
  - Verificar que el pipeline CI pasa en GitHub Actions
  - Verificar que el entorno de desarrollo local funciona con docker-compose
  - Verificar que la documentación está completa
  - Preguntar al usuario si surgen dudas.

---

## Notas

- Las tareas marcadas con `*` son opcionales y pueden saltarse para un MVP más rápido
- Cada tarea referencia requerimientos específicos para trazabilidad
- Los checkpoints aseguran validación incremental
- Los tests de propiedad validan propiedades de correctitud universales del diseño
- Los tests unitarios validan ejemplos específicos y casos borde
- Los tests de integración verifican flujos end-to-end con Testcontainers

---

## Grafo de Dependencias de Tareas

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4", "2.5", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7"] },
    { "id": 2, "tasks": ["4.1", "4.2", "5.1", "5.2", "5.3", "5.4", "10.1"] },
    { "id": 3, "tasks": ["6.1", "6.2", "6.3", "7.1", "8.1", "8.3", "9.1", "9.2", "9.4"] },
    { "id": 4, "tasks": ["7.2", "7.3", "7.4", "7.5", "8.2", "9.3", "10.2", "10.3"] },
    { "id": 5, "tasks": ["2.6", "4.3", "7.6", "7.7", "8.4", "8.5", "9.5", "9.6", "10.4"] },
    { "id": 6, "tasks": ["11.1", "11.2", "11.3", "11.4", "12.1", "12.2", "12.3"] },
    { "id": 7, "tasks": ["12.4", "12.5", "12.6", "12.7"] },
    { "id": 8, "tasks": ["13.1", "13.2", "13.3", "13.4", "13.5"] },
    { "id": 9, "tasks": ["14.1", "14.2", "14.3", "14.4", "14.5", "14.6"] },
    { "id": 10, "tasks": ["16.1", "16.2", "16.3", "16.4", "16.5"] }
  ]
}
```
