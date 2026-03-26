package fr.dauphine.miage.finance_tracker_api.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "budgets",
       uniqueConstraints = @UniqueConstraint(name = "uk_budgets_user_category", columnNames = {"user_id", "category_id"}))
@Getter @Setter @NoArgsConstructor @Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "monthly_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "alert_threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal alertThreshold;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Budget(Long id, UUID userId, Category category, BigDecimal monthlyLimit,
                  BigDecimal alertThreshold, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.alertThreshold = alertThreshold;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
