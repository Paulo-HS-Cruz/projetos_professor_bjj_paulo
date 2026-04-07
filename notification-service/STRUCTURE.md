# Notification Service — Estrutura do Projeto

## Stack
Java 17 | Spring Boot 3.x | RabbitMQ | AWS SES | AWS SNS | Docker | AWS EC2

## Arquitetura — Event-Driven Microservice

```
notification-service/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .github/workflows/ci.yml
│
└── src/
    ├── main/java/com/paulocesar/notification/
    │
    │   ├── config/
    │   │   ├── RabbitMQConfig.java          ← Declara exchanges, queues e bindings
    │   │   ├── AwsSesConfig.java            ← Bean do SesClient (credenciais via env)
    │   │   └── AwsSnsConfig.java            ← Bean do SnsClient
    │   │
    │   ├── consumer/                        ← Consome mensagens do RabbitMQ
    │   │   ├── EmailNotificationConsumer.java   ← @RabbitListener("queue.email")
    │   │   ├── SmsNotificationConsumer.java     ← @RabbitListener("queue.sms")
    │   │   └── PushNotificationConsumer.java
    │   │
    │   ├── provider/                        ← Adaptadores para serviços externos
    │   │   ├── EmailProvider.java           ← Interface
    │   │   ├── SmsProvider.java             ← Interface
    │   │   ├── aws/
    │   │   │   ├── AwsSesEmailProvider.java ← Envia via AWS SES SDK v2
    │   │   │   └── AwsSnsProvider.java      ← Envia SMS via AWS SNS
    │   │   └── stub/
    │   │       └── LogEmailProvider.java    ← Stub para testes locais
    │   │
    │   ├── service/
    │   │   ├── NotificationDispatcher.java  ← Orquestra: valida + escolhe provider + envia
    │   │   └── RetryService.java            ← Lógica de retry com dead-letter queue
    │   │
    │   ├── dto/
    │   │   ├── EmailNotificationEvent.java  ← {to, subject, body, templateId}
    │   │   ├── SmsNotificationEvent.java    ← {phoneNumber, message}
    │   │   └── NotificationResult.java      ← {success, messageId, timestamp}
    │   │
    │   ├── domain/
    │   │   ├── entity/
    │   │   │   └── NotificationLog.java     ← Histórico (destinatário, tipo, status, tentativas)
    │   │   ├── enums/
    │   │   │   ├── NotificationType.java    ← EMAIL, SMS, PUSH
    │   │   │   └── NotificationStatus.java  ← PENDING, SENT, FAILED
    │   │   └── repository/
    │   │       └── NotificationLogRepository.java
    │   │
    │   └── NotificationServiceApplication.java
    │
    └── test/java/com/paulocesar/notification/
        ├── service/
        │   └── NotificationDispatcherTest.java  ← Mockito: testa dispatch, retry, fallback
        └── consumer/
            └── EmailConsumerTest.java           ← @EmbeddedAmqp ou Testcontainers RabbitMQ
```

## Fluxo de mensagem

```
Producer (outro serviço)
        │
        ▼  RabbitMQ Exchange (topic)
        │
   ┌────┴─────────────┐
   │                  │
queue.email        queue.sms
   │                  │
EmailConsumer    SmsConsumer
   │                  │
AwsSesProvider   AwsSnsProvider
   │                  │
 AWS SES           AWS SNS
```

## Dead Letter Queue — retry automático
```yaml
# application.yml
rabbitmq:
  queues:
    email:
      name: queue.email
      dlq: queue.email.dlq      # mensagens com falha vão aqui
      retry-delay-ms: 5000      # aguarda 5s antes de recolocar na fila
      max-retries: 3
```

## Configuração AWS via Parameter Store
```
/notification-service/prod/aws-ses-region
/notification-service/prod/aws-ses-from-email
/notification-service/prod/aws-sns-region
```
