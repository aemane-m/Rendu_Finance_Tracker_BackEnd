package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetAlertResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DashboardSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalProgressResponse;
import fr.dauphine.miage.finance_tracker_api.dto.SpendingByCategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpcomingEventResponse;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface DashboardService {
    DashboardSummaryResponse getSummary(UUID userId, YearMonth month);
    List<SpendingByCategoryResponse> getSpendingByCategory(UUID userId, YearMonth month);
    List<UpcomingEventResponse> getUpcoming(UUID userId, int limit);
    List<GoalProgressResponse> getGoalsProgress(UUID userId);
    List<BudgetAlertResponse> getBudgetAlerts(UUID userId, YearMonth month);
}
