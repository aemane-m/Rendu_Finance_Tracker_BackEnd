package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationBudgetRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateBudgetRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.ConflictException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.BudgetMapper;
import fr.dauphine.miage.finance_tracker_api.models.Budget;
import fr.dauphine.miage.finance_tracker_api.models.Category;
import fr.dauphine.miage.finance_tracker_api.repository.BudgetRepository;
import fr.dauphine.miage.finance_tracker_api.repository.CategoryRepository;
import fr.dauphine.miage.finance_tracker_api.services.BudgetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                              CategoryRepository categoryRepository,
                              BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.budgetMapper = budgetMapper;
    }

    @Override
    public List<BudgetResponse> findAll(UUID userId) {
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(budgetMapper::toResponse)
                .toList();
    }

    @Override
    public BudgetResponse findById(Long id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        return budgetMapper.toResponse(budget);
    }

    @Override
    public BudgetResponse create(CreationBudgetRequest request, UUID userId) {
        Category category = categoryRepository.findVisibleByIdAndUserId(request.categoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        if (budgetRepository.existsByUserIdAndCategoryId(userId, request.categoryId())) {
            throw new ConflictException("A budget already exists for this category");
        }

        Instant now = Instant.now();
        Budget budget = Budget.builder()
                .userId(userId)
                .category(category)
                .monthlyLimit(request.monthlyLimit())
                .alertThreshold(request.alertThreshold())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return budgetMapper.toResponse(budgetRepository.save(budget));
    }

    @Override
    public BudgetResponse update(Long id, UpdateBudgetRequest request, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));

        boolean categoryChanged = !budget.getCategory().getId().equals(request.categoryId());
        if (categoryChanged) {
            Category category = categoryRepository.findVisibleByIdAndUserId(request.categoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

            if (budgetRepository.existsByUserIdAndCategoryId(userId, request.categoryId())) {
                throw new ConflictException("A budget already exists for this category");
            }

            budget.setCategory(category);
        }

        budget.setMonthlyLimit(request.monthlyLimit());
        budget.setAlertThreshold(request.alertThreshold());
        budget.setUpdatedAt(Instant.now());

        return budgetMapper.toResponse(budgetRepository.save(budget));
    }

    @Override
    public void delete(Long id, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        budgetRepository.delete(budget);
    }
}
