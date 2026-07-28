# Documento de Requerimientos

## Introducción

Este documento especifica los requerimientos para **ms-event-processor**, un microservicio event-driven construido con Java 21 y Spring Boot 3.x. El microservicio se integra con Apache Kafka para el consumo de eventos y AWS SQS para el reenvío de mensajes, siguiendo patrones empresariales con soporte completo de CI/CD.

El sistema procesa eventos de órdenes desde múltiples tópicos de Kafka, aplica lógica de negocio y reenvía mensajes procesados a una cola SQS para consumo posterior.

## Glosario

- **MS-Event-Processor**: El sistema de microservicio event-driven que consume, procesa y reenvía eventos de órdenes
- **Order_Event**: Un evento de dominio que representa la creación, modificación o cancelación de una orden
- **Kafka_Consumer**: El componente responsable de consumir mensajes de tópicos de Kafka
- **Kafka_Producer**: El componente responsable de publicar mensajes en tópicos de Kafka
- **SQS_Producer**: El componente responsable de enviar mensajes a colas AWS SQS
- **JWT_Authenticator**: El componente de seguridad responsable de la validación de tokens JWT y autenticación
- **Order_Entity**: La entidad JPA que representa datos de órdenes persistidos
- **Order_Repository**: El repositorio Spring Data JPA para persistencia de órdenes
- **Virtual_Thread**: Un hilo ligero provisto por Java 21 para procesamiento concurrente escalable
- **LocalStack**: Un stack de AWS local para probar la integración con SQS
- **KRaft_Mode**: Modo de protocolo de consenso de Kafka que opera sin Zookeeper

## Requerimientos

### Requerimiento 1: Consumo de Eventos Kafka

**Historia de Usuario:** Como integrador de sistemas, quiero que el microservicio consuma eventos de órdenes desde múltiples tópicos Kafka, para poder procesar órdenes de diferentes flujos de eventos.

#### Criterios de Aceptación

1. CUANDO se publica un mensaje en el tópico orders-created, EL Kafka_Consumer DEBERÁ consumir el mensaje dentro de 5000 milisegundos
2. CUANDO se publica un mensaje en el tópico orders-updates, EL Kafka_Consumer DEBERÁ consumir el mensaje dentro de 5000 milisegundos
3. CUANDO se publica un mensaje en el tópico orders-cancelled, EL Kafka_Consumer DEBERÁ consumir el mensaje dentro de 5000 milisegundos
4. CUANDO se consume un mensaje de cualquier tópico Kafka, EL Kafka_Consumer DEBERÁ deserializar el mensaje a un objeto Order_Event
5. CUANDO falla el consumo de un mensaje, EL Kafka_Consumer DEBERÁ registrar el error con todos los detalles de contexto

### Requerimiento 2: Procesamiento de Eventos de Órdenes

**Historia de Usuario:** Como analista de negocio, quiero que el microservicio aplique lógica de negocio a eventos de órdenes, para que las órdenes se procesen según las reglas de negocio.

#### Criterios de Aceptación

1. CUANDO se recibe un Order_Event, EL MS-Event-Processor DEBERÁ validar la estructura del payload del evento
2. SI la validación del payload falla, ENTONCES EL MS-Event-Processor DEBERÁ rechazar el evento y registrar un error de validación
3. CUANDO un Order_Event pasa la validación, EL MS-Event-Processor DEBERÁ aplicar la lógica de negocio apropiada según el tipo de evento
4. CUANDO se procesa un evento de orden creada, EL MS-Event-Processor DEBERÁ crear un nuevo registro Order_Entity
5. CUANDO se procesa un evento de actualización de orden, EL MS-Event-Processor DEBERÁ actualizar el registro Order_Entity existente
6. CUANDO se procesa un evento de cancelación de orden, EL MS-Event-Processor DEBERÁ marcar el registro Order_Entity como cancelado

### Requerimiento 3: Reenvío de Mensajes SQS

**Historia de Usuario:** Como operador de sistema downstream, quiero que las órdenes procesadas se reenvíen a SQS, para que los sistemas downstream puedan consumirlas de manera confiable.

#### Criterios de Aceptación

1. CUANDO un Order_Event se procesa exitosamente, EL SQS_Producer DEBERÁ reenviar el mensaje procesado a la cola ordenes-procesadas-queue
2. CUANDO se reenvía un mensaje a SQS, EL SQS_Producer DEBERÁ incluir todos los datos relevantes de la orden en el cuerpo del mensaje
3. SI el envío del mensaje SQS falla, ENTONCES EL SQS_Producer DEBERÁ reintentar la operación hasta 3 veces con backoff exponencial
4. CUANDO se alcanza el máximo de reintentos, EL SQS_Producer DEBERÁ registrar la falla y mover el mensaje a una cola dead-letter

