# MS-Event-Processor - Guía Completa

> Un proyecto real de microservicios event-driven que puedes ejecutar en tu laptop con un solo comando.

---

## ¿Qué es este proyecto?

**MS-Event-Processor** es un microservicio que demuestra cómo construir sistemas modernos basados en eventos. No es un ejemplo simplificado - es una arquitectura que podrías encontrar en producción.

### El problema que resuelve

Imagina una tienda online. Cada vez que alguien hace un pedido:
- El sistema guarda la orden
- Envía confirmaciones
- Actualiza inventario
- Notifica al equipo de envío

Si todo esto pasa de forma síncrona, el cliente espera 10 segundos. ¿La solución? **Eventos**.

### La arquitectura en palabras simples

```
Frontend (Angular 18)
    ↓
Backend (Java 21 + Spring Boot)
    ↓
    ├── PostgreSQL (guarda datos)
    ├── Kafka (recibe eventos)
    ├── SQS (cola de salida)
    └── SNS (notificaciones)
```

Todo corre en tu laptop con: `docker compose up -d`

---

## Stack Tecnológico

| Tecnología | Propósito |
|-----------|-----------|
| Java 21 LTS | Runtime con Virtual Threads |
| Spring Boot 3.x | Framework principal |
| Angular 18 | Frontend SPA |
| Apache Kafka | Message Broker (KRaft mode) |
| PostgreSQL | Base de datos relacional |
| LocalStack | Simulación local de AWS (SQS, SNS, Lambda) |
| Docker | Containerización |

---

## Requisitos Previos

- Docker y Docker Compose
- 8GB RAM mínimo (16GB recomendado)
- Puertos disponibles: 4200, 5432, 4566, 8080, 8081, 9092

---

## Inicio Rápido

### 1. Clonar y configurar

```bash
git clone https://github.com/moutinho132/ms-event-processor.git
cd ms-event-processor

# Copiar variables de entorno
cp .env.example .env
# Edita .env con tus configuraciones si es necesario
```

### 2. Levantar todo

```bash
docker compose up -d
```

Esto inicia:
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **LocalStack UI**: http://localhost:8081
- **PostgreSQL**: localhost:5432
- **Kafka**: localhost:9092
- **LocalStack**: localhost:4566

### 3. Verificar que todo funciona

```bash
# Health check
curl http://localhost:8080/actuator/health

# Ver estado de contenedores
docker compose ps
```

---

## Funcionalidades Principales

### 1. Gestión de Órdenes

- Crear órdenes vía API o Kafka
- Estados: PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
- Cancelación con eventos a Kafka
- Historial de auditoría completo

### 2. Autenticación JWT

- Registro e inicio de sesión
- Tokens con expiración de 24h
- Roles: CUSTOMER, SUPERVISOR, ADMIN

### 3. CRUD Completo

- **Productos**: Crear, leer, actualizar, eliminar
- **Usuarios**: Gestión con roles
- **Locales**: Puntos de venta
- **Almacenes**: Inventario por local
- **Stock**: Movimientos de inventario

### 4. Chat IA (Ollama)

```bash
# Instalar Ollama
brew install ollama          # macOS
# o descarga desde https://ollama.ai

# Descargar modelo
ollama pull llama3.2

# Iniciar servidor
ollama serve
```

### 5. Dashboard con Gráficas

- Distribución por estado
- Tendencia últimos 7 días
- Ingresos por estado

---

## API Endpoints

### Autenticación

```
POST /api/v1/auth/register   - Registrar usuario
POST /api/v1/auth/login      - Iniciar sesión
GET  /api/v1/auth/me         - Usuario actual
```

### Órdenes

```
GET    /api/v1/orders                  - Listar órdenes
GET    /api/v1/orders/{id}             - Detalle de orden
POST   /api/v1/orders/process          - Crear orden directa
POST   /api/v1/orders/kafka/send-test  - Enviar a Kafka
POST   /api/v1/orders/{id}/cancel      - Cancelar orden
POST   /api/v1/orders/{id}/status      - Cambiar estado
POST   /api/v1/orders/{id}/items       - Agregar items
```

