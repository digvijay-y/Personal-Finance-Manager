package com.example.pfm.service;

import com.example.pfm.dto.AuthResponse;
import com.example.pfm.dto.LoginRequest;
import com.example.pfm.dto.RegisterRequest;
import com.example.pfm.entity.User;
import com.example.pfm.exception.ConflictException;
import com.example.pfm.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class handling user authentication operations.
 * Provides functionality for user registration, login, logout, and session management.
 * 
 * @author PFM Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     * 
     * @param request the registration request containing username, password, fullName, and phoneNumber
     * @return AuthResponse containing success message and the new user's ID
     * @throws ConflictException if the username already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userRepository.save(user);
        
        return new AuthResponse("User registered successfully", savedUser.getId());
    }

    /**
     * Authenticates a user and creates an HTTP session.
     * 
     * @param request the login request containing username and password
     * @param httpRequest the HTTP request for session creation
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    public void login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    }

    /**
     * Logs out the current user by invalidating their session.
     * 
     * @param request the HTTP request containing the session to invalidate
     */
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     * 
     * @return the current User entity, or null if not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
