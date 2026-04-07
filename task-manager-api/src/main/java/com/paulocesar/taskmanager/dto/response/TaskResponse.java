package com.paulocesar.taskmanager.dto.response;

import com.paulocesar.taskmanager.domain.entity.Task;
import com.paulocesar.taskmanager.domain.enums.TaskPriority;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getOwner().getId(),
                task.getOwner().getName(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
