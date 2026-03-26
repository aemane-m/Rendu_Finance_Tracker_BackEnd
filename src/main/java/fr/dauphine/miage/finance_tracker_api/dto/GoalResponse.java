package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record GoalResponse(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        LocalDate deadline,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
