package com.paulocesar.taskmanager.controller;

import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.dto.response.UserResponse;
import com.paulocesar.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os usuários (apenas ADMIN)")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID (próprio perfil ou ADMIN)")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.findById(id, currentUser));
    }

    @GetMapping("/me")
    @Operation(summary = "Retornar perfil do usuário autenticado")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar usuário (apenas ADMIN)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser) {
        userService.deactivate(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
