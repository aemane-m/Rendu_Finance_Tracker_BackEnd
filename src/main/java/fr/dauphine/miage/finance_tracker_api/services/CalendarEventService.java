package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.CalendarEventResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCalendarEventRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCalendarEventRequest;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface CalendarEventService {
    List<CalendarEventResponse> findByMonth(UUID userId, YearMonth month);
    CalendarEventResponse findById(Long id, UUID userId);
    CalendarEventResponse create(CreationCalendarEventRequest request, UUID userId);
    CalendarEventResponse update(Long id, UpdateCalendarEventRequest request, UUID userId);
    void delete(Long id, UUID userId);
}
