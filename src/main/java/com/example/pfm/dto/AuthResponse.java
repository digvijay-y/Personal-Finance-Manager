package com.example.pfm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private Long userId;
    
    public AuthResponse(String message) {
        this.message = message;
    }
}
