package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DebtResponse(
        Long id,
        String creditorName,
        BigDecimal totalAmount,
        BigDecimal remainingAmount,
        BigDecimal monthlyPayment,
        BigDecimal interestRate,
        LocalDate startDate,
        LocalDate dueDate,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
