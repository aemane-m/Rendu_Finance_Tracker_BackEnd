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
@Table(name = "debts")
@Getter @Setter @NoArgsConstructor @Builder
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "creditor_name", nullable = false)
    private String creditorName;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "remaining_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "monthly_payment", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Debt(Long id, UUID userId, String creditorName, BigDecimal totalAmount,
                BigDecimal remainingAmount, BigDecimal monthlyPayment, BigDecimal interestRate,
                LocalDate startDate, LocalDate dueDate, DebtStatus status,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.creditorName = creditorName;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.monthlyPayment = monthlyPayment;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
