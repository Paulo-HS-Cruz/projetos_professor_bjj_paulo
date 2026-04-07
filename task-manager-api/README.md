# Task Manager API

API REST para gerenciamento de tarefas com autenticação JWT, controle de acesso por perfil (ADMIN/USER),
testes automatizados e pipeline CI/CD.

---

## Funcionalidades

- Cadastro e autenticação de usuários via JWT
- Perfis de acesso: **ADMIN** (acesso total) e **USER** (acesso às próprias tarefas)
- CRUD completo de tarefas com filtro por status e paginação
- Validação de entrada com Bean Validation
- Tratamento global de erros com respostas padronizadas
- Documentação automática via Swagger/OpenAPI 3.0
- Testes unitários (Mockito) e de integração (H2 + MockMvc)
- Containerização com Docker e pipeline CI/CD no GitHub Actions

---

## Tecnologias

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Segurança | Spring Security + JWT (jjwt 0.12) |
| Persistência | Spring Data JPA + Hibernate |
| Banco (dev) | PostgreSQL 16 |
| Banco (test) | H2 (in-memory) |
| Documentação | SpringDoc OpenAPI 3 (Swagger UI) |
| Testes | JUnit 5 + Mockito + MockMvc |
| Build | Maven |
| Container | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Pré-requisitos

- Java 17+
- Maven 3.9+ (ou use o `./mvnw` incluído no projeto)
- Docker e Docker Compose (para rodar via container)

---

## Como executar

### Opção 1 — Docker Compose (recomendado)

Sobe o banco PostgreSQL e a aplicação juntos com um único comando:

```bash
# Na raiz do projeto task-manager-api/
docker-compose up --build
```

A API estará disponível em: `http://localhost:8080`

Para parar:
```bash
docker-compose down
```

Para parar e remover o volume do banco:
```bash
docker-compose down -v
```

---

### Opção 2 — Rodar localmente (sem Docker)

**1. Suba apenas o banco com Docker:**
```bash
docker run -d \
  --name taskmanager-db \
  -e POSTGRES_DB=taskmanager \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

**2. Configure as variáveis de ambiente** (ou use os valores padrão do `application-dev.yml`):

```bash
export DB_USER=postgres
export DB_PASS=postgres
export JWT_SECRET=dGFza21hbmFnZXItc2VjcmV0LWtleS10YXNrbWFuYWdlci1zZWNyZXQta2V5LTEyMw==
export JWT_EXPIRATION_MS=86400000
```

> No Windows (PowerShell):
> ```powershell
> $env:DB_USER="postgres"
> $env:DB_PASS="postgres"
> ```

**3. Execute a aplicação:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Variáveis de ambiente

| Variável | Descrição | Padrão (dev) |
|----------|-----------|--------------|
| `DB_USER` | Usuário do banco | `postgres` |
| `DB_PASS` | Senha do banco | `postgres` |
| `JWT_SECRET` | Chave Base64 para assinar o JWT | valor hardcoded dev |
| `JWT_EXPIRATION_MS` | Validade do token em ms | `86400000` (24h) |

> **Importante:** Em produção, sempre defina `JWT_SECRET` como uma variável de ambiente segura. Nunca use o valor padrão.

---

## Rodando os testes

Os testes usam banco H2 em memória — **não precisa de Docker ou banco rodando**.

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários
./mvnw test -Dtest="*Test" -Dspring.profiles.active=test

# Apenas testes de integração
./mvnw test -Dtest="*IntegrationTest" -Dspring.profiles.active=test

# Relatório de cobertura (Surefire)
./mvnw test && open target/surefire-reports/index.html
```

---

## Documentação da API (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

### Fluxo básico de uso

**1. Registrar um usuário:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Paulo Cesar",
  "email": "paulo@email.com",
  "password": "senha123"
}
```

**2. Fazer login e copiar o token:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "paulo@email.com",
  "password": "senha123"
}
```

Resposta:
```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "role": "USER"
}
```

**3. Usar o token nas próximas requisições:**
```http
Authorization: Bearer eyJhbGci...
```

**4. Criar uma tarefa:**
```http
POST /api/tasks
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "title": "Estudar Spring Boot",
  "description": "Capítulo 5 — Security",
  "priority": "HIGH",
  "dueDate": "2025-12-31"
}
```

---

## Endpoints

### Auth
| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/api/auth/register` | Cadastrar usuário | Público |
| POST | `/api/auth/login` | Login e obter JWT | Público |

### Tasks
| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/api/tasks` | Criar tarefa | USER / ADMIN |
| GET | `/api/tasks` | Listar tarefas (paginado) | USER / ADMIN |
| GET | `/api/tasks/{id}` | Buscar tarefa por ID | Dono / ADMIN |
| PUT | `/api/tasks/{id}` | Atualizar tarefa | Dono / ADMIN |
| DELETE | `/api/tasks/{id}` | Deletar tarefa | Dono / ADMIN |

### Users
| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| GET | `/api/users` | Listar usuários | ADMIN |
| GET | `/api/users/{id}` | Buscar usuário | Próprio / ADMIN |
| GET | `/api/users/me` | Perfil do usuário logado | USER / ADMIN |
| DELETE | `/api/users/{id}` | Desativar usuário | ADMIN |

---

## Estrutura do projeto

```
task-manager-api/
├── .github/workflows/ci.yml          ← Pipeline CI/CD
├── src/
│   ├── main/java/com/paulocesar/taskmanager/
│   │   ├── config/                   ← SecurityConfig, OpenApiConfig
│   │   ├── controller/               ← AuthController, TaskController, UserController
│   │   ├── domain/
│   │   │   ├── entity/               ← User, Task
│   │   │   ├── enums/                ← UserRole, TaskStatus, TaskPriority
│   │   │   └── repository/           ← UserRepository, TaskRepository
│   │   ├── dto/
│   │   │   ├── request/              ← LoginRequest, RegisterRequest, TaskRequest...
│   │   │   └── response/             ← AuthResponse, TaskResponse, PageResponse...
│   │   ├── exception/                ← GlobalExceptionHandler, exceptions customizadas
│   │   ├── security/                 ← JwtTokenProvider, JwtAuthenticationFilter
│   │   └── service/                  ← AuthService, TaskService, UserService
│   └── test/
│       ├── integration/              ← AuthIntegrationTest, TaskIntegrationTest (H2)
│       └── service/                  ← AuthServiceTest, TaskServiceTest (Mockito)
├── Dockerfile                        ← Build multi-stage
├── docker-compose.yml                ← App + PostgreSQL
└── pom.xml
```

---

## Como evoluir este projeto

Sugestões de funcionalidades para implementar:

- [ ] Refresh token (expiração e renovação do JWT)
- [ ] Endpoint de busca de tarefas por texto (`/api/tasks?q=spring`)
- [ ] Soft delete (marcar como inativo em vez de deletar)
- [ ] Auditoria (quem criou, quem alterou)
- [ ] Rate limiting por IP
- [ ] Cache com Redis para consultas frequentes
- [ ] Profile de produção com configuração externa
- [ ] Testes de carga com Gatling ou k6

---

## Possíveis erros

**Porta 5432 já em uso:**
```bash
# Descobrir qual processo está usando
lsof -i :5432   # Linux/Mac
netstat -ano | findstr :5432  # Windows
```

**Erro de autenticação no banco:**
Verifique se as variáveis `DB_USER` e `DB_PASS` estão corretas e se o container do PostgreSQL está rodando:
```bash
docker ps
docker logs taskmanager-db
```

**Token JWT expirado (401):**
Faça login novamente via `POST /api/auth/login` para obter um novo token.
