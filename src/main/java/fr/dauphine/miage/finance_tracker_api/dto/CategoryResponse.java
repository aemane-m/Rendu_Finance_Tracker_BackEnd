package fr.dauphine.miage.finance_tracker_api.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String icon,
        String color,
        boolean isDefault,
        Instant createdAt
) {}
