package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.CreationTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.TransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateTransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionService {
    Page<TransactionResponse> findAll(UUID userId, Pageable pageable);
    Page<TransactionResponse> search(UUID userId, String type, Long categoryId, LocalDate startDate, LocalDate endDate, String label, Pageable pageable);
    TransactionResponse findById(Long id, UUID userId);
    TransactionResponse create(CreationTransactionRequest request, UUID userId);
    TransactionResponse update(Long id, UpdateTransactionRequest request, UUID userId);
    void delete(Long id, UUID userId);
}
