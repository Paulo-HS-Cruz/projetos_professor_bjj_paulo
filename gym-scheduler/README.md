# GymScheduler — Sistema de Agendamento para Academia

Sistema de agendamento de aulas para academia de Jiu-Jitsu, modelado com **Clean Architecture** e **Domain-Driven Design (DDD)**.
O design do domínio e a estrutura de camadas estão definidos — o projeto está pronto para ser implementado.

> **Status:** Estrutura e arquitetura definidas. Implementação a realizar.

---

## O que este projeto cobre

- Modelagem de domínio rico com DDD: entidades, value objects e regras de negócio encapsuladas
- Clean Architecture com separação clara entre `domain`, `application` e `infrastructure`
- Eliminação do problema N+1 com fetch join no Hibernate
- Controle de vagas, cancelamentos e histórico de presenças
- Validação de conflito de horário no nível do domínio

---

## Tecnologias

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Persistência | Spring Data JPA + Hibernate |
| Banco | MySQL 8 |
| Build | Maven |
| Container | Docker + Docker Compose |
| Testes | JUnit 5 + Mockito |

---

## Pré-requisitos

- Java 17+
- Maven 3.9+ (ou `./mvnw`)
- Docker e Docker Compose

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
| Artifact | `gym-scheduler` |
| Java | 17 |

**Dependências a adicionar:**
- Spring Web
- Spring Data JPA
- MySQL Driver
- Spring Boot DevTools
- Lombok
- Validation
- SpringDoc OpenAPI (adicionar manualmente no `pom.xml`)

### 2. Adicionar dependência do SpringDoc no `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.4.0</version>
</dependency>
```

### 3. Subir o banco MySQL com Docker

```bash
docker run -d \
  --name gymscheduler-db \
  -e MYSQL_DATABASE=gymscheduler \
  -e MYSQL_ROOT_PASSWORD=root \
  -p 3306:3306 \
  mysql:8-debian
```

### 4. Configurar o `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gymscheduler?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

server:
  port: 8080
```

---

## Estrutura de pastas a criar

Crie os pacotes na seguinte ordem (respeite a separação de camadas):

```
src/main/java/com/paulocesar/gymscheduler/
├── domain/                    ← Crie primeiro — sem dependências externas
│   ├── entity/
│   │   ├── Student.java
│   │   ├── Instructor.java
│   │   ├── ClassSession.java
│   │   ├── Schedule.java
│   │   └── Attendance.java
│   ├── enums/
│   │   ├── BeltRank.java
│   │   └── ScheduleStatus.java
│   ├── repository/            ← Apenas interfaces (portas)
│   └── exception/
│
├── application/               ← Crie depois — depende apenas do domain
│   ├── usecase/
│   │   ├── student/
│   │   ├── schedule/
│   │   └── class/
│   └── dto/
│
└── infrastructure/            ← Crie por último — implementa as interfaces do domain
    ├── persistence/
    │   ├── entity/            ← Entidades JPA separadas das entidades de domínio
    │   ├── repository/
    │   └── mapper/
    └── web/
        └── controller/
```

> Consulte o arquivo [STRUCTURE.md](./STRUCTURE.md) para ver todos os arquivos com descrições detalhadas.

---

## Ordem de implementação sugerida

### Etapa 1 — Entidades de domínio
Comece pelo `Student` e `Instructor` (mais simples), depois `ClassSession` e por último `Schedule` (que tem as regras mais complexas).

Exemplo de como `Schedule` deve encapsular sua regra:
```java
// domain/entity/Schedule.java
public class Schedule {
    // ...

    public static Schedule book(Student student, ClassSession classSession) {
        if (!classSession.hasAvailableSlots()) {
            throw new NoSlotsAvailableException("Aula sem vagas: " + classSession.getId());
        }
        classSession.decrementSlot();
        return new Schedule(student, classSession, ScheduleStatus.CONFIRMED);
    }
}
```

### Etapa 2 — Repositórios (interfaces no domain)
```java
// domain/repository/ScheduleRepository.java
public interface ScheduleRepository {
    Schedule save(Schedule schedule);
    List<Schedule> findByStudentId(Long studentId);
    boolean existsConflict(Long studentId, LocalDateTime start, LocalDateTime end);
}
```

### Etapa 3 — Casos de uso (application layer)
O `BookClassUseCase` é o central — implemente-o validando vagas e conflito de horário:
```java
@UseCase // ou @Service
public class BookClassUseCase {
    public ScheduleResponse execute(BookClassRequest request) {
        // 1. Busca a aula
        // 2. Valida vagas disponíveis
        // 3. Valida conflito de horário do aluno
        // 4. Cria o agendamento
        // 5. Salva e retorna
    }
}
```

### Etapa 4 — Infraestrutura JPA
Implemente os repositórios usando Spring Data JPA. Use `@Query` com `JOIN FETCH` para evitar N+1:
```java
// infrastructure/persistence/repository/ClassSessionJpaRepository.java
@Query("SELECT cs FROM ClassSessionJpaEntity cs JOIN FETCH cs.instructor WHERE cs.dayOfWeek = :day")
List<ClassSessionJpaEntity> findByDayWithInstructor(@Param("day") DayOfWeek day);
```

### Etapa 5 — Controllers REST
Implemente os endpoints seguindo o padrão do `task-manager-api`.

---

## Funcionalidades a implementar

- [ ] Cadastro de alunos e professores
- [ ] Criação de aulas com capacidade máxima
- [ ] Agendamento com validação de vagas e conflito de horário
- [ ] Cancelamento de agendamento (libera vaga)
- [ ] Histórico de presenças por aluno
- [ ] Listagem de aulas disponíveis por dia da semana
- [ ] Relatório de frequência

---

## Referência

Este projeto implementa os conceitos de Clean Architecture descritos por Robert C. Martin e DDD por Eric Evans.
Para entender o padrão adotado, leia primeiro o código do `task-manager-api` que está na mesma base.
