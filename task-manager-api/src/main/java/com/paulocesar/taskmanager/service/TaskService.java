package com.paulocesar.taskmanager.service;

import com.paulocesar.taskmanager.domain.entity.Task;
import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;
import com.paulocesar.taskmanager.domain.enums.UserRole;
import com.paulocesar.taskmanager.domain.repository.TaskRepository;
import com.paulocesar.taskmanager.dto.request.TaskRequest;
import com.paulocesar.taskmanager.dto.request.UpdateTaskRequest;
import com.paulocesar.taskmanager.dto.response.PageResponse;
import com.paulocesar.taskmanager.dto.response.TaskResponse;
import com.paulocesar.taskmanager.exception.ResourceNotFoundException;
import com.paulocesar.taskmanager.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public TaskResponse create(TaskRequest request, User currentUser) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .dueDate(request.dueDate())
                .status(TaskStatus.PENDING)
                .owner(currentUser)
                .build();

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> findAll(User currentUser, TaskStatus status, Pageable pageable) {
        Page<Task> page;

        if (currentUser.getRole() == UserRole.ADMIN) {
            page = (status != null)
                    ? taskRepository.findAll(pageable)
                    : taskRepository.findAll(pageable);
        } else {
            page = (status != null)
                    ? taskRepository.findByOwnerIdAndStatus(currentUser.getId(), status, pageable)
                    : taskRepository.findByOwnerId(currentUser.getId(), pageable);
        }

        return PageResponse.from(page.map(TaskResponse::from));
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id, User currentUser) {
        Task task = taskRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        checkAccess(task, currentUser);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request, User currentUser) {
        Task task = taskRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        checkAccess(task, currentUser);

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Task task = taskRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        checkAccess(task, currentUser);
        taskRepository.delete(task);
    }

    private void checkAccess(Task task, User currentUser) {
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = task.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("Acesso negado à tarefa: " + task.getId());
        }
    }
}
