package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.Debt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserId(UUID userId);
    Optional<Debt> findByIdAndUserId(Long id, UUID userId);
}
