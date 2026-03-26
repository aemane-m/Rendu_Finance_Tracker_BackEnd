package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal monthlyLimit,
        BigDecimal alertThreshold,
        Instant createdAt,
        Instant updatedAt
) {}
