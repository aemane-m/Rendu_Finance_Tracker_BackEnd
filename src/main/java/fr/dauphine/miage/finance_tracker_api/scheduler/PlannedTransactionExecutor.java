package fr.dauphine.miage.finance_tracker_api.scheduler;

import fr.dauphine.miage.finance_tracker_api.models.PlannedFrequency;
import fr.dauphine.miage.finance_tracker_api.models.PlannedRunStatus;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransactionRun;
import fr.dauphine.miage.finance_tracker_api.models.Transaction;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRepository;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRunRepository;
import fr.dauphine.miage.finance_tracker_api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Executes a single PlannedTransaction in its own database transaction.
 * Separated from the scheduler so that a failure on one item does not roll back the others.
 */
@Service
public class PlannedTransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(PlannedTransactionExecutor.class);

    private final TransactionRepository transactionRepository;
    private final PlannedTransactionRepository plannedTransactionRepository;
    private final PlannedTransactionRunRepository plannedTransactionRunRepository;

    public PlannedTransactionExecutor(TransactionRepository transactionRepository,
                                      PlannedTransactionRepository plannedTransactionRepository,
                                      PlannedTransactionRunRepository plannedTransactionRunRepository) {
        this.transactionRepository = transactionRepository;
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.plannedTransactionRunRepository = plannedTransactionRunRepository;
    }

    @Transactional
    public void execute(PlannedTransaction pt) {
        Instant now = Instant.now();

        Transaction transaction = Transaction.builder()
                .userId(pt.getUserId())
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
                .userId(pt.getUserId())
                .dueDate(pt.getNextDueDate())
                .executedAt(now)
                .status(PlannedRunStatus.SUCCESS)
                .createdTransaction(transaction)
                .build();
        plannedTransactionRunRepository.save(run);

        pt.setNextDueDate(nextDueDate(pt));
        pt.setUpdatedAt(now);
        plannedTransactionRepository.save(pt);

        log.info("Scheduler: executed planned transaction #{} '{}' (user={})",
                pt.getId(), pt.getLabel(), pt.getUserId());
    }

    @Transactional
    public void recordFailure(PlannedTransaction pt, String errorMessage) {
        try {
            Instant now = Instant.now();
            PlannedTransactionRun failed = PlannedTransactionRun.builder()
                    .plannedTransaction(pt)
                    .userId(pt.getUserId())
                    .dueDate(pt.getNextDueDate())
                    .executedAt(now)
                    .status(PlannedRunStatus.FAILED)
                    .errorMessage(errorMessage)
                    .build();
            plannedTransactionRunRepository.save(failed);

            pt.setNextDueDate(nextDueDate(pt));
            pt.setUpdatedAt(now);
            plannedTransactionRepository.save(pt);
        } catch (Exception e) {
            log.error("Scheduler: could not record failure for planned transaction #{}: {}",
                    pt.getId(), e.getMessage());
        }
    }

    private LocalDate nextDueDate(PlannedTransaction pt) {
        return switch (pt.getFrequency()) {
            case PlannedFrequency.DAILY   -> pt.getNextDueDate().plusDays(1);
            case PlannedFrequency.WEEKLY  -> pt.getNextDueDate().plusWeeks(1);
            case PlannedFrequency.MONTHLY -> pt.getNextDueDate().plusMonths(1);
            case PlannedFrequency.YEARLY  -> pt.getNextDueDate().plusYears(1);
        };
    }
}
