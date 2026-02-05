package com.example.pfm.controller;

import com.example.pfm.dto.AuthResponse;
import com.example.pfm.dto.LoginRequest;
import com.example.pfm.dto.MessageResponse;
import com.example.pfm.dto.RegisterRequest;
import com.example.pfm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration, login, and logout.
 * 
 * @author PFM Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user.
     * 
     * @param request the registration details
     * @return AuthResponse with user ID and success message (HTTP 201)
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and creates a session.
     * 
     * @param request the login credentials
     * @param httpRequest the HTTP request for session management
     * @return MessageResponse confirming successful login (HTTP 200)
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request, 
                                                  HttpServletRequest httpRequest) {
        authService.login(request, httpRequest);
        return ResponseEntity.ok(new MessageResponse("Login successful"));
    }

    /**
     * Logs out the current user by invalidating their session.
     * 
     * @param request the HTTP request containing the session
     * @return MessageResponse confirming successful logout (HTTP 200)
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
}
