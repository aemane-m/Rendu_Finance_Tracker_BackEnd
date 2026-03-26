package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalResponse;
import fr.dauphine.miage.finance_tracker_api.models.Goal;
import fr.dauphine.miage.finance_tracker_api.models.GoalContribution;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getStatus().name(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    public GoalContributionResponse toContributionResponse(GoalContribution contribution) {
        return new GoalContributionResponse(
                contribution.getId(),
                contribution.getGoal().getId(),
                contribution.getAmount(),
                contribution.getContributionDate(),
                contribution.getNote(),
                contribution.getTransaction() != null ? contribution.getTransaction().getId() : null,
                contribution.getCreatedAt()
        );
    }
}
