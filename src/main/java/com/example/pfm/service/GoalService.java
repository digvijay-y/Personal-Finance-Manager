package com.example.pfm.service;

import com.example.pfm.dto.*;
import com.example.pfm.entity.Goal;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import com.example.pfm.exception.ResourceNotFoundException;
import com.example.pfm.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing financial goals.
 * Handles creation, tracking, and progress calculation for savings goals.
 * 
 * @author PFM Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionService transactionService;

    /**
     * Creates a new financial goal for a user.
     * 
     * @param request the goal details including name, target amount, target date, and optional start date
     * @param user the user creating the goal
     * @return GoalResponse containing the created goal with progress information
     * @throws BadRequestException if target amount is non-positive, target date is in the past, or start date is after target date
     */
    @Transactional
    public GoalResponse createGoal(GoalRequest request, User user) {
        // Validate target amount
        if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target amount must be greater than zero");
        }

        // Validate target date is in the future
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        // Set start date to today if not provided
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        // Validate start date is before target date
        if (!startDate.isBefore(request.getTargetDate())) {
            throw new BadRequestException("Start date must be before target date");
        }

        Goal goal = new Goal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setStartDate(startDate);
        goal.setUser(user);

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, user);
    }

    /**
     * Retrieves all goals for a user with current progress calculations.
     * 
     * @param user the user whose goals to retrieve
     * @return GoalListResponse containing all goals with progress information
     */
    public GoalListResponse getAllGoals(User user) {
        List<Goal> goals = goalRepository.findByUser(user);
        List<GoalResponse> responses = goals.stream()
                .map(g -> mapToResponse(g, user))
                .collect(Collectors.toList());
        return new GoalListResponse(responses);
    }

    /**
     * Retrieves a specific goal by ID with current progress.
     * 
     * @param id the goal ID
     * @param user the user who owns the goal
     * @return GoalResponse containing the goal details with progress information
     * @throws ResourceNotFoundException if the goal is not found
     */
    public GoalResponse getGoal(Long id, User user) {
        Goal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return mapToResponse(goal, user);
    }

    /**
     * Updates an existing goal.
     * 
     * @param id the goal ID to update
     * @param request the update request containing optional new values for target amount and target date
     * @param user the user who owns the goal
     * @return GoalResponse containing the updated goal with progress information
     * @throws ResourceNotFoundException if the goal is not found
     * @throws BadRequestException if new target amount is non-positive or new target date is invalid
     */
    @Transactional
    public GoalResponse updateGoal(Long id, GoalUpdateRequest request, User user) {
        Goal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (request.getTargetAmount() != null) {
            if (request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Target amount must be greater than zero");
            }
            goal.setTargetAmount(request.getTargetAmount());
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            if (!goal.getStartDate().isBefore(request.getTargetDate())) {
                throw new BadRequestException("Start date must be before target date");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, user);
    }

    /**
     * Deletes a goal.
     * 
     * @param id the goal ID to delete
     * @param user the user who owns the goal
     * @throws ResourceNotFoundException if the goal is not found
     */
    @Transactional
    public void deleteGoal(Long id, User user) {
        Goal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goalRepository.delete(goal);
    }

    private GoalResponse mapToResponse(Goal goal, User user) {
        // Calculate progress from transactions since start date
        List<Transaction> transactions = transactionService.getTransactionsSinceDate(user, goal.getStartDate());
        
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }
        
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }
        
        double progressPercentage = currentProgress
                .multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                .doubleValue();
        
        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress,
                progressPercentage,
                remainingAmount
        );
    }
}
