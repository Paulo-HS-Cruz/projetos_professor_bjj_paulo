package com.paulocesar.taskmanager.service;

import com.paulocesar.taskmanager.domain.entity.User;
import com.paulocesar.taskmanager.domain.enums.UserRole;
import com.paulocesar.taskmanager.domain.repository.UserRepository;
import com.paulocesar.taskmanager.dto.request.LoginRequest;
import com.paulocesar.taskmanager.dto.request.RegisterRequest;
import com.paulocesar.taskmanager.dto.response.AuthResponse;
import com.paulocesar.taskmanager.exception.BusinessException;
import com.paulocesar.taskmanager.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .name("Paulo Cesar")
                .email("paulo@email.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("register - deve cadastrar usuário com sucesso")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Paulo Cesar", "paulo@email.com", "senha123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("paulo@email.com");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - deve lançar exceção quando email já existe")
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Paulo", "paulo@email.com", "senha123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - deve autenticar e retornar token")
    void login_Success() {
        LoginRequest request = new LoginRequest("paulo@email.com", "senha123");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(tokenProvider.generateToken(mockUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.type()).isEqualTo("Bearer");
    }
}
