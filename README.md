# Seguradora ACME — API de Cotações de Seguro

Sistema REST de cotações de seguro desenvolvido como desafio técnico para o Itaú. A aplicação recebe solicitações de cotação, valida contra um catálogo de produtos/ofertas, persiste no banco de dados e se comunica via broker de mensagens.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Como Executar](#como-executar)
- [API Reference](#api-reference)
- [Mensageria (Kafka)](#mensageria-kafka)
- [Catálogo Mock (WireMock)](#catálogo-mock-wiremock)
- [Cache (Redis)](#cache-redis)
- [Premissas e Decisões](#premissas-e-decisões)

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.2.3 | Framework principal |
| Maven | 3.9+ | Gerenciamento de dependências |
| PostgreSQL | 15 | Banco de dados |
| Apache Kafka | 3.9 | Broker de mensagens |
| Redis | 7 | Cache de produtos/ofertas |
| WireMock | 3.3 | Mock do serviço de catálogo |
| Flyway | — | Migrations de banco de dados |
| Docker Compose | — | Infraestrutura local |

---

## Arquitetura

O projeto adota **Clean Architecture (Hexagonal)**, separando claramente as responsabilidades em camadas:

```
com.acme.seguradora/
├── domain/                 # Modelos e exceções de domínio (sem dependências de framework)
│   ├── model/              # Quote, Coverage, Customer, QuoteStatus
│   └── exception/          # QuoteNotFoundException, QuoteValidationException
│
├── application/            # Regras de negócio puras
│   ├── port/
│   │   ├── input/          # Casos de uso (interfaces): CreateQuoteUseCase, GetQuoteUseCase
│   │   └── output/         # Portas de saída (interfaces): QuoteRepositoryPort, CatalogServicePort
│   ├── dto/                # DTOs internos da aplicação (CatalogProductDto, CatalogOfferDto)
│   └── service/            # QuoteService: implementa os casos de uso e todas as validações
│
└── infrastructure/         # Adaptadores concretos (Spring, JPA, Kafka, Redis, HTTP)
    ├── web/                # Controller REST, DTOs de request/response, GlobalExceptionHandler
    ├── persistence/        # JPA entity, repository, adapter para QuoteRepositoryPort
    ├── messaging/          # Kafka producer (OutboxProcessor) e consumer (PolicyIssuedConsumer)
    ├── outbox/             # OutboxEvent, OutboxEventRepository, OutboxProcessor, OutboxService
    ├── catalog/            # Cliente HTTP para a API do Catálogo (com cache Redis)
    └── config/             # Configurações de Kafka, Redis e RestTemplate
```

**Fluxo de criação de cotação:**

```
POST /api/v1/quotes
        │
        ▼
QuoteController (valida Bean Validation)
        │
        ▼
QuoteService.createQuote()
   ├── CatalogApiAdapter.findProductById()       [Redis cache]
   ├── CatalogApiAdapter.findOfferById()         [Redis cache]
   ├── Valida produto ativo
   ├── Valida oferta ativa e pertencente ao produto
   ├── Valida coberturas (existência + valor máximo)
   ├── Valida assistências (existência na oferta)
   ├── Valida prêmio mensal total (min/max)
   ├── Valida total de coberturas (somatória)
   ├── QuoteRepositoryAdapter.save()             [PostgreSQL]
   └── OutboxService.saveQuoteReceivedEvent()    [PostgreSQL outbox_events]
        │
        ▼ (OutboxProcessor — agendado a cada 5s)
   KafkaTemplate.send("quote.received")          [Kafka]
        │
        ▼
Retorna 201 Created com a cotação
```

**Fluxo de recebimento de apólice:**

```
[Kafka — tópico policy.issued]
        │
        ▼
PolicyIssuedConsumer.consume()
        │
        ▼
QuoteService.updateQuoteWithPolicy()
        │
        ├── Busca cotação por ID no banco
        ├── Atualiza status → ACTIVE + policyId + receivedAt
        └── OutboxService.markPolicyIssuedReceived()  [marca dat_received no outbox]
```

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados

### 1. Subir toda a infraestrutura e a aplicação

```bash
docker compose up -d
```

Aguarde todos os containers ficarem saudáveis (cerca de 30–60 segundos):

```bash
docker compose ps
```

A aplicação estará disponível em: `http://localhost:8080`

### 2. Verificar saúde da aplicação

```bash
curl http://localhost:8080/actuator/health
```

### 3. Executar apenas os testes unitários

```bash
mvn test
```

---

## API Reference

### Criar Cotação

**POST** `/api/v1/quotes`

**Request Body:**
```json
{
  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
  "category": "LIFE",
  "total_monthly_premium_amount": 75.25,
  "total_coverage_amount": 825000.00,
  "coverages": [
    { "name": "Morte Acidental", "value": 500000.00 },
    { "name": "Invalidez Permanente", "value": 300000.00 },
    { "name": "Assistência Funeral", "value": 25000.00 }
  ],
  "assistances": [
    "Funeral",
    "Ambulância"
  ],
  "customer": {
    "document_number": "36205578900",
    "name": "John Wick",
    "type": "NATURAL",
    "gender": "MALE",
    "date_of_birth": "1973-05-02",
    "email": "johnwick@gmail.com",
    "phone_number": "11950503030"
  }
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
  "category": "LIFE",
  "total_monthly_premium_amount": 75.25,
  "total_coverage_amount": 825000.00,
  "coverages": [
    { "name": "Morte Acidental", "value": 500000.00 },
    { "name": "Invalidez Permanente", "value": 300000.00 },
    { "name": "Assistência Funeral", "value": 25000.00 }
  ],
  "assistances": ["Funeral", "Ambulância"],
  "customer": {
    "document_number": "36205578900",
    "name": "John Wick",
    "type": "NATURAL",
    "gender": "MALE",
    "date_of_birth": "1973-05-02",
    "email": "johnwick@gmail.com",
    "phone_number": "11950503030"
  },
  "policy_id": null,
  "status": "PENDING",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:30:00"
}
```

**Response 422 Unprocessable Entity** (validação falhou):
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Coverage value exceeds maximum for Morte Acidental: max=500000.00 requested=600000.00",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Consultar Cotação

**GET** `/api/v1/quotes/{id}`

**Response 200 OK:**
```json
{
  "id": 1,
  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
  "category": "LIFE",
  "total_monthly_premium_amount": 75.25,
  "total_coverage_amount": 825000.00,
  "coverages": [...],
  "assistances": [...],
  "customer": {...},
  "policy_id": 98765,
  "status": "ACTIVE",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:35:00"
}
```

**Response 404 Not Found:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Quote not found with id: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Mensageria (Kafka)

### Configuração

| Item | Valor |
|---|---|
| Bootstrap server | `kafka:9092` |
| Consumer group | `seguradora-group` |
| Tópico de envio | `quote.received` |
| Tópico de recebimento | `policy.issued` |
| Partições por tópico | `3` |
| Padrão de entrega | Outbox Pattern |

### Padrão Outbox

A publicação no tópico `quote.received` **não ocorre diretamente** durante a criação da cotação. Em vez disso, é usada a estratégia **Transactional Outbox**:

1. A cotação e o evento de saída são gravados na mesma transação do banco (`quotes` + `outbox_events`)
2. Um `OutboxProcessor` agendado (a cada 5s) consulta eventos pendentes e publica no Kafka
3. Após publicação bem-sucedida, o evento é marcado como `flagSent = true`

Isso garante que **nenhum evento é perdido** mesmo que o Kafka esteja temporariamente indisponível.

### Simular envio de apólice (serviço de apólices)

Para simular o serviço de apólices publicando no tópico `policy.issued`, use o CLI do Kafka dentro do container:

```bash
docker exec seguradora-kafka kafka-console-producer.sh \
  --bootstrap-server kafka:9092 \
  --topic policy.issued
```

Cole o seguinte JSON e pressione Enter:

```json
{"quote_id": 1, "policy_id": 98765, "created_at": "2024-01-15T10:35:00"}
```

Após o envio, a cotação terá seu `status` atualizado para `ACTIVE` e o campo `policy_id` preenchido.

### Listar mensagens publicadas em quote.received

```bash
docker exec seguradora-kafka kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 \
  --topic quote.received \
  --from-beginning
```

---

## Catálogo Mock (WireMock)

O WireMock simula o serviço de catálogo de produtos/ofertas. Os stubs estão em `./wiremock/mappings/`.

**Produto disponível (ID pré-configurado):**
- ID: `1ab2c3d4-e5f6-7890-abcd-ef1234567890`
- Nome: `Seguro de Vida`

**Oferta disponível (ID pré-configurado):**
- ID: `aaa1b2c3-d4e5-6789-abcd-ef0123456789`
- Coberturas: Morte Acidental (max R$500k), Invalidez Permanente (max R$300k), Assistência Funeral (max R$25k)
- Assistências: Funeral, Ambulância, Chaveiro 24h
- Prêmio mensal: R$50 a R$200

---

## Cache (Redis)

Os dados de produtos e ofertas consultados do catálogo são armazenados em cache Redis por **5 minutos** para evitar chamadas desnecessárias à API do catálogo.

Chaves de cache:
- `products::{productId}`
- `offers::{offerId}`

---

## Validações Implementadas

| Validação | Comportamento |
|---|---|
| Produto existe e está ativo | 422 se falhar |
| Oferta existe, está ativa e pertence ao produto | 422 se falhar |
| Coberturas existem na oferta e não excedem o valor máximo | 422 se falhar |
| Assistências existem na oferta | 422 se falhar |
| Prêmio mensal total dentro do intervalo (min/max) da oferta | 422 se falhar |
| Total de coberturas = somatória das coberturas | 422 se falhar |
| Bean Validation nos campos obrigatórios do request | 400 se falhar |

---

## Premissas e Decisões

### Clean Architecture (Hexagonal)
A arquitetura foi escolhida para garantir **desacoplamento** entre a lógica de negócio e os detalhes de infraestrutura (banco de dados, broker, APIs externas). Isso facilita testes unitários sem dependências externas e torna o sistema extensível.

### Apache Kafka + Outbox Pattern
Kafka foi escolhido pela alta throughput, durabilidade das mensagens e capacidade de replay de eventos. Para garantir consistência entre a persistência no banco e a publicação no Kafka, foi implementado o **Transactional Outbox Pattern**: o evento é gravado na tabela `outbox_events` na mesma transação da cotação e um processor agendado realiza a publicação assíncrona, eliminando o risco de perda de mensagens em caso de falha do broker.

### Redis como Cache
Caching de produtos e ofertas com TTL de 5 minutos evita latência excessiva e sobrecarga no catálogo. O Spring Cache abstrai a implementação, permitindo troca fácil do provedor.
