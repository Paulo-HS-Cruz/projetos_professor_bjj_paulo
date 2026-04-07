package com.paulocesar.taskmanager.controller;

import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;
import com.paulocesar.taskmanager.dto.request.TaskRequest;
import com.paulocesar.taskmanager.dto.request.UpdateTaskRequest;
import com.paulocesar.taskmanager.dto.response.PageResponse;
import com.paulocesar.taskmanager.dto.response.TaskResponse;
import com.paulocesar.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Gerenciamento de tarefas")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova tarefa")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Listar tarefas com paginação e filtro de status")
    public ResponseEntity<PageResponse<TaskResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal User currentUser) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findAll(currentUser, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID")
    public ResponseEntity<TaskResponse> findById(@PathVariable Long id,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.findById(id, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateTaskRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(taskService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar tarefa")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
