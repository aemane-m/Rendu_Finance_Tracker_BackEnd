package fr.dauphine.miage.finance_tracker_api.scheduler;

import fr.dauphine.miage.finance_tracker_api.models.PlannedFrequency;
import fr.dauphine.miage.finance_tracker_api.models.PlannedTransaction;
import fr.dauphine.miage.finance_tracker_api.models.TransactionType;
import fr.dauphine.miage.finance_tracker_api.repository.PlannedTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannedTransactionSchedulerTest {

    @Mock
    private PlannedTransactionRepository plannedTransactionRepository;

    @Mock
    private PlannedTransactionExecutor executor;

    @InjectMocks
    private PlannedTransactionScheduler scheduler;

    private PlannedTransaction mockPlannedTransaction(Long id, LocalDate nextDueDate) {
        return PlannedTransaction.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .label("Loyer #" + id)
                .amount(BigDecimal.valueOf(900))
                .type(TransactionType.EXPENSE)
                .frequency(PlannedFrequency.MONTHLY)
                .nextDueDate(nextDueDate)
                .autoCreate(true)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ---- Cas : aucune transaction à exécuter ----

    @Test
    void processDue_nothingDue_executorNeverCalled() {
        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of());

        scheduler.processDuePlannedTransactions();

        verifyNoInteractions(executor);
    }

    // ---- Cas : une transaction due, exécution réussie ----

    @Test
    void processDue_oneDue_executorCalledOnce() {
        PlannedTransaction pt = mockPlannedTransaction(1L, LocalDate.now().minusDays(1));

        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of(pt));

        scheduler.processDuePlannedTransactions();

        verify(executor, times(1)).execute(pt);
        verify(executor, never()).recordFailure(any(), anyString());
    }

    // ---- Cas : plusieurs transactions dues, toutes réussies ----

    @Test
    void processDue_multipleDue_allExecuted() {
        PlannedTransaction pt1 = mockPlannedTransaction(1L, LocalDate.now().minusDays(1));
        PlannedTransaction pt2 = mockPlannedTransaction(2L, LocalDate.now());
        PlannedTransaction pt3 = mockPlannedTransaction(3L, LocalDate.now().minusDays(30));

        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of(pt1, pt2, pt3));

        scheduler.processDuePlannedTransactions();

        verify(executor, times(1)).execute(pt1);
        verify(executor, times(1)).execute(pt2);
        verify(executor, times(1)).execute(pt3);
        verify(executor, never()).recordFailure(any(), anyString());
    }

    // ---- Cas : exécution échoue → recordFailure appelé, les autres continuent ----

    @Test
    void processDue_executionFails_recordFailureCalledAndOthersContinue() {
        PlannedTransaction pt1 = mockPlannedTransaction(1L, LocalDate.now().minusDays(1));
        PlannedTransaction pt2 = mockPlannedTransaction(2L, LocalDate.now());

        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of(pt1, pt2));

        doThrow(new RuntimeException("DB error")).when(executor).execute(pt1);

        scheduler.processDuePlannedTransactions();

        verify(executor).execute(pt1);
        verify(executor).recordFailure(pt1, "DB error");
        verify(executor).execute(pt2);            // pt2 est bien exécuté malgré l'échec de pt1
        verify(executor, times(1)).recordFailure(any(), anyString()); // un seul échec
    }

    // ---- Cas : toutes les exécutions échouent ----

    @Test
    void processDue_allFail_allRecordFailureCalled() {
        PlannedTransaction pt1 = mockPlannedTransaction(1L, LocalDate.now());
        PlannedTransaction pt2 = mockPlannedTransaction(2L, LocalDate.now());

        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of(pt1, pt2));

        doThrow(new RuntimeException("Erreur 1")).when(executor).execute(pt1);
        doThrow(new RuntimeException("Erreur 2")).when(executor).execute(pt2);

        scheduler.processDuePlannedTransactions();

        verify(executor).recordFailure(pt1, "Erreur 1");
        verify(executor).recordFailure(pt2, "Erreur 2");
    }

    // ---- Vérification que la query utilise la date du jour ----

    @Test
    void processDue_queryUsesToday() {
        LocalDate before = LocalDate.now();

        given(plannedTransactionRepository
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(any(LocalDate.class)))
                .willReturn(List.of());

        scheduler.processDuePlannedTransactions();

        LocalDate after = LocalDate.now();

        var dateCaptor = org.mockito.ArgumentCaptor.forClass(LocalDate.class);

        verify(plannedTransactionRepository)
                .findByIsActiveTrueAndAutoCreateTrueAndNextDueDateLessThanEqual(dateCaptor.capture());

        LocalDate capturedDate = dateCaptor.getValue();
        assertTrue(!capturedDate.isBefore(before) && !capturedDate.isAfter(after));
    }
}
