package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b JOIN FETCH b.category WHERE b.userId = :userId")
    List<Budget> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT b FROM Budget b JOIN FETCH b.category WHERE b.id = :id AND b.userId = :userId")
    Optional<Budget> findByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);

    boolean existsByUserIdAndCategoryId(UUID userId, Long categoryId);
}
