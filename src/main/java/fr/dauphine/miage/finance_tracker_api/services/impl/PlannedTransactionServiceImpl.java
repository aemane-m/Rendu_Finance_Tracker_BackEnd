package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CreationPlannedTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionRunResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdatePlannedTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.PlannedTransactionMapper;
import fr.dauphine.miage.finance_tracker_api.models.Category;
import fr.dauphine.miage.finance_tracker_api.models.PlannedFrequency;
import fr.dauphine.miage.finance_tracker_api.models.PlannedRunStatus;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransactionRun;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.repository.CategoryRepository;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRepository;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRunRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import fr.dauphine.miage.finance_tracker_api.services.PlannedTransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlannedTransactionServiceImpl implements PlannedTransactionService {

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final PlannedTransactionRunRepository plannedTransactionRunRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PlannedTransactionMapper plannedTransactionMapper;

    public PlannedTransactionServiceImpl(PlannedTransactionRepository plannedTransactionRepository,
                                          PlannedTransactionRunRepository plannedTransactionRunRepository,
                                          TransactionRepository transactionRepository,
                                          CategoryRepository categoryRepository,
                                          PlannedTransactionMapper plannedTransactionMapper) {
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.plannedTransactionRunRepository = plannedTransactionRunRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.plannedTransactionMapper = plannedTransactionMapper;
    }

    @Override
    public List<PlannedTransactionResponse> findAll(UUID userId) {
        return plannedTransactionRepository.findByUserId(userId)
                .stream()
                .map(plannedTransactionMapper::toResponse)
                .toList();
    }

    @Override
    public PlannedTransactionResponse findById(Long id, UUID userId) {
        PlannedTransaction pt = plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));
        return plannedTransactionMapper.toResponse(pt);
    }

    @Override
    @Transactional
    public PlannedTransactionResponse create(CreationPlannedTransactionRequest request, UUID userId) {
        Category category = resolveCategory(request.categoryId(), userId);

        Instant now = Instant.now();
        PlannedTransaction pt = PlannedTransaction.builder()
                .userId(userId)
                .label(request.label().trim())
                .amount(request.amount())
                .type(request.type())
                .category(category)
                .frequency(request.frequency())
                .nextDueDate(request.nextDueDate())
                .autoCreate(request.autoCreate())
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return plannedTransactionMapper.toResponse(plannedTransactionRepository.save(pt));
    }

    @Override
    @Transactional
    public PlannedTransactionResponse update(Long id, UpdatePlannedTransactionRequest request, UUID userId) {
        PlannedTransaction pt = plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));

        Category category = resolveCategory(request.categoryId(), userId);

        pt.setLabel(request.label().trim());
        pt.setAmount(request.amount());
        pt.setType(request.type());
        pt.setCategory(category);
        pt.setFrequency(request.frequency());
        pt.setNextDueDate(request.nextDueDate());
        pt.setAutoCreate(request.autoCreate());
        pt.setUpdatedAt(Instant.now());

        return plannedTransactionMapper.toResponse(plannedTransactionRepository.save(pt));
    }

    @Override
    @Transactional
    public PlannedTransactionResponse toggle(Long id, UUID userId) {
        PlannedTransaction pt = plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));

        pt.setActive(!pt.isActive());
        pt.setUpdatedAt(Instant.now());

        return plannedTransactionMapper.toResponse(plannedTransactionRepository.save(pt));
    }

    @Override
    @Transactional
    public PlannedTransactionRunResponse runNow(Long id, UUID userId) {
        PlannedTransaction pt = plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));

        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(pt.getAmount())
                .type(pt.getType())
                .category(pt.getCategory())
                .label(pt.getLabel())
                .date(pt.getNextDueDate())
                .note("Auto-generated from planned transaction #" + pt.getId())
                .source("PLANNED")
                .createdAt(now)
                .updatedAt(now)
                .build();
        transaction = transactionRepository.save(transaction);

        PlannedTransactionRun run = PlannedTransactionRun.builder()
                .plannedTransaction(pt)
                .userId(userId)
                .dueDate(pt.getNextDueDate())
                .executedAt(now)
                .status(PlannedRunStatus.SUCCESS)
                .createdTransaction(transaction)
                .build();
        run = plannedTransactionRunRepository.save(run);

        pt.setNextDueDate(computeNextDueDate(pt));
        pt.setUpdatedAt(now);
        plannedTransactionRepository.save(pt);

        return plannedTransactionMapper.toRunResponse(run);
    }

    @Override
    public Page<PlannedTransactionRunResponse> findRuns(Long id, UUID userId, Pageable pageable) {
        plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));

        return plannedTransactionRunRepository.findByPlannedTransactionId(id, pageable)
                .map(plannedTransactionMapper::toRunResponse);
    }

    @Override
    @Transactional
    public void delete(Long id, UUID userId) {
        PlannedTransaction pt = plannedTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned transaction not found: " + id));
        plannedTransactionRepository.delete(pt);
    }

    private Category resolveCategory(Long categoryId, UUID userId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findVisibleByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    private java.time.LocalDate computeNextDueDate(PlannedTransaction pt) {
        return switch (pt.getFrequency()) {
            case PlannedFrequency.DAILY -> pt.getNextDueDate().plusDays(1);
            case PlannedFrequency.WEEKLY -> pt.getNextDueDate().plusWeeks(1);
            case PlannedFrequency.MONTHLY -> pt.getNextDueDate().plusMonths(1);
            case PlannedFrequency.YEARLY -> pt.getNextDueDate().plusYears(1);
        };
    }
}
