package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PlannedTransactionResponse(
        Long id,
        String label,
        BigDecimal amount,
        String type,
        Long categoryId,
        String categoryName,
        String frequency,
        LocalDate nextDueDate,
        boolean autoCreate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
