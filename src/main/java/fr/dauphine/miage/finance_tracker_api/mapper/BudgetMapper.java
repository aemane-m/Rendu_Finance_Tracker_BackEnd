package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetResponse;
import fr.dauphine.miage.finance_tracker_api.models.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonthlyLimit(),
                budget.getAlertThreshold(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