### Productos

```
GET    /api/v1/products           - Listar productos
POST   /api/v1/products           - Crear producto
PUT    /api/v1/products/{id}      - Actualizar producto
DELETE /api/v1/products/{id}      - Eliminar producto
```

### Chat IA

```
POST /api/v1/chat/ask         - Hacer pregunta
GET  /api/v1/chat/suggestions - Sugerencias contextuales
```

---

## Demo para Presentaciones

### Paso 1: Mostrar el Frontend (2 min)

Abre http://localhost:4200 y muestra la interfaz limpia y moderna.

### Paso 2: Crear una Orden (3 min)

1. Click en "Crear Orden Rápida"
2. Explica que detrás pasa:
   - POST al backend
   - Validación
   - Guardado en PostgreSQL
   - Mensaje a SQS
   - Notificación push si está activado

### Paso 3: Ver Detalles y Auditoría (2 min)

1. Click en una orden
2. Muestra el historial de auditoría
3. Explica que cada operación queda registrada

### Paso 4: Transición de Estados (3 min)

1. Cambia el estado de la orden
2. Muestra cómo se actualiza el historial
3. Explica los eventos que se disparan

### Paso 5: Chat IA (2 min)

1. Abre el chat flotante
2. Pregunta sobre el sistema
3. Muestra respuestas contextuales

### Paso 6: Swagger UI (2 min)

Abre http://localhost:8080/swagger-ui.html y muestra todos los endpoints disponibles.

---

## Comandos Útiles

```bash
# Iniciar todo
docker compose up -d

# Ver logs
docker compose logs -f app
docker compose logs -f frontend

# Reiniciar un servicio
docker compose restart app

# Detener todo
docker compose down

# Limpiar volúmenes (reset completo)
docker compose down -v

# Reconstruir después de cambios
docker compose up -d --build
```

---

## Migrar a Otra PC o Windows

### Paso 1: Clonar el repositorio

```bash
git clone https://github.com/moutinho132/ms-event-processor.git
cd ms-event-processor
```

### Paso 2: Configurar variables de entorno

```bash
# Copiar template
cp .env.example .env

# Editar con tus valores
# IMPORTANTE: Cambia OPENAI_API_KEY si usas Chat IA
```

### Paso 3: Levantar servicios

```bash
docker compose up -d
```

### Configuración para Windows

**Docker Desktop**: Asegúrate de tener Docker Desktop instalado y corriendo.

**Ollama para Chat IA**:
```powershell
# Descargar desde https://ollama.ai
# O usar winget
winget install Ollama.Ollama

# Descargar modelo
ollama pull llama3.2

# Iniciar
ollama serve
```

**Si usas WSL2**:
Los comandos son idénticos a Linux/macOS.

### Diferencias en Windows

| Aspecto | macOS/Linux | Windows |
|---------|-------------|---------|
| Maven wrapper | `./mvnw` | `mvnw.cmd` |
| Scripts shell | `./script.sh` | Git Bash o WSL |
| Docker | Docker Desktop | Docker Desktop |
| Ollama | `brew install ollama` | Descarga .exe |

---

## LocalStack (AWS Local)

LocalStack simula servicios de AWS localmente. No necesitas cuenta de AWS.

### Servicios disponibles

- **SQS**: Colas de mensajes
- **SNS**: Notificaciones
- **Lambda**: Functions serverless
- **S3**: Almacenamiento de objetos

### Ver recursos

Abre http://localhost:8081 para ver la interfaz gráfica de LocalStack.

### Crear recursos manualmente

```bash
# Crear cola SQS
aws --endpoint-url=http://localhost:4566 \
  sqs create-queue --queue-name mi-cola

# Listar colas
aws --endpoint-url=http://localhost:4566 \
  sqs list-queues

# Crear tópico SNS
aws --endpoint-url=http://localhost:4566 \
  sns create-topic --name mi-topico
```

---

## Troubleshooting

### Kafka no conecta

```bash
# Verificar estado
docker logs ms-event-processor-kafka

# Reiniciar
docker compose restart kafka
```

### Base de datos vacía

