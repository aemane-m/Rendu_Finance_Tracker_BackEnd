package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CalendarEventResponse(
        Long id,
        String title,
        LocalDate eventDate,
        BigDecimal amount,
        String color,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
