package com.paulocesar.taskmanager.service;

import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.UserRole;
import com.paulocesar.taskmanager.domain.repository.UserRepository;
import com.paulocesar.taskmanager.dto.response.UserResponse;
import com.paulocesar.taskmanager.exception.ResourceNotFoundException;
import com.paulocesar.taskmanager.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id, User currentUser) {
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isSelf = currentUser.getId().equals(id);

        if (!isAdmin && !isSelf) {
            throw new UnauthorizedException("Acesso negado.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        return UserResponse.from(user);
    }

    @Transactional
    public void deactivate(Long id, User currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Apenas administradores podem desativar usuários.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.setActive(false);
        userRepository.save(user);
    }
}
