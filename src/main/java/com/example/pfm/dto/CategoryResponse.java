package com.example.pfm.dto;

import com.example.pfm.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String name;
    private TransactionType type;
    private boolean custom;
}