### Requerimiento 4: Persistencia de Datos en PostgreSQL

**Historia de Usuario:** Como arquitecto de datos, quiero que los datos de órdenes persistan en PostgreSQL, para tener un registro confiable de todas las órdenes procesadas.

#### Criterios de Aceptación

1. CUANDO se crea un Order_Entity, EL Order_Repository DEBERÁ persistir la entidad en la base de datos PostgreSQL
2. CUANDO se actualiza un Order_Entity, EL Order_Repository DEBERÁ actualizar el registro correspondiente en la base de datos
3. CUANDO se consulta una orden por ID, EL Order_Repository DEBERÁ retornar el Order_Entity si existe
4. CUANDO una operación de base de datos falla, EL Order_Repository DEBERÁ lanzar una DataAccessException con detalles de error apropiados

### Requerimiento 5: Autenticación JWT y Seguridad

**Historia de Usuario:** Como administrador de seguridad, quiero que el microservicio valide tokens JWT, para que solo solicitudes autenticadas sean procesadas.

#### Criterios de Aceptación

1. CUANDO se recibe una solicitud, EL JWT_Authenticator DEBERÁ validar el token JWT del header Authorization
2. SI el token JWT falta, ENTONCES EL JWT_Authenticator DEBERÁ rechazar la solicitud con HTTP 401 Unauthorized
3. SI el token JWT está expirado, ENTONCES EL JWT_Authenticator DEBERÁ rechazar la solicitud con HTTP 401 Unauthorized
4. SI el token JWT es inválido, ENTONCES EL JWT_Authenticator DEBERÁ rechazar la solicitud con HTTP 401 Unauthorized
5. CUANDO el token JWT es válido, EL JWT_Authenticator DEBERÁ extraer los roles del usuario y establecer el contexto de seguridad

### Requerimiento 6: Configuración de Hilos Virtuales

**Historia de Usuario:** Como ingeniero de plataforma, quiero que el microservicio use hilos virtuales de Java 21, para lograr alto throughput con mínimo uso de recursos.

#### Criterios de Aceptación

1. CUANDO la aplicación inicia, EL MS-Event-Processor DEBERÁ configurar hilos virtuales como el executor por defecto
2. CUANDO se procesan solicitudes concurrentes, EL MS-Event-Processor DEBERÁ usar hilos virtuales para todas las operaciones asíncronas
3. CUANDO un hilo virtual se bloquea en I/O, EL MS-Event-Processor DEBERÁ permitir a la plataforma desmontar y programar otros hilos virtuales

### Requerimiento 7: Infraestructura de Desarrollo Local

**Historia de Usuario:** Como desarrollador, quiero infraestructura local via Docker Compose, para poder desarrollar y probar sin dependencias externas.

#### Criterios de Aceptación

1. CUANDO se ejecuta docker-compose up, LA infraestructura DEBERÁ iniciar LocalStack con servicio SQS en el puerto 4566
2. CUANDO se ejecuta docker-compose up, LA infraestructura DEBERÁ iniciar Kafka en modo KRaft en el puerto 9092
3. CUANDO el contenedor LocalStack inicia, LA infraestructura DEBERÁ ejecutar el script de inicialización para crear la cola ordenes-procesadas-queue
4. CUANDO el contenedor Kafka inicia, LA infraestructura DEBERÁ estar lista para aceptar conexiones dentro de 30 segundos

### Requerimiento 8: Pipeline CI/CD

**Historia de Usuario:** Como ingeniero DevOps, quiero CI/CD automatizado via GitHub Actions, para que la calidad de código y despliegue sean automatizados.

#### Criterios de Aceptación

1. CUANDO se hace push a las ramas main o develop, EL CI_Pipeline DEBERÁ disparar el job build-and-test
2. CUANDO un pull request targetea la rama main, EL CI_Pipeline DEBERÁ disparar el job build-and-test
3. CUANDO el job build-and-test ejecuta, EL CI_Pipeline DEBERÁ iniciar servicios LocalStack y Kafka como contenedores
4. CUANDO la infraestructura está lista, EL CI_Pipeline DEBERÁ crear la cola ordenes-procesadas-queue en LocalStack
5. CUANDO el build ejecuta, EL CI_Pipeline DEBERÁ correr ./mvnw clean verify con tests de integración
6. SI los tests fallan, ENTONCES EL CI_Pipeline DEBERÁ subir reportes de test como artefactos
7. CUANDO el build tiene éxito, EL CI_Pipeline DEBERÁ reportar estado de éxito

