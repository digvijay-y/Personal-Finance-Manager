package com.example.pfm.service;

import com.example.pfm.dto.MonthlyReportResponse;
import com.example.pfm.dto.YearlyReportResponse;
import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for generating financial reports.
 * Provides monthly and yearly summaries of income, expenses, and net savings.
 * 
 * @author PFM Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionService transactionService;

    /**
     * Generates a monthly financial report for a user.
     * Aggregates income and expenses by category for the specified month.
     * 
     * @param year the year for the report
     * @param month the month for the report (1-12)
     * @param user the user whose report to generate
     * @return MonthlyReportResponse containing income/expense breakdown and net savings
     * @throws BadRequestException if month is not between 1 and 12
     */
    public MonthlyReportResponse getMonthlyReport(int year, int month, User user) {
        // Validate month
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month. Month must be between 1 and 12");
        }

        List<Transaction> transactions = transactionService.getTransactionsForMonth(user, year, month);
        
        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                incomeSum = incomeSum.add(t.getAmount());
            } else {
                totalExpenses.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                expenseSum = expenseSum.add(t.getAmount());
            }
        }

        BigDecimal netSavings = incomeSum.subtract(expenseSum);

        return new MonthlyReportResponse(month, year, totalIncome, totalExpenses, netSavings);
    }

    /**
     * Generates a yearly financial report for a user.
     * Aggregates income and expenses by category for the entire year.
     * 
     * @param year the year for the report
     * @param user the user whose report to generate
     * @return YearlyReportResponse containing income/expense breakdown and net savings
     */
    public YearlyReportResponse getYearlyReport(int year, User user) {
        List<Transaction> transactions = transactionService.getTransactionsForYear(user, year);
        
        Map<String, BigDecimal> totalIncome = new HashMap<>();
        Map<String, BigDecimal> totalExpenses = new HashMap<>();
        BigDecimal incomeSum = BigDecimal.ZERO;
        BigDecimal expenseSum = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                incomeSum = incomeSum.add(t.getAmount());
            } else {
                totalExpenses.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
                expenseSum = expenseSum.add(t.getAmount());
            }
        }

        BigDecimal netSavings = incomeSum.subtract(expenseSum);

        return new YearlyReportResponse(year, totalIncome, totalExpenses, netSavings);
    }
}
