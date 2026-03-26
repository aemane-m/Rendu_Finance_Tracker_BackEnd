package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserIdAndEventDateBetween(UUID userId, LocalDate start, LocalDate end);
    Optional<CalendarEvent> findByIdAndUserId(Long id, UUID userId);

    List<CalendarEvent> findByUserIdAndEventDateGreaterThanEqualOrderByEventDateAsc(UUID userId, LocalDate date);
}
