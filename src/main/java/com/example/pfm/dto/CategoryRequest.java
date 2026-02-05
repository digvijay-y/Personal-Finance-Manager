package com.example.pfm.dto;

import com.example.pfm.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    @NotNull(message = "Category type is required")
    private TransactionType type;
}
