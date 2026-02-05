package com.example.pfm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String goalName;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;
    
    @Column(nullable = false)
    private LocalDate targetDate;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
