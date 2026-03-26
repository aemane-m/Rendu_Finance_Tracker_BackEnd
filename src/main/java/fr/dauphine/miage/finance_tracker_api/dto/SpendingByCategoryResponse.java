package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;

public record SpendingByCategoryResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalSpent,
        BigDecimal budgetLimit,
        BigDecimal usagePercent
) {}
