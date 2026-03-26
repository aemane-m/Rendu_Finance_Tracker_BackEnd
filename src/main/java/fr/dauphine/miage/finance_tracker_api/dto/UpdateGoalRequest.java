package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateGoalRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.01") BigDecimal targetAmount,
        LocalDate deadline
) {}
