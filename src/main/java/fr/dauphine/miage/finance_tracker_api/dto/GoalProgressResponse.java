package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalProgressResponse(
        Long id,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal percentage,
        LocalDate deadline,
        String status
) {}
