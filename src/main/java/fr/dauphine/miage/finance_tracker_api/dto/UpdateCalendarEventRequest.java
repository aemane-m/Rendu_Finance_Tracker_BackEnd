package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCalendarEventRequest(
        @NotBlank String title,
        @NotNull LocalDate eventDate,
        BigDecimal amount,
        String color,
        String notes
) {}
