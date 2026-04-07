package com.paulocesar.taskmanager.dto.request;

import com.paulocesar.taskmanager.domain.enums.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(

        @NotBlank(message = "Título é obrigatório")
        @Size(min = 3, max = 150, message = "Título deve ter entre 3 e 150 caracteres")
        String title,

        String description,

        @NotNull(message = "Prioridade é obrigatória")
        TaskPriority priority,

        @FutureOrPresent(message = "Data de vencimento não pode ser no passado")
        LocalDate dueDate
) {}
