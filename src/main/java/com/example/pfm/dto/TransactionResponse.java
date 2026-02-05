package com.example.pfm.dto;

import com.example.pfm.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String description;
    private TransactionType type;
}
