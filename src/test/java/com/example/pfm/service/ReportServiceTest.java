package com.example.pfm.service;

import com.example.pfm.dto.MonthlyReportResponse;
import com.example.pfm.dto.YearlyReportResponse;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        incomeTransaction = new Transaction();
        incomeTransaction.setId(1L);
        incomeTransaction.setAmount(new BigDecimal("5000.00"));
        incomeTransaction.setDate(LocalDate.of(2024, 1, 15));
        incomeTransaction.setCategory("Salary");
        incomeTransaction.setType(TransactionType.INCOME);
        incomeTransaction.setUser(user);

        expenseTransaction = new Transaction();
        expenseTransaction.setId(2L);
        expenseTransaction.setAmount(new BigDecimal("1200.00"));
        expenseTransaction.setDate(LocalDate.of(2024, 1, 16));
        expenseTransaction.setCategory("Rent");
        expenseTransaction.setType(TransactionType.EXPENSE);
        expenseTransaction.setUser(user);
    }

    @Test
    @DisplayName("Should generate monthly report successfully")
    void getMonthlyReport_Success() {
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction);
        when(transactionService.getTransactionsForMonth(user, 2024, 1)).thenReturn(transactions);

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 1, user);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("1200.00"), response.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("3800.00"), response.getNetSavings());
    }

    @Test
    @DisplayName("Should return empty report for month with no data")
    void getMonthlyReport_NoData() {
        when(transactionService.getTransactionsForMonth(user, 2024, 12)).thenReturn(Collections.emptyList());

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 12, user);

        assertNotNull(response);
        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid month > 12")
    void getMonthlyReport_InvalidMonth_ThrowsException() {
        assertThrows(BadRequestException.class, 
                () -> reportService.getMonthlyReport(2024, 13, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for month 0")
    void getMonthlyReport_ZeroMonth_ThrowsException() {
        assertThrows(BadRequestException.class, 
                () -> reportService.getMonthlyReport(2024, 0, user));
    }

    @Test
    @DisplayName("Should throw BadRequestException for negative month")
    void getMonthlyReport_NegativeMonth_ThrowsException() {
        assertThrows(BadRequestException.class, 
                () -> reportService.getMonthlyReport(2024, -1, user));
    }

    @Test
    @DisplayName("Should generate yearly report successfully")
    void getYearlyReport_Success() {
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction);
        when(transactionService.getTransactionsForYear(user, 2024)).thenReturn(transactions);

        YearlyReportResponse response = reportService.getYearlyReport(2024, user);

        assertNotNull(response);
        assertEquals(2024, response.getYear());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("1200.00"), response.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("3800.00"), response.getNetSavings());
    }

    @Test
    @DisplayName("Should return empty yearly report for year with no data")
    void getYearlyReport_NoData() {
        when(transactionService.getTransactionsForYear(user, 2023)).thenReturn(Collections.emptyList());

        YearlyReportResponse response = reportService.getYearlyReport(2023, user);

        assertNotNull(response);
        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    @DisplayName("Should aggregate multiple transactions of same category")
    void getMonthlyReport_AggregatesCategories() {
        Transaction secondIncome = new Transaction();
        secondIncome.setAmount(new BigDecimal("500.00"));
        secondIncome.setCategory("Salary");
        secondIncome.setType(TransactionType.INCOME);

        List<Transaction> transactions = Arrays.asList(incomeTransaction, secondIncome, expenseTransaction);
        when(transactionService.getTransactionsForMonth(user, 2024, 1)).thenReturn(transactions);

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 1, user);

        assertEquals(new BigDecimal("5500.00"), response.getTotalIncome().get("Salary"));
    }
}
