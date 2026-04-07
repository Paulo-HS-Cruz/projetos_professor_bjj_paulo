package com.paulocesar.taskmanager.service;

import com.paulocesar.taskmanager.domain.entity.Task;
import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.TaskPriority;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;
import com.paulocesar.taskmanager.domain.enums.UserRole;
import com.paulocesar.taskmanager.domain.repository.TaskRepository;
import com.paulocesar.taskmanager.dto.request.TaskRequest;
import com.paulocesar.taskmanager.dto.request.UpdateTaskRequest;
import com.paulocesar.taskmanager.dto.response.TaskResponse;
import com.paulocesar.taskmanager.exception.ResourceNotFoundException;
import com.paulocesar.taskmanager.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User ownerUser;
    private User adminUser;
    private User otherUser;
    private Task task;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id(1L).name("Paulo").email("paulo@email.com").role(UserRole.USER).active(true).build();
        adminUser = User.builder().id(2L).name("Admin").email("admin@email.com").role(UserRole.ADMIN).active(true).build();
        otherUser = User.builder().id(3L).name("Other").email("other@email.com").role(UserRole.USER).active(true).build();

        task = Task.builder()
                .id(1L)
                .title("Estudar Spring Boot")
                .description("Revisar capítulo 5")
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .owner(ownerUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("create - deve criar tarefa com sucesso")
    void create_Success() {
        TaskRequest request = new TaskRequest("Estudar Spring Boot", "Revisar capítulo 5", TaskPriority.HIGH, LocalDate.now().plusDays(3));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(request, ownerUser);

        assertThat(response.title()).isEqualTo("Estudar Spring Boot");
        assertThat(response.ownerId()).isEqualTo(ownerUser.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("findById - deve retornar tarefa ao dono")
    void findById_Owner_Success() {
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(1L, ownerUser);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Estudar Spring Boot");
    }

    @Test
    @DisplayName("findById - ADMIN deve acessar qualquer tarefa")
    void findById_Admin_CanAccessAnyTask() {
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(1L, adminUser);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - deve lançar exceção quando tarefa não existe")
    void findById_NotFound_ThrowsException() {
        when(taskRepository.findByIdWithOwner(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L, ownerUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById - deve lançar exceção quando usuário não é o dono")
    void findById_NotOwner_ThrowsUnauthorized() {
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(1L, otherUser))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("update - deve atualizar tarefa com sucesso")
    void update_Success() {
        UpdateTaskRequest request = new UpdateTaskRequest("Título atualizado", null, TaskStatus.IN_PROGRESS, null, null);
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.update(1L, request, ownerUser);

        assertThat(task.getTitle()).isEqualTo("Título atualizado");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("delete - deve deletar tarefa do dono")
    void delete_Owner_Success() {
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L, ownerUser);

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("delete - outro usuário não pode deletar tarefa alheia")
    void delete_NotOwner_ThrowsUnauthorized() {
        when(taskRepository.findByIdWithOwner(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.delete(1L, otherUser))
                .isInstanceOf(UnauthorizedException.class);

        verify(taskRepository, never()).delete(any());
    }
}
