package com.paulocesar.taskmanager.dto.request;

import com.paulocesar.taskmanager.domain.enums.TaskPriority;
import com.paulocesar.taskmanager.domain.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @Size(min = 3, max = 150, message = "Título deve ter entre 3 e 150 caracteres")
        String title,

        String description,

        TaskStatus status,

        TaskPriority priority,

        @FutureOrPresent(message = "Data de vencimento não pode ser no passado")
        LocalDate dueDate
) {}
