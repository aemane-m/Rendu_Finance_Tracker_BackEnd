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
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 2000)
    private String note;

    @Column(nullable = false)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Transaction(Long id, UUID userId, BigDecimal amount, TransactionType type,
                       Category category, String label, LocalDate date, String note,
                       String source, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.label = label;
        this.date = date;
        this.note = note;
        this.source = source;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
