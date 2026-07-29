# MS-Event-Processor - Guia para Directo/Live

## Visao General do Projeto

**MS-Event-Processor** e um monorepo fullstack com:

- **Backend**: Microservico event-driven em Java 21 + Spring Boot 3.x
- **Frontend**: SPA em Angular 18 com TypeScript

### O que faz?

1. **Consome eventos** de ordens desde **Apache Kafka** (3 topicos)
2. **Processa** as ordens aplicando logica de negocio
3. **Persiste** os dados em **PostgreSQL**
4. **Reenvia** mensagens processadas a **AWS SQS**
5. **API REST** para testes e verificacao
6. **Frontend Angular** para gerenciamento completo de ordens

---

## Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND (Angular 18)                              │
│                              http://localhost:4200                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Order List │  │Order Detail │  │  Services   │  │  Nginx Proxy /api/  │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└───────────────────────────────────────────┬─────────────────────────────────┘
                                            │ /api/* -> backend:8080
                                            ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          BACKEND (Java 21 + Spring Boot)                     │
│                              http://localhost:8080                           │
│                                                                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────────────┐  │
│  │ Kafka Consumers  │  │ OrderProcessor    │  │      REST API             │  │
│  │  (3 listeners)   │  │  (Business Logic) │  │  /api/v1/orders/*        │  │
│  └────────┬─────────┘  └────────┬─────────┘  └─────────────┬─────────────┘  │
│           │                     │                          │                 │
│           └─────────────────────┼──────────────────────────┘                 │
│                                 ▼                                            │
│           ┌──────────────────────────────────────┐                          │
│           │   OrderRepository + SqsProducer       │                          │
│           └──────────┬──────────────┬────────────┘                          │
└──────────────────────┼──────────────┼───────────────────────────────────────┘
                       ▼              ▼
             ┌─────────────────┐   ┌─────────────────────┐
             │   PostgreSQL    │   │    AWS SQS          │
             │   (Database)    │   │ (Message Queue)     │
             └─────────────────┘   └─────────────────────┘
                       ▲
                       │
             ┌─────────────────┐   ┌─────────────────────┐
             │ Apache Kafka    │   │    LocalStack       │
             │ (KRaft mode)    │   │    (AWS Local)      │
             └─────────────────┘   └─────────────────────┘
```

---

## Stack Tecnologico

### Backend

| Tecnologia | Versao | Proposito |
|-----------|---------|-----------|
| Java | 21 LTS | Runtime com **Virtual Threads** |
| Spring Boot | 3.3.5 | Framework principal |
| Spring Kafka | 3.x | Consumo de eventos Kafka |
| Spring Data JPA | 3.x | Persistencia em PostgreSQL |
| Spring Security | 3.x | Autenticacao JWT |
| Apache Kafka | 3.7.0 | Message Broker (KRaft mode) |
| PostgreSQL | 15 | Banco de dados relacional |
| AWS SQS | - | Fila de mensagens de saida |
| LocalStack | 3.7 | Simulacao local de AWS |
| Testcontainers | 1.20.3 | Testing de integracao |

### Frontend

| Tecnologia | Versao | Proposito |
|-----------|---------|-----------|
| Angular | 18.2.0 | Framework SPA |
| TypeScript | 5.5.2 | Linguagem |
| RxJS | 7.8.0 | Programacao reativa |
| Node.js | 20 Alpine | Build runtime |
| Nginx | Alpine | Servidor web + proxy |

---

## Inicio Rapido

### 1. Clonar o repositorio

```bash
git clone https://github.com/moutinho132/ms-event-processor.git
cd ms-event-processor
```

### 2. Levantar toda a infraestrutura (Docker)

```bash
# Iniciar todos os servicos: PostgreSQL, Kafka, LocalStack, Backend e Frontend
docker compose up -d

# Verificar que todos os containers estao rodando
docker compose ps
```

**Containers iniciados:**
- `ms-event-processor-app` - Backend (porta 8080)
- `ms-event-processor-frontend` - Frontend (porta 4200)
- `ms-event-processor-postgres` - PostgreSQL (porta 5432)
- `ms-event-processor-kafka` - Apache Kafka (portas 9092, 9093)
- `ms-event-processor-localstack` - LocalStack SQS (porta 4566)

### 3. Acessar a aplicacao

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1/orders
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

---

## Comandos de Desenvolvimento

### Backend (Java/Spring Boot)

```bash
# Entrar no diretorio do backend
cd backend

# Compilar projeto
./mvnw clean compile

# Executar tests unitarios
./mvnw test

# Executar tests completos (inclui integracao)
./mvnw verify

# Executar aplicacao local (sem Docker)
./mvnw spring-boot:run

# Executar com profile Docker
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### Frontend (Angular)

```bash
# Entrar no diretorio do frontend
cd frontend

# Instalar dependencias
npm install

# Executar em modo desenvolvimento (porta 4200)
npm start
# ou
ng serve

# Build de producao
npm run build

# Executar tests
npm test

# Build com watch para desenvolvimento
npm run watch
```

### Docker Compose

```bash
# Iniciar todos os servicos
docker compose up -d

# Ver logs de todos os servicos
docker compose logs -f

# Ver logs de um servico especifico
docker compose logs -f app
docker compose logs -f frontend

# Reconstruir imagens
docker compose build --no-cache

# Reconstruir e reiniciar um servico especifico
docker compose up -d --build app
docker compose up -d --build frontend

# Parar todos os servicos
docker compose down

# Parar e remover volumes
docker compose down -v
```

---

## API REST - Endpoints

### Endpoints Principais

Base path: `/api/v1/orders`

| Endpoint | Metodo | Descricao |
|----------|--------|-----------|
| `/` | GET | Listar todas as ordens |
| `/{orderId}` | GET | Obter ordem por ID |
| `/create-test` | POST | Criar ordem de teste |
| `/process` | POST | Processar ordem diretamente |
| `/{orderId}/cancel` | POST | Cancelar ordem |
| `/kafka/send-test` | POST | Enviar mensagem teste ao Kafka |
| `/kafka/send` | POST | Enviar mensagem custom ao Kafka |
| `/database/check` | GET | Ver todas as ordens no BD |
| `/database/check/{id}` | GET | Ver ordem especifica + auditoria |

### Exemplos de Uso

#### 1. Criar ordem de teste

```bash
curl -X POST http://localhost:4200/api/v1/orders/create-test
```

#### 2. Listar todas as ordens

```bash
curl http://localhost:4200/api/v1/orders
```

#### 3. Obter ordem especifica

```bash
curl http://localhost:4200/api/v1/orders/{orderId}
```

#### 4. Cancelar ordem

```bash
curl -X POST http://localhost:4200/api/v1/orders/{orderId}/cancel
```

#### 5. Enviar mensagem ao Kafka

```bash
curl -X POST http://localhost:4200/api/v1/orders/kafka/send-test
```

#### 6. Processar ordem diretamente

```bash
curl -X POST http://localhost:4200/api/v1/orders/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order-001",
    "customerId": "customer-123",
    "eventType": "CREATED",
    "totalAmount": 1500.00,
    "currency": "USD",
    "items": [{
      "productId": "product-456",
      "productName": "Widget Premium",
      "quantity": 2,
      "unitPrice": 750.00
    }],
    "metadata": {
      "source": "api-test",
      "correlationId": "corr-001",
      "timestamp": "2026-07-28T22:00:00Z"
    }
  }'
```

---

## Frontend Angular

### Estrutura do Projeto

```
frontend/
├── src/
│   ├── app/
│   │   ├── models/           # Interfaces TypeScript
│   │   │   └── order.model.ts
│   │   ├── services/         # Servicos HTTP
│   │   │   └── order.service.ts
│   │   ├── orders/           # Modulo de ordens
│   │   │   ├── order-list/   # Lista de ordens
│   │   │   └── order-detail/ # Detalhes da ordem
│   │   ├── shared/           # Componentes compartilhados
│   │   ├── app.component.ts  # Componente raiz
│   │   ├── app.config.ts     # Configuracao da app
│   │   └── app.routes.ts     # Rotas
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
├── angular.json
├── package.json
├── tsconfig.json
├── Dockerfile
└── nginx.conf
```

### Rotas do Frontend

| Rota | Componente | Descricao |
|------|------------|-----------|
| `/` | OrderListComponent | Lista todas as ordens |
| `/orders/:id` | OrderDetailComponent | Detalhes de uma ordem |

### Funcionalidades do Frontend

1. **Lista de Ordens**: Tabela com todas as ordens, filtros e paginacao
2. **Detalhes da Ordem**: Visualizacao completa com auditoria
3. **Criar Ordem**: Botao para criar ordem de teste
4. **Cancelar Ordem**: Acao para cancelar ordens
5. **Refresh**: Atualizacao manual da lista
6. **Status Badges**: Indicadores visuais de status

### Configuracao Nginx

O frontend usa Nginx como servidor web e proxy reverso:

```nginx
# API proxy para backend
location /api/ {
    proxy_pass http://app:8080;
    # ... headers
}

# Angular routing (SPA)
location / {
    try_files $uri $uri/ /index.html;
}
```

---

## Estrutura do Backend

```
backend/src/main/java/com/dev/app/
├── config/              # Configuracoes (Kafka, SQS, Security, Virtual Threads)
├── consumer/            # Kafka Consumers (3 listeners)
├── controller/          # REST API Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # Entidades JPA (Order, OrderItem, OrderAudit)
├── exception/           # Manejo de excecoes
├── producer/            # SQS Producer
├── processor/           # Logica de processamento
├── repository/          # Spring Data JPA Repositories
├── security/            # JWT Authentication
└── util/                # Utilidades (RetryHelper)
```

---

## Apache Kafka

### Topicos

| Topico | Evento | Descricao |
|--------|--------|-----------|
| `orders-created` | CREATED | Novas ordens criadas |
| `orders-updates` | UPDATED | Atualizacoes de ordens |
| `orders-cancelled` | CANCELLED | Cancelamentos de ordens |

### Configuracao

- **Consumer Group**: `order-processor-group`
- **Ack Mode**: `MANUAL`
- **Offset Reset**: `earliest`
- **Max Poll Records**: 100

### Scripts de Teste

```bash
# Enviar mensagem de teste via script shell
./backend/test-producer.sh

# Verificar ordens no banco de dados
./backend/check-orders.sh
./backend/check-orders.sh {order_id}
```

---

## AWS SQS (LocalStack)

### Filas

- **Principal**: `ordenes-procesadas-queue`
- **Dead Letter Queue**: `ordenes-procesadas-dlq`

### Comandos AWS CLI

```bash
# Listar filas
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Criar fila manualmente
aws --endpoint-url=http://localhost:4566 \
  sqs create-queue --queue-name ordenes-procesadas-queue

# Receber mensagens da fila
aws --endpoint-url=http://localhost:4566 \
  sqs receive-message --queue-url http://localhost:4566/000000000000/ordenes-procesadas-queue
```

---

## Banco de Dados PostgreSQL

### Tabelas Principais

- `orders` - Ordens processadas
- `order_items` - Items de cada ordem
- `order_audit_log` - Auditoria completa de operacoes

### Estados da Ordem

| Estado | Descricao |
|--------|-----------|
| `PENDING` | Ordem criada, pendente de processamento |
| `CONFIRMED` | Ordem confirmada |
| `PROCESSING` | Ordem em processo |
| `SHIPPED` | Ordem enviada |
| `DELIVERED` | Ordem entregue |
| `CANCELLED` | Ordem cancelada |
| `REFUNDED` | Ordem reembolsada |

### Consultas SQL

```bash
# Conectar ao PostgreSQL
docker exec -it ms-event-processor-postgres psql -U postgres -d orderdb

# Ver todas as ordens
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;

# Ver auditoria
SELECT * FROM order_audit_log ORDER BY processed_at DESC LIMIT 10;

# Contar ordens por status
SELECT status, COUNT(*) FROM orders GROUP BY status;
```

---

## Monitoreo e Observabilidade

### Actuator Endpoints

| Endpoint | Descricao |
|----------|-----------|
| `/actuator/health` | Estado da aplicacao |
| `/actuator/info` | Informacao da aplicacao |
| `/actuator/metrics` | Metricas detalhadas |
| `/actuator/prometheus` | Metricas para Prometheus |

### Logs

```bash
# Ver logs do backend
docker logs -f ms-event-processor-app

# Ver logs do frontend
docker logs -f ms-event-processor-frontend

# Ver logs do Kafka
docker logs -f ms-event-processor-kafka
```

---

## Testes

### Backend

```bash
cd backend

# Tests unitarios
./mvnw test

# Tests de integracao (com Testcontainers)
./mvnw verify

# Cobertura: 39+ tests passando
```

### Frontend

```bash
cd frontend

# Tests unitarios
npm test

# Tests com coverage
npm test -- --code-coverage
```

---

## Troubleshooting

### Kafka nao conecta

```bash
# Verificar estado
docker ps | grep kafka

# Ver logs
docker logs ms-event-processor-kafka

# Criar topicos manualmente
docker exec ms-event-processor-kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --create --topic orders-created \
  --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1
```

### Banco de dados vazio

A aplicacao cria as tabelas automaticamente com `ddl-auto: update`.

```bash
# Verificar tabelas
docker exec -it ms-event-processor-postgres \
  psql -U postgres -d orderdb -c "\dt"
```

### SQS nao recebe mensagens

```bash
# Verificar LocalStack
docker ps | grep localstack

# Listar filas
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

### Frontend nao carrega

```bash
# Verificar se o container esta rodando
docker ps | grep frontend

# Verificar logs
docker logs ms-event-processor-frontend

# Reconstruir o frontend
docker compose up -d --build frontend
```

### Erro 503 ao criar ordem

Verificar que o backend e LocalStack estao saudaveis:

```bash
# Health check do backend
curl http://localhost:8080/actuator/health

# Verificar LocalStack
curl http://localhost:4566/_localstack/health
```

---

## Conceitos Demonstrados

### Backend

1. **Event-Driven Architecture**: Desacoplamento entre produtores e consumidores
2. **Java 21 Virtual Threads**: Milhares de hilos concorrentes com minimo overhead
3. **Idempotencia**: Processar o mesmo mensagem multiplas vezes = mesmo resultado
4. **CQRS Pattern**: Separacao de comandos (Kafka) e consultas (REST API)
5. **Audit Trail**: Registro completo de todas as operacoes
6. **Retry com Backoff Exponencial**: Reintentos automaticos ante falhas transitorias
7. **Dead Letter Queue**: Mensagens que falham apos maximos reintentos

### Frontend

1. **Angular 18**: Framework moderno com standalone components
2. **RxJS**: Programacao reativa para chamadas HTTP
3. **TypeScript**: Tipagem estatica
4. **Nginx Proxy**: Redirecionamento transparente `/api/` ao backend
5. **SPA Routing**: Navegacao sem reload de pagina

---

## Fluxo de Demonstracao

### Cenario Completo

1. **Acessar Frontend**
   ```
   http://localhost:4200
   ```

2. **Criar Ordem de Teste**
   - Clicar em "Nova Ordem de Teste" ou
   - `curl -X POST http://localhost:4200/api/v1/orders/create-test`

3. **Verificar Lista de Ordens**
   - A tabela atualiza automaticamente
   - Verificar status, ID, cliente, valor

4. **Ver Detalhes da Ordem**
   - Clicar no ID da ordem ou botao "Ver Detalhes"
   - Ver auditoria completa

5. **Cancelar Ordem**
   - Clicar em "Cancelar" na lista ou detalhes
   - Verificar mudanca de status

6. **Verificar Backend**
   ```
   http://localhost:8080/swagger-ui.html
   ```

7. **Verificar Kafka**
   ```bash
   docker exec ms-event-processor-kafka \
     /opt/kafka/bin/kafka-console-consumer.sh \
     --bootstrap-server localhost:9092 \
     --topic orders-created --from-beginning
   ```

---

## CI/CD

### GitHub Actions

Pipeline configurado em `.github/workflows/ci.yml`:

1. **Build and Test**: Compilacao e testes unitarios
2. **Integration Tests**: Testes de integracao com Testcontainers

### Executar Pipeline

Push para branch `develop` ou `main` dispara o pipeline automaticamente.

---

## Recursos

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **README.md**: Documentacao tecnica
- **TESTING.md**: Guia de testes
- **QUICK-START.md**: Comandos rapidos

---

**Listo para o directo!**
