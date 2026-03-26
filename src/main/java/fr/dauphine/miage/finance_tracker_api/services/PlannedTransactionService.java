package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.CreationPlannedTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionRunResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdatePlannedTransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PlannedTransactionService {
    List<PlannedTransactionResponse> findAll(UUID userId);
    PlannedTransactionResponse findById(Long id, UUID userId);
    PlannedTransactionResponse create(CreationPlannedTransactionRequest request, UUID userId);
    PlannedTransactionResponse update(Long id, UpdatePlannedTransactionRequest request, UUID userId);
    PlannedTransactionResponse toggle(Long id, UUID userId);
    PlannedTransactionRunResponse runNow(Long id, UUID userId);
    Page<PlannedTransactionRunResponse> findRuns(Long id, UUID userId, Pageable pageable);
    void delete(Long id, UUID userId);
}
