package com.example.pfm.controller;

import com.example.pfm.dto.*;
import com.example.pfm.entity.User;
import com.example.pfm.service.AuthService;
import com.example.pfm.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for transaction operations.
 * Provides endpoints for creating, listing, updating, and deleting transactions.
 * 
 * @author PFM Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    /**
     * Creates a new transaction.
     * 
     * @param request the transaction details
     * @return TransactionResponse with created transaction (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves transactions with optional filtering.
     * 
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param category optional category filter
     * @return TransactionListResponse with matching transactions (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category) {
        User user = authService.getCurrentUser();
        TransactionListResponse response = transactionService.getTransactions(user, startDate, endDate, category);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing transaction.
     * 
     * @param id the transaction ID
     * @param request the update details
     * @return TransactionResponse with updated transaction (HTTP 200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionUpdateRequest request) {
        User user = authService.getCurrentUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction.
     * 
     * @param id the transaction ID to delete
     * @return MessageResponse confirming deletion (HTTP 200)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        transactionService.deleteTransaction(id, user);
        return ResponseEntity.ok(new MessageResponse("Transaction deleted successfully"));
    }
}
