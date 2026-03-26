package fr.dauphine.miage.finance_tracker_api.dto;

import fr.dauphine.miage.finance_tracker_api.models.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreationTransactionRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull TransactionType type,
        Long categoryId,
        @NotBlank String label,
        @NotNull LocalDate date,
        String note
) {}
