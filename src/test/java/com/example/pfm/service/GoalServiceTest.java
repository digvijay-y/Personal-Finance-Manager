package com.example.pfm.service;

import com.example.pfm.dto.GoalListResponse;
import com.example.pfm.dto.GoalRequest;
import com.example.pfm.dto.GoalResponse;
import com.example.pfm.dto.GoalUpdateRequest;
import com.example.pfm.entity.Goal;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import com.example.pfm.exception.ResourceNotFoundException;
import com.example.pfm.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Goal goal;
    private GoalRequest goalRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        goal = new Goal();
        goal.setId(1L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setTargetDate(LocalDate.now().plusYears(1));
        goal.setStartDate(LocalDate.now());
        goal.setUser(user);

        goalRequest = new GoalRequest();
        goalRequest.setGoalName("Emergency Fund");
        goalRequest.setTargetAmount(new BigDecimal("10000.00"));
        goalRequest.setTargetDate(LocalDate.now().plusYears(1));
    }

    @Test
    @DisplayName("Should create goal successfully")
    void createGoal_Success() {
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionService.getTransactionsSinceDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse response = goalService.createGoal(goalRequest, user);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    @DisplayName("Should create goal with default start date")
    void createGoal_DefaultStartDate() {
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionService.getTransactionsSinceDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse response = goalService.createGoal(goalRequest, user);

        assertNotNull(response);
        assertNotNull(response.getStartDate());
    }

    @Test
    @DisplayName("Should throw BadRequestException for zero target amount")
    void createGoal_ZeroAmount_ThrowsException() {
        goalRequest.setTargetAmount(BigDecimal.ZERO);

        assertThrows(BadRequestException.class, 
                () -> goalService.createGoal(goalRequest, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for negative target amount")
    void createGoal_NegativeAmount_ThrowsException() {
        goalRequest.setTargetAmount(new BigDecimal("-1000.00"));

        assertThrows(BadRequestException.class, 
                () -> goalService.createGoal(goalRequest, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for past target date")
    void createGoal_PastTargetDate_ThrowsException() {
        goalRequest.setTargetDate(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, 
                () -> goalService.createGoal(goalRequest, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException when start date is after target date")
    void createGoal_StartAfterTarget_ThrowsException() {
        goalRequest.setStartDate(LocalDate.now().plusYears(2));
        goalRequest.setTargetDate(LocalDate.now().plusYears(1));

        assertThrows(BadRequestException.class, 
                () -> goalService.createGoal(goalRequest, user));
    }

    @Test
    @DisplayName("Should get all goals for user")
    void getAllGoals_Success() {
        List<Goal> goals = Arrays.asList(goal);
        when(goalRepository.findByUser(user)).thenReturn(goals);
        when(transactionService.getTransactionsSinceDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalListResponse response = goalService.getAllGoals(user);

        assertNotNull(response);
        assertEquals(1, response.getGoals().size());
    }

    @Test
    @DisplayName("Should get goal by id")
    void getGoal_Success() {
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionService.getTransactionsSinceDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse response = goalService.getGoal(1L, user);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when goal not found")
    void getGoal_NotFound_ThrowsException() {
        when(goalRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> goalService.getGoal(999L, user));
    }

    @Test
    @DisplayName("Should calculate goal progress correctly")
    void getGoal_CalculatesProgress() {
        Transaction income = new Transaction();
        income.setAmount(new BigDecimal("5000.00"));
        income.setType(TransactionType.INCOME);

        Transaction expense = new Transaction();
        expense.setAmount(new BigDecimal("1000.00"));
        expense.setType(TransactionType.EXPENSE);

        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionService.getTransactionsSinceDate(user, goal.getStartDate()))
                .thenReturn(Arrays.asList(income, expense));

        GoalResponse response = goalService.getGoal(1L, user);

        assertEquals(new BigDecimal("4000.00"), response.getCurrentProgress());
        assertEquals(40.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("6000.00"), response.getRemainingAmount());
    }

    @Test
    @DisplayName("Should update goal successfully")
    void updateGoal_Success() {
        GoalUpdateRequest updateRequest = new GoalUpdateRequest();
        updateRequest.setTargetAmount(new BigDecimal("15000.00"));

        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(transactionService.getTransactionsSinceDate(any(User.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalResponse response = goalService.updateGoal(1L, updateRequest, user);

        assertNotNull(response);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent goal")
    void updateGoal_NotFound_ThrowsException() {
        GoalUpdateRequest updateRequest = new GoalUpdateRequest();
        when(goalRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> goalService.updateGoal(999L, updateRequest, user));
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void deleteGoal_Success() {
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));

        assertDoesNotThrow(() -> goalService.deleteGoal(1L, user));
        verify(goalRepository).delete(goal);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent goal")
    void deleteGoal_NotFound_ThrowsException() {
        when(goalRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> goalService.deleteGoal(999L, user));
    }
}
