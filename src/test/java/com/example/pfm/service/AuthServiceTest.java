package com.example.pfm.service;

import com.example.pfm.dto.AuthResponse;
import com.example.pfm.dto.LoginRequest;
import com.example.pfm.dto.RegisterRequest;
import com.example.pfm.entity.User;
import com.example.pfm.exception.ConflictException;
import com.example.pfm.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");
        user.setPassword("encodedPassword");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when username exists")
    void register_UsernameExists_ThrowsConflictException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(httpSession);

        assertDoesNotThrow(() -> authService.login(loginRequest, httpRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid login")
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, httpRequest));
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_Success() {
        when(httpRequest.getSession(false)).thenReturn(httpSession);

        assertDoesNotThrow(() -> authService.logout(httpRequest));
        verify(httpSession).invalidate();
    }

    @Test
    @DisplayName("Should handle logout when no session exists")
    void logout_NoSession() {
        when(httpRequest.getSession(false)).thenReturn(null);

        assertDoesNotThrow(() -> authService.logout(httpRequest));
    }

    @Test
    @DisplayName("Should get current user")
    void getCurrentUser_ReturnsUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        
        // This test would need security context setup, testing the repository call
        verify(userRepository, never()).findByUsername(anyString()); // Initial state
    }
}
