package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetAlertResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DashboardSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalProgressResponse;
import fr.dauphine.miage.finance_tracker_api.dto.SpendingByCategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpcomingEventResponse;
import fr.dauphine.miage.finance_tracker_api.models.Budget;
import fr.dauphine.miage.finance_tracker_api.models.Goal;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.models.TransactionType;
import fr.dauphine.miage.finance_tracker_api.repository.BudgetRepository;
import fr.dauphine.miage.finance_tracker_api.repository.CalendarEventRepository;
import fr.dauphine.miage.finance_tracker_api.repository.GoalRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import fr.dauphine.miage.finance_tracker_api.services.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;

    public DashboardServiceImpl(TransactionRepository transactionRepository,
                                 CalendarEventRepository calendarEventRepository,
                                 BudgetRepository budgetRepository,
                                 GoalRepository goalRepository) {
        this.transactionRepository = transactionRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
    }

    @Override
    public DashboardSummaryResponse getSummary(UUID userId, YearMonth month) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                userId, month.atDay(1), month.atEndOfMonth());

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new DashboardSummaryResponse(
                month,
                totalIncome,
                totalExpenses,
                netBalance,
                transactions.size(),
                savingsRate
        );
    }

    @Override
    public List<SpendingByCategoryResponse> getSpendingByCategory(UUID userId, YearMonth month) {
        List<Transaction> expenses = transactionRepository.findByUserIdAndDateBetween(
                        userId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
                .toList();

        Map<Long, BigDecimal> spentByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<Long, Budget> budgetByCategory = budgetRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(b -> b.getCategory().getId(), b -> b));

        return expenses.stream()
                .map(t -> t.getCategory().getId())
                .distinct()
                .map(categoryId -> {
                    Transaction sample = expenses.stream()
                            .filter(t -> t.getCategory().getId().equals(categoryId))
                            .findFirst()
                            .orElseThrow();
                    BigDecimal spent = spentByCategory.get(categoryId);
                    Budget budget = budgetByCategory.get(categoryId);
                    BigDecimal budgetLimit = budget != null ? budget.getMonthlyLimit() : null;
                    BigDecimal usagePercent = null;
                    if (budget != null && budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
                        usagePercent = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                    return new SpendingByCategoryResponse(
                            categoryId,
                            sample.getCategory().getName(),
                            spent,
                            budgetLimit,
                            usagePercent
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UpcomingEventResponse> getUpcoming(UUID userId, int limit) {
        return calendarEventRepository
                .findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(userId, LocalDate.now())
                .stream()
                .limit(limit)
                .map(event -> new UpcomingEventResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getEventDate(),
                        event.getAmount(),
                        event.getColor()
                ))
                .toList();
    }

    @Override
    public List<GoalProgressResponse> getGoalsProgress(UUID userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(this::toGoalProgress)
                .toList();
    }

    @Override
    public List<BudgetAlertResponse> getBudgetAlerts(UUID userId, YearMonth month) {
        List<Transaction> expenses = transactionRepository.findByUserIdAndDateBetween(
                        userId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
                .toList();

        Map<Long, BigDecimal> spentByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        List<BudgetAlertResponse> alerts = new ArrayList<>();
        for (Budget budget : budgetRepository.findByUserId(userId)) {
            Long categoryId = budget.getCategory().getId();
            BigDecimal spent = spentByCategory.getOrDefault(categoryId, BigDecimal.ZERO);

            if (budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal usagePercent = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            if (usagePercent.compareTo(budget.getAlertThreshold()) >= 0) {
                 alerts.add(new BudgetAlertResponse(
                        budget.getId(),
                        categoryId,
                        budget.getCategory().getName(),
                        budget.getMonthlyLimit(),
                        budget.getAlertThreshold(),
                        spent,
                        budget.getMonthlyLimit().subtract(spent).max(BigDecimal.ZERO),
                        spent.subtract(budget.getMonthlyLimit()).max(BigDecimal.ZERO),
                        usagePercent,
                        spent.compareTo(budget.getMonthlyLimit()) > 0
                ));
            }
        }
        return alerts;
    }

    private GoalProgressResponse toGoalProgress(Goal goal) {
        BigDecimal percentage = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new GoalProgressResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                percentage,
                goal.getDeadline(),
                goal.getStatus().name()
        );
    }
}
