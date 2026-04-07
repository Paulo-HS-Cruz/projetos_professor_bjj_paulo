# GymScheduler — Estrutura do Projeto

## Stack
Java 17 | Spring Boot 3.x | JPA/Hibernate | MySQL | Clean Architecture | DDD

## Arquitetura — Clean Architecture + DDD

```
gym-scheduler/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .github/workflows/ci.yml
│
└── src/
    ├── main/java/com/paulocesar/gymscheduler/
    │
    │   ├── domain/                          ← Núcleo do domínio (sem dependências externas)
    │   │   ├── entity/
    │   │   │   ├── Student.java             ← Aluno (nome, email, faixa, ativo)
    │   │   │   ├── Instructor.java          ← Professor (nome, especialidade, carga horária)
    │   │   │   ├── ClassSession.java        ← Aula (instrutor, horário, capacidade máx, vagas)
    │   │   │   ├── Schedule.java            ← Agendamento (aluno + aula + status)
    │   │   │   └── Attendance.java          ← Presença (schedule + data + confirmado)
    │   │   ├── enums/
    │   │   │   ├── BeltRank.java            ← WHITE, BLUE, PURPLE, BROWN, BLACK
    │   │   │   ├── ScheduleStatus.java      ← CONFIRMED, CANCELLED, COMPLETED
    │   │   │   └── DayOfWeekPT.java
    │   │   ├── repository/                  ← Interfaces (portas de saída)
    │   │   │   ├── StudentRepository.java
    │   │   │   ├── InstructorRepository.java
    │   │   │   ├── ClassSessionRepository.java
    │   │   │   └── ScheduleRepository.java
    │   │   └── exception/
    │   │       ├── NoSlotsAvailableException.java
    │   │       ├── ScheduleConflictException.java
    │   │       └── ResourceNotFoundException.java
    │   │
    │   ├── application/                     ← Casos de uso (orquestração)
    │   │   ├── usecase/
    │   │   │   ├── student/
    │   │   │   │   ├── RegisterStudentUseCase.java
    │   │   │   │   └── ListStudentsUseCase.java
    │   │   │   ├── schedule/
    │   │   │   │   ├── BookClassUseCase.java        ← Valida vagas + conflito de horário
    │   │   │   │   ├── CancelScheduleUseCase.java
    │   │   │   │   └── GetStudentHistoryUseCase.java
    │   │   │   └── class/
    │   │   │       ├── CreateClassSessionUseCase.java
    │   │   │       └── GetAvailableClassesUseCase.java
    │   │   └── dto/
    │   │       ├── request/
    │   │       │   ├── RegisterStudentRequest.java
    │   │       │   ├── BookClassRequest.java
    │   │       │   └── CreateClassSessionRequest.java
    │   │       └── response/
    │   │           ├── StudentResponse.java
    │   │           ├── ClassSessionResponse.java
    │   │           └── ScheduleResponse.java
    │   │
    │   ├── infrastructure/                  ← Adaptadores (JPA, controllers)
    │   │   ├── persistence/
    │   │   │   ├── entity/                  ← Entidades JPA (separadas do domínio)
    │   │   │   │   ├── StudentJpaEntity.java
    │   │   │   │   ├── InstructorJpaEntity.java
    │   │   │   │   ├── ClassSessionJpaEntity.java
    │   │   │   │   └── ScheduleJpaEntity.java
    │   │   │   ├── repository/              ← Implementações dos repositórios
    │   │   │   │   ├── StudentRepositoryImpl.java
    │   │   │   │   └── ScheduleRepositoryImpl.java
    │   │   │   └── mapper/
    │   │   │       └── ScheduleMapper.java
    │   │   └── web/
    │   │       ├── controller/
    │   │       │   ├── StudentController.java
    │   │       │   ├── InstructorController.java
    │   │       │   ├── ClassSessionController.java
    │   │       │   └── ScheduleController.java
    │   │       └── exception/
    │   │           └── GlobalExceptionHandler.java
    │   │
    │   └── GymSchedulerApplication.java
    │
    └── test/java/com/paulocesar/gymscheduler/
        ├── domain/
        │   └── usecase/
        │       ├── BookClassUseCaseTest.java    ← Testa: vagas, conflito de horário
        │       └── CancelScheduleUseCaseTest.java
        └── integration/
            └── ScheduleIntegrationTest.java
```

## Pontos de destaque no código

### BookClassUseCase — regra de negócio central
```java
// 1. Valida se a aula existe e tem vagas
// 2. Verifica conflito de horário do aluno
// 3. Cria o Schedule com status CONFIRMED
// 4. Decrementa vagas disponíveis (locking otimista com @Version)
```

### Otimização N+1 com fetch join
```java
@Query("SELECT cs FROM ClassSessionJpaEntity cs JOIN FETCH cs.instructor WHERE cs.dayOfWeek = :day")
List<ClassSessionJpaEntity> findByDayWithInstructor(@Param("day") DayOfWeek day);
```
