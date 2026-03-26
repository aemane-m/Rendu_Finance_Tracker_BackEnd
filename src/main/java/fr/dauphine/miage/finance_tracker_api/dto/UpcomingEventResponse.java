package fr.dauphine.miage.finance_tracker_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpcomingEventResponse(
        Long id,
        String title,
        LocalDate eventDate,
        BigDecimal amount,
        String color
) {}
