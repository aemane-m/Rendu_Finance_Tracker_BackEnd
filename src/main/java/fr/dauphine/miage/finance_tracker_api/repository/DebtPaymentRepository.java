package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.DebtPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {
    Page<DebtPayment> findByDebtId(Long debtId, Pageable pageable);
}
