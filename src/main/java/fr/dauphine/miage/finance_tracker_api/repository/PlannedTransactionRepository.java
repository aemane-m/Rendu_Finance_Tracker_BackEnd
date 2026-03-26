package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlannedTransactionRepository extends JpaRepository<PlannedTransaction, Long> {

    @Query("SELECT pt FROM PlannedTransaction pt LEFT JOIN FETCH pt.category WHERE pt.userId = :userId")
    List<PlannedTransaction> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT pt FROM PlannedTransaction pt LEFT JOIN FETCH pt.category WHERE pt.id = :id AND pt.userId = :userId")
    Optional<PlannedTransaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);

    @Query("SELECT pt FROM PlannedTransaction pt LEFT JOIN FETCH pt.category WHERE pt.isActive = true AND pt.autoCreate = true AND pt.nextDueDate <= :date")
    List<PlannedTransaction> findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(@Param("date") LocalDate date);
}
