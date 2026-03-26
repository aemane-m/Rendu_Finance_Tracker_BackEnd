package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationBudgetRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateBudgetRequest;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    List<BudgetResponse> findAll(UUID userId);
    BudgetResponse findById(Long id, UUID userId);
    BudgetResponse create(CreationBudgetRequest request, UUID userId);
    BudgetResponse update(Long id, UpdateBudgetRequest request, UUID userId);
    void delete(Long id, UUID userId);
}
