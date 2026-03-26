package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.CalendarEventResponse;
import fr.dauphine.miage.finance_tracker_api.models.CalendarEvent;
import org.springframework.stereotype.Component;

@Component
public class CalendarEventMapper {

    public CalendarEventResponse toResponse(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getAmount(),
                event.getColor(),
                event.getNotes(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