La aplicación crea tablas automáticamente. Si hay problemas:

```bash
# Reiniciar todo
docker compose down -v
docker compose up -d
```

### Frontend no carga

```bash
# Reconstruir
docker compose up -d --build frontend
```

### Puerto ocupado

```bash
# Ver qué usa el puerto
lsof -i :8080    # macOS/Linux
netstat -ano | findstr :8080    # Windows
```

---

## Arquitectura Detallada

```
┌─────────────────────────────────────────────────────────────────┐
│                    FRONTEND (Angular 18)                         │
│                       localhost:4200                             │
│  ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌─────────┐ ┌──────────┐ │
│  │  Login  │ │ Dashboard│ │ Orders  │ │ Products│ │   Chat   │ │
│  └─────────┘ └──────────┘ └─────────┘ └─────────┘ └──────────┘ │
└───────────────────────────────┬─────────────────────────────────┘
                                │ /api/*
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│               BACKEND (Java 21 + Spring Boot)                    │
│                      localhost:8080                              │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │ Auth Service │  │  Kafka       │  │    REST Controllers   │ │
│  │ JWT + BCrypt │  │  Consumers   │  │    /api/v1/*          │ │
│  └──────────────┘  └──────────────┘  └───────────────────────┘ │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │   Services   │  │  SQS/SNS     │  │    Chat Service       │ │
│  │  (Business)  │  │  Producers   │  │    (Ollama/LLM)       │ │
│  └──────────────┘  └──────────────┘  └───────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  PostgreSQL   │      │ Apache Kafka  │      │  LocalStack   │
│   :5432       │      │   :9092       │      │  SQS/SNS/Lambda│
│   (Datos)     │      │  (Eventos)    │      │    :4566      │
└───────────────┘      └───────────────┘      └───────────────┘
```

---

## Los Números que Impresionan

- **39+ tests** pasando
- **3 Kafka consumers** escuchando simultáneamente
- **6+ servicios Docker** en paralelo
- **Java 21 Virtual Threads** manejando miles de conexiones
- **Idempotencia**: mismo mensaje 10 veces = se procesa una vez
- **Audit trail**: cada operación queda registrada

---

## Conceptos Clave

### Event-Driven

En lugar de que A llame a B que llame a C, A publica un evento y B y C escuchan. Nadie espera a nadie. Todo funciona en paralelo.

### Virtual Threads (Java 21)

Antes: Cada hilo consumía 1MB de memoria. 1000 hilos = 1GB solo en hilos.

Ahora: Los Virtual Threads son "virtuales". Puedes tener 100,000 hilos usando la misma memoria.

### Idempotencia

Si el mismo mensaje llega 10 veces, el sistema lo procesa solo una vez. Esto es crítico en sistemas distribuidos donde los mensajes pueden duplicarse.

### LocalStack

No necesitas cuenta de AWS para desarrollar. LocalStack simula SQS, SNS, Lambda y S3 localmente.

---

## Hoja de Ruta

- [x] Consumo de eventos desde Kafka
- [x] Procesamiento idempotente
- [x] API REST completa
- [x] Frontend Angular
- [x] Autenticación JWT
- [x] Chat IA con Ollama
- [x] Dashboard con gráficas
- [x] CI/CD con GitHub Actions
- [ ] Métricas Prometheus/Grafana
- [ ] Kubernetes manifests
- [ ] Circuit breaker con Resilience4j

---

## Contribuir

1. Fork el repositorio
2. Crea una rama: `git checkout -b feature/mi-cambio`
3. Commit: `git commit -m 'Agrega funcionalidad'`
4. Push: `git push origin feature/mi-cambio`
5. Abre un Pull Request

Ver [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles.

---

## Licencia

Licencia Apache 2.0 - ver [LICENSE](LICENSE) para más detalles.

---

## Contacto

**Autor:** Fernando Moutinho  
**GitHub:** [@moutinho132](https://github.com/moutinho132)  
**Proyecto:** [ms-event-processor](https://github.com/moutinho132/ms-event-processor)

---

**Hecho con Java 21, Spring Boot, Angular y mucho ☕**
