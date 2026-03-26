package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDebtRequest(
        @NotBlank String creditorName,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
        @NotNull @DecimalMin("0.00") BigDecimal monthlyPayment,
        BigDecimal interestRate,
        @NotNull LocalDate startDate,
        LocalDate dueDate
) {}
