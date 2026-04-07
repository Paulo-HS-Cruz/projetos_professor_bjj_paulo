# Notification Service — Microsserviço de Notificações

Microsserviço assíncrono baseado em eventos para envio de notificações via e-mail e SMS,
usando **RabbitMQ** como message broker e **AWS SES/SNS** como provedores de entrega.

> **Status:** Estrutura e arquitetura definidas. Implementação a realizar.

---

## O que este projeto cobre

- Arquitetura event-driven com RabbitMQ (publish/subscribe)
- Envio de e-mails via AWS SES (Simple Email Service)
- Envio de SMS via AWS SNS (Simple Notification Service)
- Dead Letter Queue (DLQ) para retry automático em caso de falha
- Deploy na AWS EC2 com variáveis via AWS Parameter Store
- Containerização com Docker

---

## Tecnologias

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Mensageria | RabbitMQ 3.x |
| E-mail | AWS SES (SDK v2) |
| SMS | AWS SNS (SDK v2) |
| Persistência | Spring Data JPA (log de notificações) |
| Banco | PostgreSQL |
| Build | Maven |
| Container | Docker + Docker Compose |
| Testes | JUnit 5 + Mockito |

---

## Pré-requisitos

- Java 17+
- Maven 3.9+ (ou `./mvnw`)
- Docker e Docker Compose
- Conta AWS (para SES e SNS em produção — opcional para desenvolvimento)

---

## Como iniciar o projeto do zero

### 1. Gerar a estrutura base com Spring Initializr

Acesse [start.spring.io](https://start.spring.io) com as configurações:

| Campo | Valor |
|-------|-------|
| Project | Maven |
| Language | Java |
| Spring Boot | 3.2.x |
| Group | `com.paulocesar` |
| Artifact | `notification-service` |
| Java | 17 |

**Dependências a adicionar:**
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring AMQP (RabbitMQ)
- Lombok
- Validation

### 2. Adicionar dependências do AWS SDK e RabbitMQ no `pom.xml`

```xml
<!-- AWS SDK v2 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>ses</artifactId>
    <version>2.25.0</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sns</artifactId>
    <version>2.25.0</version>
</dependency>

<!-- Spring AMQP (RabbitMQ) já incluído se você marcou a dependência no Initializr -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

### 3. Subir RabbitMQ e PostgreSQL com Docker

O `docker-compose.yml` vai subir tudo o que você precisa para desenvolver localmente:

```yaml
# docker-compose.yml
version: "3.9"

services:
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: notification-rabbitmq
    ports:
      - "5672:5672"    # AMQP
      - "15672:15672"  # Painel de administração
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

  db:
    image: postgres:16-alpine
    container_name: notification-db
    environment:
      POSTGRES_DB: notificationdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

```bash
docker-compose up -d
```

Painel do RabbitMQ: `http://localhost:15672` (usuário: `guest`, senha: `guest`)

---

### 4. Configurar o `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notificationdb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

app:
  rabbitmq:
    exchange: notification.exchange
    queues:
      email: queue.email
      email-dlq: queue.email.dlq
      sms: queue.sms
      sms-dlq: queue.sms.dlq
  aws:
    region: ${AWS_REGION:us-east-1}
    ses:
      from-email: ${AWS_SES_FROM_EMAIL:noreply@seudominio.com}
    use-stub: ${AWS_USE_STUB:true}   # true = usa log local, false = usa AWS real
```

> Com `aws.use-stub=true`, o serviço apenas imprime a notificação no log — sem precisar de conta AWS.

---

## Estrutura de pastas a criar

```
src/main/java/com/paulocesar/notification/
├── config/
│   ├── RabbitMQConfig.java        ← Declara exchanges, queues e bindings
│   ├── AwsSesConfig.java          ← Bean do SesClient
│   └── AwsSnsConfig.java          ← Bean do SnsClient
│
├── consumer/                      ← Escuta as filas do RabbitMQ
│   ├── EmailNotificationConsumer.java
│   └── SmsNotificationConsumer.java
│
├── provider/                      ← Adaptadores para serviços externos
│   ├── EmailProvider.java         ← Interface
│   ├── SmsProvider.java           ← Interface
│   ├── aws/
│   │   ├── AwsSesEmailProvider.java
│   │   └── AwsSnsProvider.java
│   └── stub/
│       └── LogEmailProvider.java  ← Implementação local para dev/test
│
├── service/
│   └── NotificationDispatcher.java
│
├── dto/
│   ├── EmailNotificationEvent.java
│   └── SmsNotificationEvent.java
│
└── domain/
    ├── entity/
    │   └── NotificationLog.java
    ├── enums/
    │   ├── NotificationType.java
    │   └── NotificationStatus.java
    └── repository/
        └── NotificationLogRepository.java
```

> Consulte o arquivo [STRUCTURE.md](./STRUCTURE.md) para descrições detalhadas de cada arquivo.

---

## Ordem de implementação sugerida

### Etapa 1 — Configuração do RabbitMQ

Configure as filas, exchange e dead-letter queue. Este é o ponto mais crítico do projeto:

```java
// config/RabbitMQConfig.java
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange("notification.exchange");
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("queue.email")
                .withArgument("x-dead-letter-exchange", "notification.exchange")
                .withArgument("x-dead-letter-routing-key", "queue.email.dlq")
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable("queue.email.dlq").build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with("notification.email");
    }
}
```

### Etapa 2 — DTO do evento

```java
// dto/EmailNotificationEvent.java
public record EmailNotificationEvent(
    String to,
    String subject,
    String body,
    String correlationId   // ID para rastrear a notificação
) {}
```

### Etapa 3 — Consumer

```java
// consumer/EmailNotificationConsumer.java
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final NotificationDispatcher dispatcher;

    @RabbitListener(queues = "queue.email")
    public void consume(EmailNotificationEvent event) {
        dispatcher.dispatch(event);
    }
}
```

### Etapa 4 — Provider com stub local

Crie primeiro a implementação stub (para desenvolvimento sem AWS):
```java
// provider/stub/LogEmailProvider.java
@Slf4j
@Component
@ConditionalOnProperty(name = "app.aws.use-stub", havingValue = "true")
public class LogEmailProvider implements EmailProvider {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[STUB] E-mail para: {} | Assunto: {}", to, subject);
    }
}
```

### Etapa 5 — Provider AWS SES (produção)

```java
// provider/aws/AwsSesEmailProvider.java
@Component
@ConditionalOnProperty(name = "app.aws.use-stub", havingValue = "false")
public class AwsSesEmailProvider implements EmailProvider {

