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
@Table(name = "planned_transactions")
@Getter @Setter @NoArgsConstructor @Builder
public class PlannedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlannedFrequency frequency;

    @Column(name = "next_due_date", nullable = false)
    private LocalDate nextDueDate;

    @Column(name = "auto_create", nullable = false)
    private boolean autoCreate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlannedTransaction(Long id, UUID userId, String label, BigDecimal amount,
                              TransactionType type, Category category, PlannedFrequency frequency,
                              LocalDate nextDueDate, boolean autoCreate, boolean isActive,
                              Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.label = label;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.autoCreate = autoCreate;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
