package com.example.pfm.service;

import com.example.pfm.dto.*;
import com.example.pfm.entity.Category;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import com.example.pfm.exception.ResourceNotFoundException;
import com.example.pfm.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing financial transactions.
 * Handles creation, retrieval, update, and deletion of income and expense transactions.
 * 
 * @author PFM Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    /**
     * Creates a new financial transaction for a user.
     * 
     * @param request the transaction details including amount, date, category, and description
     * @param user the user creating the transaction
     * @return TransactionResponse containing the created transaction details
     * @throws BadRequestException if date is in the future, amount is non-positive, or category is invalid
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        // Validate date is not in the future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        // Validate amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        // Validate category exists
        Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);
        if (category == null) {
            throw new BadRequestException("Invalid category: " + request.getCategory());
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setType(category.getType());
        transaction.setUser(user);

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Retrieves transactions for a user with optional filtering.
     * 
     * @param user the user whose transactions to retrieve
     * @param startDate optional start date filter (inclusive)
     * @param endDate optional end date filter (inclusive)
     * @param category optional category filter
     * @return TransactionListResponse containing the filtered list of transactions
     */
    public TransactionListResponse getTransactions(User user, LocalDate startDate, LocalDate endDate, String category) {
        List<Transaction> transactions = transactionRepository.findByUserWithFilters(
                user, startDate, endDate, category);
        
        List<TransactionResponse> responses = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new TransactionListResponse(responses);
    }

    /**
     * Updates an existing transaction.
     * 
     * @param id the ID of the transaction to update
     * @param request the update request containing optional new values for amount, category, and description
     * @param user the user who owns the transaction
     * @return TransactionResponse containing the updated transaction details
     * @throws ResourceNotFoundException if the transaction is not found
     * @throws BadRequestException if the new amount is non-positive or category is invalid
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Update amount if provided
        if (request.getAmount() != null) {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Amount must be greater than zero");
            }
            transaction.setAmount(request.getAmount());
        }

        // Update category if provided
        if (request.getCategory() != null) {
            Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);
            if (category == null) {
                throw new BadRequestException("Invalid category: " + request.getCategory());
            }
            transaction.setCategory(request.getCategory());
            transaction.setType(category.getType());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        // Note: Date cannot be updated per requirements

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Deletes a transaction.
     * 
     * @param id the ID of the transaction to delete
     * @param user the user who owns the transaction
     * @throws ResourceNotFoundException if the transaction is not found
     */
    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
    }

    /**
     * Retrieves all transactions since a given date for goal progress calculation.
     * 
     * @param user the user whose transactions to retrieve
     * @param startDate the start date (inclusive)
     * @return list of transactions since the start date
     */
    public List<Transaction> getTransactionsSinceDate(User user, LocalDate startDate) {
        return transactionRepository.findByUserAndDateGreaterThanEqual(user, startDate);
    }

    /**
     * Retrieves all transactions for a specific month for monthly reports.
     * 
     * @param user the user whose transactions to retrieve
     * @param year the year
     * @param month the month (1-12)
     * @return list of transactions for the specified month
     */
    public List<Transaction> getTransactionsForMonth(User user, int year, int month) {
        return transactionRepository.findByUserAndYearAndMonth(user, year, month);
    }

    /**
     * Retrieves all transactions for a specific year for yearly reports.
     * 
     * @param user the user whose transactions to retrieve
     * @param year the year
     * @return list of transactions for the specified year
     */
    public List<Transaction> getTransactionsForYear(User user, int year) {
        return transactionRepository.findByUserAndYear(user, year);
    }

    public boolean isCategoryInUse(User user, String categoryName) {
        return transactionRepository.existsByUserAndCategory(user, categoryName);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getType()
        );
    }
}
