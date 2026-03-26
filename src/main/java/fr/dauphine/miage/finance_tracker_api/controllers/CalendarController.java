package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CalendarEventResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCalendarEventRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCalendarEventRequest;
import fr.dauphine.miage.finance_tracker_api.services.CalendarEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/calendar/events")
@Tag(name = "Calendar", description = "Manage calendar events (punctual financial events tied to a date)")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {

    private final CalendarEventService calendarEventService;

    public CalendarController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @Operation(
            summary = "List events for a month",
            description = "Retrieve all calendar events of the authenticated user for a given month (format: `YYYY-MM`)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CalendarEventResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid month format (expected YYYY-MM)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> findByMonth(@RequestParam YearMonth month,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(calendarEventService.findByMonth(userId, month));
    }

    @Operation(
            summary = "Get a calendar event by ID",
            description = "Retrieve a single calendar event by its ID. The event must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CalendarEventResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEventResponse> findById(@PathVariable Long id,
                                                          @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(calendarEventService.findById(id, userId));
    }

    @Operation(
            summary = "Create a calendar event",
            description = "Create a new calendar event for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CalendarEventResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CalendarEventResponse> create(@Valid @RequestBody CreationCalendarEventRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CalendarEventResponse created = calendarEventService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/calendar/events/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a calendar event",
            description = "Replace all fields of an existing calendar event. The event must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CalendarEventResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateCalendarEventRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(calendarEventService.update(id, request, userId));
    }

    @Operation(
            summary = "Delete a calendar event",
            description = "Permanently delete a calendar event. The event must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        calendarEventService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
