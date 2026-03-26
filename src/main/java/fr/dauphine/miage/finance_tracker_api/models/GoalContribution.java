package fr.dauphine.miage.finance_tracker_api.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goal_contributions")
@Getter @Setter @NoArgsConstructor @Builder
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GoalContribution(Long id, Goal goal, UUID userId, BigDecimal amount,
                            LocalDate contributionDate, String note,
                            Transaction transaction, Instant createdAt) {
        this.id = id;
        this.goal = goal;
        this.userId = userId;
        this.amount = amount;
        this.contributionDate = contributionDate;
        this.note = note;
        this.transaction = transaction;
        this.createdAt = createdAt;
    }
}
