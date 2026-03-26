package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record GoalContributionResponse(
        Long id,
        Long goalId,
        BigDecimal amount,
        LocalDate contributionDate,
        String note,
        Long transactionId,
        Instant createdAt
) {}
