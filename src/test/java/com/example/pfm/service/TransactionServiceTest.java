package com.example.pfm.service;

import com.example.pfm.dto.TransactionListResponse;
import com.example.pfm.dto.TransactionRequest;
import com.example.pfm.dto.TransactionResponse;
import com.example.pfm.dto.TransactionUpdateRequest;
import com.example.pfm.entity.Category;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import com.example.pfm.exception.ResourceNotFoundException;
import com.example.pfm.repository.TransactionRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Transaction transaction;
    private TransactionRequest transactionRequest;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Salary");
        category.setType(TransactionType.INCOME);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setDate(LocalDate.of(2024, 1, 15));
        transaction.setCategory("Salary");
        transaction.setDescription("January Salary");
        transaction.setType(TransactionType.INCOME);
        transaction.setUser(user);

        transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("5000.00"));
        transactionRequest.setDate(LocalDate.of(2024, 1, 15));
        transactionRequest.setCategory("Salary");
        transactionRequest.setDescription("January Salary");
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void createTransaction_Success() {
        when(categoryService.findCategoryByNameForUser("Salary", user)).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(transactionRequest, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for future date")
    void createTransaction_FutureDate_ThrowsException() {
        transactionRequest.setDate(LocalDate.now().plusDays(1));

        assertThrows(BadRequestException.class, 
                () -> transactionService.createTransaction(transactionRequest, user));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid category")
    void createTransaction_InvalidCategory_ThrowsException() {
        when(categoryService.findCategoryByNameForUser("Invalid", user)).thenReturn(null);
        transactionRequest.setCategory("Invalid");

        assertThrows(BadRequestException.class, 
                () -> transactionService.createTransaction(transactionRequest, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for zero amount")
    void createTransaction_ZeroAmount_ThrowsException() {
        transactionRequest.setAmount(BigDecimal.ZERO);

        assertThrows(BadRequestException.class, 
                () -> transactionService.createTransaction(transactionRequest, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for negative amount")
    void createTransaction_NegativeAmount_ThrowsException() {
        transactionRequest.setAmount(new BigDecimal("-100.00"));

        assertThrows(BadRequestException.class, 
                () -> transactionService.createTransaction(transactionRequest, user));
    }

    @Test
    @DisplayName("Should get all transactions for user")
    void getTransactions_Success() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserWithFilters(user, null, null, null))
                .thenReturn(transactions);

        TransactionListResponse response = transactionService.getTransactions(user, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void updateTransaction_Success() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest();
        updateRequest.setAmount(new BigDecimal("6000.00"));
        updateRequest.setDescription("Updated description");

        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(1L, updateRequest, user);

        assertNotNull(response);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent transaction")
    void updateTransaction_NotFound_ThrowsException() {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest();
        when(transactionRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.updateTransaction(999L, updateRequest, user));
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void deleteTransaction_Success() {
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, user));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent transaction")
    void deleteTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.deleteTransaction(999L, user));
    }

    @Test
    @DisplayName("Should check if category is in use")
    void isCategoryInUse_ReturnsTrue() {
        when(transactionRepository.existsByUserAndCategory(user, "Salary")).thenReturn(true);

        assertTrue(transactionService.isCategoryInUse(user, "Salary"));
    }

    @Test
    @DisplayName("Should get transactions for month")
    void getTransactionsForMonth_Success() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 1)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsForMonth(user, 2024, 1);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get transactions for year")
    void getTransactionsForYear_Success() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByUserAndYear(user, 2024)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsForYear(user, 2024);

        assertEquals(1, result.size());
    }
}