### Requerimiento 9: Manejo de Errores y Resiliencia

**Historia de Usuario:** Como ingeniero de confiabilidad, quiero manejo de errores robusto, para que el sistema se degrade gracefully bajo condiciones de falla.

#### Criterios de Aceptación

1. CUANDO ocurre una excepción inesperada durante el procesamiento de mensajes, EL MS-Event-Processor DEBERÁ registrar el error y continuar procesando mensajes subsiguientes
2. CUANDO el broker Kafka no está disponible, EL Kafka_Consumer DEBERÁ intentar reconectar con backoff exponencial
3. CUANDO el servicio SQS no está disponible, EL SQS_Producer DEBERÁ reintentar la operación hasta 3 veces antes de fallar
4. CUANDO la conexión a base de datos falla, EL Order_Repository DEBERÁ reintentar la conexión antes de lanzar una excepción
5. SI todos los intentos de reintento se agotan, ENTONCES EL MS-Event-Processor DEBERÁ registrar un error crítico con contexto completo

### Requerimiento 10: Monitoreo y Observabilidad

**Historia de Usuario:** Como ingeniero de operaciones, quiero métricas y logs de aplicación, para poder monitorear la salud del sistema y solucionar problemas.

#### Criterios de Aceptación

1. CUANDO se procesa un evento de orden, EL MS-Event-Processor DEBERÁ registrar los detalles del evento a nivel INFO
2. CUANDO ocurre un error, EL MS-Event-Processor DEBERÁ registrar el error a nivel ERROR con stack trace
3. CUANDO se envía un mensaje a SQS, EL SQS_Producer DEBERÁ registrar el message ID a nivel DEBUG
4. CUANDO la aplicación inicia, EL MS-Event-Processor DEBERÁ registrar el resumen de configuración a nivel INFO

## Restricciones Técnicas

### RT-1: Stack Tecnológico
- EL MS-Event-Processor DEBERÁ usar Java 21 como lenguaje de programación
- EL MS-Event-Processor DEBERÁ usar Spring Boot 3.3.x o 3.4.x como framework
- EL MS-Event-Processor DEBERÁ usar Spring Data JPA para acceso a base de datos
- EL MS-Event-Processor DEBERÁ usar Spring Security para autenticación y autorización
- EL MS-Event-Processor DEBERÁ usar Spring Kafka para integración con Kafka
- EL MS-Event-Processor DEBERÁ usar Spring Cloud AWS para integración con SQS

### RT-2: Estructura de Paquetes
- EL MS-Event-Processor DEBERÁ usar com.dev.app como paquete base
- EL MS-Event-Processor DEBERÁ organizar código en paquetes config, consumer, producer y otros por feature

### RT-3: Infraestructura
- EL MS-Event-Processor DEBERÁ usar PostgreSQL como base de datos relacional
- EL MS-Event-Processor DEBERÁ usar tópicos Kafka: orders-created, orders-updates, orders-cancelled
- EL MS-Event-Processor DEBERÁ usar cola SQS: ordenes-procesadas-queue
- EL MS-Event-Processor DEBERÁ usar LocalStack para pruebas locales de SQS
- EL MS-Event-Processor DEBERÁ usar Kafka en modo KRaft sin Zookeeper

### RT-4: Requerimientos CI/CD
- EL CI_Pipeline DEBERÁ usar GitHub Actions para integración continua
- EL CI_Pipeline DEBERÁ ejecutar en runner Ubuntu
- EL CI_Pipeline DEBERÁ usar Java 21 con distribución Temurin
- EL CI_Pipeline DEBERÁ cachear dependencias Maven

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

## Requerimientos No Funcionales

### RNF-1: Performance
- EL MS-Event-Processor DEBERÁ procesar al menos 1000 eventos de orden por segundo bajo carga normal
- EL MS-Event-Processor DEBERÁ responder a solicitudes de health check dentro de 100 milisegundos

### RNF-2: Confiabilidad
- EL MS-Event-Processor DEBERÁ mantener 99.9% de disponibilidad durante operación normal
- EL MS-Event-Processor DEBERÁ recuperarse de fallas transitorias sin intervención manual

### RNF-3: Mantenibilidad
- EL MS-Event-Processor DEBERÁ tener al menos 80% de cobertura de código en tests unitarios
- EL MS-Event-Processor DEBERÁ pasar todos los tests de integración antes del despliegue
