package fr.dauphine.miage.finance_tracker_api.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "planned_transaction_runs",
       uniqueConstraints = @UniqueConstraint(name = "uk_planned_run_due", columnNames = {"planned_transaction_id", "due_date"}))
@Getter @Setter @NoArgsConstructor @Builder
public class PlannedTransactionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_transaction_id", nullable = false)
    private PlannedTransaction plannedTransaction;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlannedRunStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_transaction_id")
    private Transaction createdTransaction;

    @Column(name = "error_message")
    private String errorMessage;

    public PlannedTransactionRun(Long id, PlannedTransaction plannedTransaction, UUID userId,
                                 LocalDate dueDate, Instant executedAt, PlannedRunStatus status,
                                 Transaction createdTransaction, String errorMessage) {
        this.id = id;
        this.plannedTransaction = plannedTransaction;
        this.userId = userId;
        this.dueDate = dueDate;
        this.executedAt = executedAt;
        this.status = status;
        this.createdTransaction = createdTransaction;
        this.errorMessage = errorMessage;
    }
}
