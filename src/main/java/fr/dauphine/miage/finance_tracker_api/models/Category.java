package fr.dauphine.miage.finance_tracker_api.models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories",
       uniqueConstraints = @UniqueConstraint(name = "uk_categories_user_name", columnNames = {"user_id", "name"}))
@Getter @Setter @NoArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String name;

    private String icon;

    private String color;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Category(Long id, UUID userId, String name, String icon,
                    String color, boolean isDefault, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }
}
