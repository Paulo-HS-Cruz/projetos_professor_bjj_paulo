package com.paulocesar.taskmanager.service;

import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.UserRole;
import com.paulocesar.taskmanager.domain.repository.UserRepository;
import com.paulocesar.taskmanager.dto.request.LoginRequest;
import com.paulocesar.taskmanager.dto.request.RegisterRequest;
import com.paulocesar.taskmanager.dto.response.AuthResponse;
import com.paulocesar.taskmanager.exception.BusinessException;
import com.paulocesar.taskmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = tokenProvider.generateToken(saved);

        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        String token = tokenProvider.generateToken(user);

        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
