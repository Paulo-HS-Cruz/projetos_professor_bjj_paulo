package com.paulocesar.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulocesar.taskmanager.domain.enums.TaskPriority;
import com.paulocesar.taskmanager.dto.request.RegisterRequest;
import com.paulocesar.taskmanager.dto.request.TaskRequest;
import com.paulocesar.taskmanager.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Task Integration Tests")
class TaskIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest register = new RegisterRequest("Paulo", "paulo@tasktest.com", "senha123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andReturn();

        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        userToken = auth.token();
    }

    @Test
    @DisplayName("POST /api/tasks - deve criar tarefa autenticado")
    void createTask_Authenticated_Returns201() throws Exception {
        TaskRequest request = new TaskRequest("Estudar Docker", "Ver docs oficiais", TaskPriority.MEDIUM, LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Estudar Docker"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("POST /api/tasks - deve retornar 403 sem token")
    void createTask_Unauthenticated_Returns403() throws Exception {
        TaskRequest request = new TaskRequest("Tarefa", null, TaskPriority.LOW, null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/tasks - deve listar tarefas do usuário autenticado")
    void listTasks_Authenticated_ReturnsPage() throws Exception {
        TaskRequest request = new TaskRequest("Tarefa 1", null, TaskPriority.HIGH, null);
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - deve deletar tarefa do dono")
    void deleteTask_Owner_Returns204() throws Exception {
        TaskRequest request = new TaskRequest("Para deletar", null, TaskPriority.LOW, null);
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        Long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
    }
}
