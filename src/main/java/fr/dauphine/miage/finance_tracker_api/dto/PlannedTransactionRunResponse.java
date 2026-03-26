package fr.dauphine.miage.finance_tracker_api.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PlannedTransactionRunResponse(
        Long id,
        Long plannedTransactionId,
        LocalDate dueDate,
        Instant executedAt,
        String status,
        Long createdTransactionId,
        String errorMessage
) {}
