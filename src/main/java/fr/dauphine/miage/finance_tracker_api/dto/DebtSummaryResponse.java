package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;

public record DebtSummaryResponse(
        int totalActiveDebts,
        int totalPaidDebts,
        BigDecimal totalRemainingAmount,
        BigDecimal totalMonthlyPayment
) {}
