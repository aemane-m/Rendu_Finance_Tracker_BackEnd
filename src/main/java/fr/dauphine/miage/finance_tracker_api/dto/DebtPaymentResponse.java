package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DebtPaymentResponse(
        Long id,
        Long debtId,
        LocalDate paymentDate,
        BigDecimal amount,
        String note,
        Long transactionId,
        Instant createdAt
) {}
