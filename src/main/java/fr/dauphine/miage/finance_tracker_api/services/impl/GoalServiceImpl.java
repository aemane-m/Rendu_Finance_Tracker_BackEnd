package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CreationGoalRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateGoalRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.GoalMapper;
import fr.dauphine.miage.finance_tracker_api.models.Goal;
import fr.dauphine.miage.finance_tracker_api.models.GoalContribution;
import fr.dauphine.miage.finance_tracker_api.models.GoalStatus;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.repository.GoalContributionRepository;
import fr.dauphine.miage.finance_tracker_api.repository.GoalRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import fr.dauphine.miage.finance_tracker_api.services.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final TransactionRepository transactionRepository;
    private final GoalMapper goalMapper;

    public GoalServiceImpl(GoalRepository goalRepository,
                            GoalContributionRepository goalContributionRepository,
                            TransactionRepository transactionRepository,
                            GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.transactionRepository = transactionRepository;
        this.goalMapper = goalMapper;
    }

    @Override
    public List<GoalResponse> findAll(UUID userId) {
        return goalRepository.findByUserId(userId)
                .stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public GoalResponse findById(Long id, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));
        return goalMapper.toResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse create(CreationGoalRequest request, UUID userId) {
        Instant now = Instant.now();
        Goal goal = Goal.builder()
                .userId(userId)
                .name(request.name().trim())
                .targetAmount(request.targetAmount())
                .currentAmount(java.math.BigDecimal.ZERO)
                .deadline(request.deadline())
                .status(GoalStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse update(Long id, UpdateGoalRequest request, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new BadRequestException("Only active goals can be updated");
        }

        goal.setName(request.name().trim());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());
        goal.setUpdatedAt(Instant.now());

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse contribute(Long id, GoalContributionRequest request, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new BadRequestException("Cannot contribute to a non-active goal");
        }

        Transaction transaction = null;
        if (request.transactionId() != null) {
            transaction = transactionRepository.findByIdAndUserId(request.transactionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + request.transactionId()));
        }

        GoalContribution contribution = GoalContribution.builder()
                .goal(goal)
                .userId(userId)
                .amount(request.amount())
                .contributionDate(request.contributionDate())
                .note(request.note())
                .transaction(transaction)
                .createdAt(Instant.now())
                .build();
        goalContributionRepository.save(contribution);

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.amount()));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        goal.setUpdatedAt(Instant.now());

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    public Page<GoalContributionResponse> findContributions(Long goalId, UUID userId, Pageable pageable) {
        goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));

        return goalContributionRepository.findByGoalId(goalId, pageable)
                .map(goalMapper::toContributionResponse);
    }

    @Override
    @Transactional
    public void delete(Long id, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));
        goalRepository.delete(goal);
    }
}
