package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreationBudgetRequest(
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.01") BigDecimal monthlyLimit,
        @NotNull @DecimalMin("0.00") BigDecimal alertThreshold
) {}
