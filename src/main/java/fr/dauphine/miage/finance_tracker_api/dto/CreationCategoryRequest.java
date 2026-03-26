package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreationCategoryRequest(
        @NotBlank String name,
        String icon,
        String color
) {}
