package com.example.pfm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    
    @NotBlank(message = "Goal name is required")
    private String goalName;
    
    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;
    
    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
    
    private LocalDate startDate;
}
