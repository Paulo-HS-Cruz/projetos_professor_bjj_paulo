# Projetos Backend Java — Paulo Cesar Silva

Repositório com três projetos backend em Java 17 + Spring Boot 3.x, cobrindo desde APIs REST com autenticação
até arquitetura de microsserviços. Cada projeto foi estruturado para ser usado como base de estudo e evolução contínua.

---

## Projetos

| Projeto | Descrição | Status |
|---------|-----------|--------|
| [task-manager-api](./task-manager-api) | API REST completa com JWT, roles ADMIN/USER, Docker e CI/CD | Implementado |
| [gym-scheduler](./gym-scheduler) | Sistema de agendamento com Clean Architecture e DDD | Estrutura definida |
| [notification-service](./notification-service) | Microsserviço de notificações com RabbitMQ e AWS SES/SNS | Estrutura definida |

---

## Stack comum

- **Java 17** — streams, records, lambdas
- **Spring Boot 3.2** — web, security, data JPA, validation
- **Maven** — gerenciamento de dependências
- **Docker + Docker Compose** — containerização
- **GitHub Actions** — CI/CD

---

## Por onde começar

Se você é um dev júnior usando este repositório como base:

1. **Comece pelo `task-manager-api`** — ele está 100% implementado e é o melhor projeto para entender o padrão adotado
2. **Leia o código na ordem:** `domain` → `security` → `service` → `controller`
3. **Rode os testes** antes de qualquer mudança: `./mvnw test`
4. **Implemente o `gym-scheduler`** seguindo a estrutura em `STRUCTURE.md` — o design de domínio já está definido
5. **Implemente o `notification-service`** por último — requer RabbitMQ rodando localmente

---

## Pré-requisitos globais

- [Java 17+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/) ou use o `mvnw` embutido
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

---

## Autor

**Paulo Cesar Silva** — [github.com/paulocesarsilva](https://github.com/paulocesarsilva) | [linkedin.com/in/paulocesarsilva](https://linkedin.com/in/paulocesarsilva)
