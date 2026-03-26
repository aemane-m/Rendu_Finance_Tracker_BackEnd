package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CalendarEventResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCalendarEventRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCalendarEventRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.CalendarEventMapper;
import fr.dauphine.miage.finance_tracker_api.models.CalendarEvent;
import fr.dauphine.miage.finance_tracker_api.repository.CalendarEventRepository;
import fr.dauphine.miage.finance_tracker_api.services.CalendarEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarEventMapper calendarEventMapper;

    public CalendarEventServiceImpl(CalendarEventRepository calendarEventRepository,
                                     CalendarEventMapper calendarEventMapper) {
        this.calendarEventRepository = calendarEventRepository;
        this.calendarEventMapper = calendarEventMapper;
    }

    @Override
    public List<CalendarEventResponse> findByMonth(UUID userId, YearMonth month) {
        return calendarEventRepository
                .findByUserIdAndEventDateBetween(userId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .map(calendarEventMapper::toResponse)
                .toList();
    }

    @Override
    public CalendarEventResponse findById(Long id, UUID userId) {
        CalendarEvent event = calendarEventRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found: " + id));
        return calendarEventMapper.toResponse(event);
    }

    @Override
    public CalendarEventResponse create(CreationCalendarEventRequest request, UUID userId) {
        Instant now = Instant.now();
        CalendarEvent event = CalendarEvent.builder()
                .userId(userId)
                .title(request.title())
                .eventDate(request.eventDate())
                .amount(request.amount())
                .color(request.color())
                .notes(request.notes())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return calendarEventMapper.toResponse(calendarEventRepository.save(event));
    }

    @Override
    public CalendarEventResponse update(Long id, UpdateCalendarEventRequest request, UUID userId) {
        CalendarEvent event = calendarEventRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found: " + id));

        event.setTitle(request.title());
        event.setEventDate(request.eventDate());
        event.setAmount(request.amount());
        event.setColor(request.color());
        event.setNotes(request.notes());
        event.setUpdatedAt(Instant.now());

        return calendarEventMapper.toResponse(calendarEventRepository.save(event));
    }

    @Override
    public void delete(Long id, UUID userId) {
        CalendarEvent event = calendarEventRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found: " + id));
        calendarEventRepository.delete(event);
    }
}
