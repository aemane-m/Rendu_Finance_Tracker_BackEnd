package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserId(UUID userId);
    Optional<Goal> findByIdAndUserId(Long id, UUID userId);
}
