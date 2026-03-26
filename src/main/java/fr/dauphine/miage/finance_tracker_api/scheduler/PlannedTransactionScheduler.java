package fr.dauphine.miage.finance_tracker_api.scheduler;

import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily scheduler that auto-executes active planned transactions that are due.
 *
 * Only processes transactions where:
 *   - isActive = true
 *   - autoCreate = true
 *   - nextDueDate <= today
 *
 * Each item is executed in its own transaction (via PlannedTransactionExecutor)
 * so that a failure on one item does not affect the others.
 *
 * Cron can be overridden via application.yaml:
 *   scheduler.planned-transactions.cron: "0 0 6 * * *"
 */
@Component
public class PlannedTransactionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PlannedTransactionScheduler.class);

    private final PlannedTransactionRepository plannedTransactionRepository;
    private final PlannedTransactionExecutor executor;

    public PlannedTransactionScheduler(PlannedTransactionRepository plannedTransactionRepository,
                                       PlannedTransactionExecutor executor) {
        this.plannedTransactionRepository = plannedTransactionRepository;
        this.executor = executor;
    }

    @Scheduled(cron = "${scheduler.planned-transactions.cron:0 0 6 * * *}")
    public void processDuePlannedTransactions() {
        LocalDate today = LocalDate.now();

        List<PlannedTransaction> due = plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(today);

        if (due.isEmpty()) {
            log.debug("Scheduler: no planned transactions due today ({})", today);
            return;
        }

        log.info("Scheduler: {} planned transaction(s) to execute for {}", due.size(), today);

        int success = 0;
        int failure = 0;

        for (PlannedTransaction pt : due) {
            try {
                executor.execute(pt);
                success++;
            } catch (Exception e) {
                log.error("Scheduler: failed to execute planned transaction #{} '{}': {}",
                        pt.getId(), pt.getLabel(), e.getMessage());
                executor.recordFailure(pt, e.getMessage());
                failure++;
            }
        }

        log.info("Scheduler: completed — {} success, {} failure(s)", success, failure);
    }
}
