package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record DashboardSummaryResponse(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        int transactionCount,
        BigDecimal savingsRate
) {}
