package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtPaymentRequest(
        @NotNull LocalDate paymentDate,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String note,
        Long transactionId
) {}
