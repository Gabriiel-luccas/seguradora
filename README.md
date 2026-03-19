# Seguradora ACME — API de Cotações de Seguro

Sistema REST de cotações de seguro desenvolvido como desafio técnico para o Itaú. A aplicação recebe solicitações de cotação, valida contra um catálogo de produtos/ofertas, persiste no banco de dados e se comunica via broker de mensagens.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Como Executar](#como-executar)
- [API Reference](#api-reference)
- [Mensageria (RabbitMQ)](#mensageria-rabbitmq)
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
| RabbitMQ | 3.12 | Broker de mensagens |
| Redis | 7 | Cache de produtos/ofertas |
| WireMock | 3.3 | Mock do serviço de catálogo |
| Flyway | | Migrations de banco de dados |
| Docker Compose | | Infraestrutura local |

---

## Arquitetura

O projeto adota **Clean Architecture (Hexagonal)**, separando claramente as responsabilidades em camadas:

```
com.acme.seguradora/
├── domain/               ← Modelos e exceções de domínio (sem dependências de framework)
│   ├── model/            ← Quote, Coverage, Customer, QuoteStatus
│   └── exception/        ← QuoteNotFoundException, QuoteValidationException
│
├── application/          ← Regras de negócio puras
│   ├── port/
│   │   ├── input/        ← Casos de uso (interfaces): CreateQuoteUseCase, GetQuoteUseCase
│   │   └── output/       ← Portas de saída (interfaces): QuoteRepositoryPort, CatalogServicePort, MessagePublisherPort
│   ├── dto/              ← DTOs internos da aplicação (CatalogProductDto, CatalogOfferDto)
│   └── service/          ← QuoteService: implementa os casos de uso e todas as validações
│
└── infrastructure/       ← Adaptadores concretos (Spring, JPA, RabbitMQ, Redis, HTTP)
    ├── web/              ← Controller REST, DTOs de request/response, GlobalExceptionHandler
    ├── persistence/      ← JPA entity, repository, adapter para QuoteRepositoryPort
    ├── messaging/        ← Publisher RabbitMQ, Consumer policy.issued
    ├── catalog/          ← Cliente HTTP para a API do Catálogo (com cache Redis)
    └── config/           ← Configurações de RabbitMQ, Redis e RestTemplate
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
  ├── CatalogApiAdapter.findProductById()  [Redis cache]
  ├── CatalogApiAdapter.findOfferById()    [Redis cache]
  ├── Valida produto ativo
  ├── Valida oferta ativa e pertencente ao produto
  ├── Valida coberturas (existência + valor máximo)
  ├── Valida assistências (existência na oferta)
  ├── Valida prêmio mensal total (min/max)
  ├── Valida total de coberturas (somatória)
  ├── QuoteRepositoryAdapter.save()        [PostgreSQL]
  └── RabbitMQPublisher.publishQuoteReceived()  →  [quote.received queue]
       │
       ▼
Retorna 201 Created com a cotação
```

**Fluxo de recebimento de apólice:**

```
[policy.issued queue]  →  PolicyIssuedConsumer
                                │
                                ▼
                     QuoteService.updateQuoteWithPolicy()
                                │
                                ▼
                     Atualiza status → ACTIVE + policyId
```

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados

### 1. Subir toda a infraestrutura e a aplicação

```bash
docker-compose up -d
```

Aguarde todos os containers ficarem saudáveis (cerca de 30-60 segundos):

```bash
docker-compose ps
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
    "Assistência Funeral",
    "Telemedicina"
  ],
  "customer": {
    "document_number": "36205578900",
    "name": "John Wick",
    "type": "NATURAL_PERSON",
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
  "assistances": ["Assistência Funeral", "Telemedicina"],
  "customer": {
    "document_number": "36205578900",
    "name": "John Wick",
    "type": "NATURAL_PERSON",
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
  "error": "Validation Error",
  "message": "Coverage value exceeds maximum for Morte Acidental: max=100000.00 requested=200000.00",
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

## Mensageria (RabbitMQ)

### Configuração

| Item | Valor |
|---|---|
| Exchange | `insurance.exchange` (topic) |
| Fila de envio | `quote.received` |
| Fila de recebimento | `policy.issued` |
| Routing key (envio) | `quote.received` |
| Routing key (recebimento) | `policy.issued` |

### Interface Web do RabbitMQ

Acesse o painel de administração: `http://localhost:15672`
- Usuário: `guest`
- Senha: `guest`

### Simular envio de apólice (serviço de apólices)

Para simular o serviço de apólices publicando uma mensagem de apólice emitida, envie a seguinte mensagem JSON para a fila `policy.issued` via interface web do RabbitMQ ou CLI:

**Mensagem de exemplo:**
```json
{
  "policyId": 98765,
  "quoteId": 1,
  "issuedAt": "2024-01-15T10:35:00",
  "status": "ISSUED",
  "insuredName": "John Wick"
}
```

**Via CLI (rabbitmqadmin):**
```bash
docker exec seguradora-rabbitmq rabbitmqadmin publish \
  exchange=insurance.exchange \
  routing_key=policy.issued \
  payload='{"policyId":98765,"quoteId":1,"issuedAt":"2024-01-15T10:35:00","status":"ISSUED","insuredName":"John Wick"}'
```

**Via interface web:**
1. Acesse `http://localhost:15672`
2. Vá em **Exchanges** → `insurance.exchange`
3. Clique em **Publish message**
4. Routing key: `policy.issued`
5. Cole o JSON no campo **Payload**
6. Clique em **Publish message**

Após o envio, a cotação terá seu `status` atualizado para `ACTIVE` e o campo `policy_id` preenchido.

---

## Catálogo Mock (WireMock)

O WireMock simula o serviço de catálogo de produtos/ofertas. Os stubs estão em `./wiremock/mappings/`.

**Produto disponível (ID pré-configurado):**
- ID: `1ab2c3d4-e5f6-7890-abcd-ef1234567890`
- Nome: `Seguro de Vida Premium`

**Oferta disponível (ID pré-configurado):**
- ID: `aaa1b2c3-d4e5-6789-abcd-ef0123456789`
- Coberturas: Morte Acidental (max R$100k), Invalidez Permanente (max R$100k), Morte por Causas Naturais (max R$500k), Assistência Funeral (max R$10k)
- Assistências: Assistência Funeral, Mensalidade Protegida, Telemedicina, Descontos em Farmácias, Check-Up Médico
- Prêmio mensal: R$50 a R$1.000

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

### RabbitMQ
Escolhido pela maturidade, facilidade de configuração e interface de administração web que facilita testes manuais. A troca `insurance.exchange` do tipo `topic` permite escalabilidade futura com novos tipos de mensagens.

### Redis como Cache
Caching de produtos e ofertas com TTL de 5 minutos evita latência excessiva e sobrecarga no catálogo. O Spring Cache abstrai a implementação, permitindo troca fácil do provedor.

### PostgreSQL com Flyway
Banco de dados relacional para garantir consistência transacional. Flyway para versionamento de schema. Campos complexos (coberturas, assistências, dados do cliente) são armazenados como JSON em colunas TEXT, simplificando o modelo sem perder a capacidade de consulta.

### IDs Numéricos via Sequence
Uso de `SEQUENCE` do PostgreSQL para geração de IDs numéricos únicos, atendendo ao requisito do enunciado.

### WireMock para Catálogo
Mock server configurado via arquivos JSON em `./wiremock/mappings/`, permitindo extensão fácil de novos produtos/ofertas sem alterar código.

### Campos do Cliente Livres
Conforme o enunciado, os campos de dados do cliente são livres. O `Customer` aceita documentNumber, name, type, gender, dateOfBirth, email e phoneNumber — todos opcionais na validação de domínio.

### Observabilidade
- **Logs estruturados** com SLF4J/Logback em todos os fluxos principais
- **Métricas** via Spring Actuator + Micrometer expostas em `/actuator/metrics`
- **Health checks** em `/actuator/health`
- **Tracing** com OpenTelemetry (configurável via OTLP exporter)
