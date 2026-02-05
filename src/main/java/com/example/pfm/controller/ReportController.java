package com.example.pfm.controller;

import com.example.pfm.dto.MonthlyReportResponse;
import com.example.pfm.dto.YearlyReportResponse;
import com.example.pfm.entity.User;
import com.example.pfm.service.AuthService;
import com.example.pfm.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for report operations.
 * Provides endpoints for generating monthly and yearly financial reports.
 * 
 * @author PFM Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    /**
     * Generates a monthly financial report.
     * 
     * @param year the year for the report
     * @param month the month for the report (1-12)
     * @return MonthlyReportResponse with income/expense breakdown (HTTP 200)
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        User user = authService.getCurrentUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(year, month, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a yearly financial report.
     * 
     * @param year the year for the report
     * @return YearlyReportResponse with income/expense breakdown (HTTP 200)
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        User user = authService.getCurrentUser();
        YearlyReportResponse response = reportService.getYearlyReport(year, user);
        return ResponseEntity.ok(response);
    }
}
