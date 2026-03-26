package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CreationTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.TransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.TransactionMapper;
import fr.dauphine.miage.finance_tracker_api.models.Category;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.models.TransactionType;
import fr.dauphine.miage.finance_tracker_api.repository.CategoryRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import fr.dauphine.miage.finance_tracker_api.services.TransactionService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                   CategoryRepository categoryRepository,
                                   TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Page<TransactionResponse> findAll(UUID userId, Pageable pageable) {
        Specification<Transaction> spec = (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
        return transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
    }

    @Override
    public Page<TransactionResponse> search(UUID userId, String type, Long categoryId,
                                            LocalDate startDate, LocalDate endDate,
                                            String label, Pageable pageable) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (type != null && !type.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("type"), TransactionType.valueOf(type.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid transaction type: " + type + ". Expected INCOME or EXPENSE");
                }
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (label != null && !label.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("label")), "%" + label.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
    }

    @Override
    public TransactionResponse findById(Long id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse create(CreationTransactionRequest request, UUID userId) {
        Category category = resolveCategory(request.categoryId(), userId);
        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.amount())
                .type(request.type())
                .category(category)
                .label(request.label())
                .date(request.date())
                .note(request.note())
                .source("MANUAL")
                .createdAt(now)
                .updatedAt(now)
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, UpdateTransactionRequest request, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        Category category = resolveCategory(request.categoryId(), userId);

        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setLabel(request.label());
        transaction.setDate(request.date());
        transaction.setNote(request.note());
        transaction.setUpdatedAt(Instant.now());

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void delete(Long id, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        transactionRepository.delete(transaction);
    }

    private Category resolveCategory(Long categoryId, UUID userId) {
        if (categoryId == null) return null;
        return categoryRepository.findVisibleByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }
}
