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
@Table(name = "debt_payments")
@Getter @Setter @NoArgsConstructor @Builder
public class DebtPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DebtPayment(Long id, Debt debt, UUID userId, LocalDate paymentDate,
                       BigDecimal amount, String note, Transaction transaction, Instant createdAt) {
        this.id = id;
        this.debt = debt;
        this.userId = userId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.note = note;
        this.transaction = transaction;
        this.createdAt = createdAt;
    }
}
