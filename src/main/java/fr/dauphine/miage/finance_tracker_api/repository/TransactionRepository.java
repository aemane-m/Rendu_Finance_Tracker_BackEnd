package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.id = :id AND t.userId = :userId")
    Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.userId = :userId AND t.date BETWEEN :start AND :end")
    List<Transaction> findByUserIdAndDateBetween(@Param("userId") UUID userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
