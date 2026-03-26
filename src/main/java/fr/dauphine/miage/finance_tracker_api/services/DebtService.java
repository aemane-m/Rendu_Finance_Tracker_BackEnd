package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationDebtRequest;
import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateDebtRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DebtService {
    List<DebtResponse> findAll(UUID userId);
    DebtResponse findById(Long id, UUID userId);
    DebtResponse create(CreationDebtRequest request, UUID userId);
    DebtResponse update(Long id, UpdateDebtRequest request, UUID userId);
    DebtResponse registerPayment(Long id, DebtPaymentRequest request, UUID userId);
    Page<DebtPaymentResponse> findPayments(Long debtId, UUID userId, Pageable pageable);
    DebtSummaryResponse summary(UUID userId);
    void delete(Long id, UUID userId);
}
