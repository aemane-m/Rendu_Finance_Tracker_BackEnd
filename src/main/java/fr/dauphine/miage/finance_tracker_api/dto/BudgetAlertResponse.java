package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;

public record BudgetAlertResponse(
        Long budgetId,
        Long categoryId,
        String categoryName,
        BigDecimal monthlyLimit,
        BigDecimal alertThreshold,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal overageAmount,
        BigDecimal percentage,
        boolean overBudget
) {}
