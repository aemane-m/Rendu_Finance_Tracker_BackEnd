package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.PlannedTransactionRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannedTransactionRunRepository extends JpaRepository<PlannedTransactionRun, Long> {
    Page<PlannedTransactionRun> findByPlannedTransactionId(Long plannedTransactionId, Pageable pageable);
}
