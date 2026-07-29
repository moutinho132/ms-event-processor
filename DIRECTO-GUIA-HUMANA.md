# Guion Humanizado para Transmisión en Vivo
## MS-Event-Processor: De la Idea al Código

---

## Antes de Empezar (5 minutos antes)

1. Ejecuta `docker compose up -d`
2. Abre http://localhost:4200 y http://localhost:8080/swagger-ui.html
3. Ten una terminal lista para mostrar logs
4. Prepara un vaso de agua, vas a hablar mucho

---

## Bloque 1: La Introducción (5 minutos)

### El Gancho

"Hola a todos! Hoy vamos a hacer algo diferente. No voy a mostrarles código aburrido. Les voy a mostrar CÓMO se construye un sistema real de microservicios."

### El Problema

"Imaginen esto: Tienen una tienda online. Cada vez que alguien compra, necesitan:
- Guardar la orden
- Enviar confirmación
- Actualizar inventario
- Notificar al equipo de envío

Si hacen todo esto al mismo tiempo, el cliente espera 10 segundos. ¿La solución? Eventos."

### El Concepto Clave

"Event-driven no es complicado. Es como un periódico:
1. Alguien publica una noticia (el evento)
2. Los suscriptores la reciben
3. Cada uno hace lo que necesita con esa información

Nadie espera a nadie. Todo funciona en paralelo."

---

## Bloque 2: La Demo Visual (10 minutos)

### Paso 1: Mostrar el Frontend

"Primero, vean esto..."

- Abre http://localhost:4200
- "Parece una lista simple, ¿verdad? Pero detrás hay una arquitectura completa"

### Paso 2: Crear una Orden

"Hagamos click en 'Crear Orden Rápida'..."

[Espera a que procese]

"En menos de un segundo, esto pasó por:
1. El frontend hizo POST al backend
2. El backend guardó en PostgreSQL
3. Envió mensaje a SQS
4. Y si activaron las notificaciones... ¡BUM! Una notificación push en su navegador"

### Paso 3: El Detalle de la Orden

"Ahora hagamos click en una orden..."

- Muestra la vista de detalle
- "Aquí pueden ver:
  - Información del cliente
  - Items de la orden
  - Y el historial de auditoría - cada cosa que pasó con esta orden"

### Paso 4: Agregar Items

"¿Qué pasa si quiero agregar un item? Miren esto..."

- Click en "Agregar Item"
- Llena el formulario
- "El producto se agrega y el total se recalcula automáticamente"

### Paso 5: Transición de Estados

"Esto es lo más interesante. Una orden tiene estados:
- PENDING → Recién creada
- PROCESSING → Se está preparando
- PROCESSED → Lista para enviar
- CANCELLED → Cancelada"

[Demuestra la transición]

"Cuando cambio a 'PROCESSED', automáticamente:
1. Se actualiza en la base de datos
2. Se envía un evento a SQS
3. Y se envía un EMAIL al cliente vía SNS"

### Paso 6: Cancelar con Kafka

"¿Y si necesito cancelar? Miren el modal..."

- Abre el modal de cancelación
- "Puedo escribir la razón..."
- "Y cuando confirmo, se envía a KAFKA. Todos los servicios que estén escuchando recibirán la notificación"

---

## Bloque 3: La Arquitectura (10 minutos)

### El Diagrama Mental

"No voy a mostrarles un diagrama aburrido. Mejor imaginemos esto:

```
Frontend (Angular)
    ↓
API Gateway (Nginx)
    ↓
Backend (Java 21)
    ↓
    ├── PostgreSQL (guarda datos)
    ├── Kafka (recibe eventos)
    ├── SQS (cola de salida)
    └── SNS (notificaciones)
```

Todo esto corre en tu laptop con un solo comando: `docker compose up`"

### Los Números que Impresionan

"¿Por qué esta arquitectura es especial?
- 39+ tests pasando
- 3 Kafka consumers escuchando
- 5 servicios Docker en paralelo
- Java 21 Virtual Threads (puedo manejar miles de conexiones)
- Idempotencia: el mismo mensaje 10 veces = se procesa una vez"

### La Magia de Virtual Threads

"Java 21 trajo algo increíble: Virtual Threads.

Antes: Cada hilo consumía 1MB de memoria. 1000 hilos = 1GB solo en hilos.

Ahora: Los Virtual Threads son 'virtuales'. Puedo tener 100,000 hilos usando la misma memoria.

Es como tener un equipo pequeño que hace el trabajo de uno gigante."

---

## Bloque 4: El Backend (10 minutos)

### Swagger UI

"Veamos qué hay detrás del curtain..."

- Abre http://localhost:8080/swagger-ui.html

