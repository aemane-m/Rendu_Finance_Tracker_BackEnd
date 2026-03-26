package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CalendarEventResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.CalendarEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import fr.dauphine.miage.finance_tracker_api.config.SecurityConfig;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import(SecurityConfig.class)
@WebMvcTest(CalendarController.class)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarEventService calendarEventService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private CalendarEventResponse mockEvent() {
        return new CalendarEventResponse(
                1L,
                "Loyer",
                LocalDate.of(2026, 3, 1),
                BigDecimal.valueOf(900),
                "#FF0000",
                null,
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/calendar/events ----

    @Test
    void findByMonth_authenticated_returns200() throws Exception {
        given(calendarEventService.findByMonth(any(), any())).willReturn(List.of(mockEvent()));

        mockMvc.perform(get("/v1/calendar/events")
                        .param("month", "2026-03")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Loyer")));
    }

    @Test
    void findByMonth_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/calendar/events").param("month", "2026-03"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(calendarEventService);
    }

    @Test
    void findByMonth_missingMonthParam_returns400() throws Exception {
        mockMvc.perform(get("/v1/calendar/events")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /v1/calendar/events/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(calendarEventService.findById(1L, USER_ID)).willReturn(mockEvent());

        mockMvc.perform(get("/v1/calendar/events/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Loyer")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(calendarEventService.findById(anyLong(), any())).willThrow(new ResourceNotFoundException("Event not found"));

        mockMvc.perform(get("/v1/calendar/events/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/calendar/events ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(calendarEventService.create(any(), any())).willReturn(mockEvent());

        String body = """
                {
                    "title": "Loyer",
                    "eventDate": "2026-03-01",
                    "amount": 900.00
                }
                """;

        mockMvc.perform(post("/v1/calendar/events")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/calendar/events/1"));
    }

    @Test
    void create_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/v1/calendar/events")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(calendarEventService);
    }

    // ---- PUT /v1/calendar/events/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(calendarEventService.update(anyLong(), any(), any())).willReturn(mockEvent());

        String body = """
                {
                    "title": "Loyer mis à jour",
                    "eventDate": "2026-03-01"
                }
                """;

        mockMvc.perform(put("/v1/calendar/events/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(calendarEventService.update(anyLong(), any(), any())).willThrow(new ResourceNotFoundException("Event not found"));

        String body = """
                {
                    "title": "Loyer",
                    "eventDate": "2026-03-01"
                }
                """;

        mockMvc.perform(put("/v1/calendar/events/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /v1/calendar/events/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(calendarEventService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/calendar/events/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Event not found")).given(calendarEventService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/calendar/events/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
