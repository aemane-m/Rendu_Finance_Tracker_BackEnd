package fr.dauphine.miage.finance_tracker_api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String newPassword) {}