    private final SesClient sesClient;
    private final String fromEmail;

    @Override
    public void send(String to, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
            .destination(Destination.builder().toAddresses(to).build())
            .message(Message.builder()
                .subject(Content.builder().data(subject).build())
                .body(Body.builder()
                    .text(Content.builder().data(body).build())
                    .build())
                .build())
            .source(fromEmail)
            .build();

        sesClient.sendEmail(request);
    }
}
```

### Etapa 6 — Log de notificações no banco

Persista cada notificação enviada para auditoria:
```java
// domain/entity/NotificationLog.java
@Entity
public class NotificationLog {
    @Id @GeneratedValue
    private Long id;
    private String recipient;
    private NotificationType type;
    private NotificationStatus status;
    private int attempts;
    private LocalDateTime sentAt;
    private String errorMessage;
}
```

---

## Como testar sem a AWS

Com `aws.use-stub=true` no `application.yml`, o serviço usa o `LogEmailProvider` que apenas imprime no console.

Para publicar uma mensagem de teste no RabbitMQ, use o painel em `http://localhost:15672`:
1. Vá em **Exchanges** → `notification.exchange`
2. Em **Publish message**, coloque routing key `notification.email`
3. No body, coloque um JSON:
```json
{
  "to": "teste@email.com",
  "subject": "Teste de notificação",
  "body": "Mensagem de teste",
  "correlationId": "abc-123"
}
```
4. Clique em **Publish** e observe o log da aplicação

---

## Funcionalidades a implementar

- [ ] Consumer de e-mail com RabbitMQ
- [ ] Consumer de SMS com RabbitMQ
- [ ] Provider stub (log local)
- [ ] Provider AWS SES (produção)
- [ ] Provider AWS SNS para SMS
- [ ] Dead Letter Queue e retry automático
- [ ] Log de notificações no banco
- [ ] Endpoint para consultar histórico de envios
- [ ] Métricas de entrega (taxa de sucesso/falha)
- [ ] Templates de e-mail com Thymeleaf

---

## Configuração AWS (produção)

Para usar em produção, configure as credenciais AWS via variáveis de ambiente:

```bash
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=sua-access-key
export AWS_SECRET_ACCESS_KEY=sua-secret-key
export AWS_SES_FROM_EMAIL=noreply@seudominio.com
export AWS_USE_STUB=false
```

> **Atenção:** O domínio de envio precisa estar verificado no AWS SES. Em modo sandbox, só é possível enviar para e-mails verificados.

---

## Referência

Para entender o padrão de código adotado, consulte primeiro o `task-manager-api` que está 100% implementado.
A estrutura de packages, nomenclatura e boas práticas seguem o mesmo padrão.
