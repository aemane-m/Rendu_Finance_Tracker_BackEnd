package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
        @NotBlank String name,
        String icon,
        String color
) {}
