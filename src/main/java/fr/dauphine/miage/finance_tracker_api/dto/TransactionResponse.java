package fr.dauphine.miage.finance_tracker_api.dto;

import fr.dauphine.miage.finance_tracker_api.models.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        Long categoryId,
        String categoryName,
        String label,
        LocalDate date,
        String note,
        String source,
        Instant createdAt,
        Instant updatedAt
) {}