"Todos estos endpoints hacen cosas específicas:
- POST /api/v1/orders/create-test → Crea orden de prueba
- POST /api/v1/orders/kafka/send-test → Envía a Kafka
- GET /api/v1/orders/database/check → Lista órdenes
- POST /api/v1/orders/{id}/cancel → Cancela y envía a Kafka
- POST /api/v1/orders/{id}/status → Cambia estado
- POST /api/v1/orders/{id}/items → Agrega items"

### Mostrar Logs

"Si queremos ver qué pasa internamente..."

```bash
docker compose logs -f app
```

"Aquí pueden ver cada mensaje que llega, cada evento que se procesa, cada error que ocurre."

---

## Bloque 5: El Feature Nuevo - Notificaciones (5 minutos)

### El Código de Notificaciones

"Hoy agregué notificaciones push. Miren qué simple:"

```typescript
// Servicio de notificaciones
notifyOrderCreated(orderId: string) {
  new Notification('Nueva Orden', {
    body: `Orden ${orderId} creada`
  });
}
```

"El navegador se encarga de todo. Solo pido permiso y envío la notificación."

### SNS para Emails

"Y cuando una orden se completa, usamos AWS SNS para enviar emails.

En desarrollo, LocalStack simula AWS. En producción, usarías AWS real.

El código no cambia. Solo la configuración."

---

## Bloque 6: Tips para el Público (5 minutos)

### Antes de Empezar

"Si quieren construir algo similar:
1. Empiecen con el dominio: ¿qué eventos existen?
2. Luego la infraestructura: Kafka, SQS, PostgreSQL
3. Finalmente el código: consumers, producers, APIs"

### Los Errores Comunes

"Lo que NO deben hacer:
- No usar eventos para todo (a veces un simple REST es suficiente)
- No olvidar idempotencia (los mensajes se duplican)
- No ignorar los errores (siempre habrá fallos)"

### La Herramienta Secreta

"Su mejor amigo en proyectos event-driven: los logs.

Sin buenos logs, debugging es imposible. Yo uso:
- correlationId para rastrear un evento
- timestamps para medir latencia
- niveles de log (DEBUG, INFO, ERROR)"

---

## Bloque 7: Q&A y Cierre (5 minutos)

### Preguntas Frecuentes

**"¿Por qué Java 21?"**
Por los Virtual Threads. Es un game-changer. Y es LTS hasta 2031.

**"¿Por qué Angular?"**
Estructura. Para proyectos enterprise, tener opiniones reduce la fricción del equipo.

**"¿Puedo usar esto en producción?"**
Sí, con modificaciones:
- Configurar secrets correctamente
- Usar SQS/SNS reales de AWS
- Agregar Kubernetes
- Implementar circuit breakers

**"¿Cuánto tiempo te tardaste?"**
Backend: un fin de semana. Frontend: otro fin de semana. Integración: una tarde.

### El Mensaje Final

"Este proyecto demuestra algo importante:

Construir microservicios modernos no tiene que ser complicado. Con las herramientas correctas y los patrones adecuados, puedes tener un sistema robusto, escalable y mantenible.

Lo que ven aquí - Kafka, PostgreSQL, SQS, SNS, Virtual Threads, Angular - es el stack que muchas empresas usan EN PRODUCCIÓN hoy.

La diferencia: aquí lo tienen funcionando en sus laptops con un solo comando."

### La Despedida

"Gracias por ver este directo. El código está en GitHub, la documentación está en los archivos DIRECTO-GUIA.md y DIRECTO-HISTORIA.md.

Y recuerden: la mejor forma de aprender es CONSTRUYENDO. Tomen este proyecto, rómpanlo, agréguenle features, vean cómo se comporta.

Esa es la verdadera ingeniería.

¡Nos vemos en el próximo directo!"

---

## Checklist Pre-Directo

- [ ] Docker compose ejecutándose
- [ ] Frontend accesible en localhost:4200
- [ ] Swagger UI accesible en localhost:8080/swagger-ui.html
- [ ] Terminal lista para logs
- [ ] Vaso de agua cerca
- [ ] Notificaciones del navegador activadas
- [ ] Microphone/webcam probados

---

## Notas Técnicas para Referencia Rápida

### Comandos Útiles

```bash
# Iniciar todo
docker compose up -d

# Ver logs
docker compose logs -f app

# Reiniciar un servicio
docker compose restart app

# Ver estado
docker compose ps

# Reconstruir después de cambios
docker compose up -d --build frontend
```

### URLs

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api/v1/orders
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

### Endpoints Principales

| Endpoint | Descripción |
|----------|-------------|
| POST /create-test | Crear orden rápida |
| POST /kafka/send-test | Enviar a Kafka |
| GET /database/check | Listar órdenes |
| GET /database/check/{id} | Detalle con auditoría |
| POST /{id}/cancel | Cancelar + Kafka |
| POST /{id}/status | Cambiar estado |
| POST /{id}/items | Agregar items |
| GET /{id}/valid-transitions | Ver transiciones válidas |
