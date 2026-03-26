package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.CreationGoalRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateGoalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    List<GoalResponse> findAll(UUID userId);
    GoalResponse findById(Long id, UUID userId);
    GoalResponse create(CreationGoalRequest request, UUID userId);
    GoalResponse update(Long id, UpdateGoalRequest request, UUID userId);
    GoalResponse contribute(Long id, GoalContributionRequest request, UUID userId);
    Page<GoalContributionResponse> findContributions(Long goalId, UUID userId, Pageable pageable);
    void delete(Long id, UUID userId);
}
