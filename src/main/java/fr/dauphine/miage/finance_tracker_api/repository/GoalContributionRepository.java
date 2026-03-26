package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.GoalContribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    Page<GoalContribution> findByGoalId(Long goalId, Pageable pageable);
}
