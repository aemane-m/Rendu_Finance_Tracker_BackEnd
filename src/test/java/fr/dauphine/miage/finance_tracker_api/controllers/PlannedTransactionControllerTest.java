package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionRunResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.PlannedTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import fr.dauphine.miage.finance_tracker_api.config.SecurityConfig;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
@WebMvcTest(PlannedTransactionController.class)
class PlannedTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlannedTransactionService plannedTransactionService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private PlannedTransactionResponse mockPlanned() {
        return new PlannedTransactionResponse(
                1L,
                "Loyer mensuel",
                BigDecimal.valueOf(900),
                "EXPENSE",
                5L,
                "Logement",
                "MONTHLY",
                LocalDate.of(2026, 4, 1),
                true,
                true,
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/planned-transactions ----

    @Test
    void findAll_authenticated_returns200() throws Exception {
        given(plannedTransactionService.findAll(USER_ID)).willReturn(List.of(mockPlanned()));

        mockMvc.perform(get("/v1/planned-transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].label", is("Loyer mensuel")));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/planned-transactions"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(plannedTransactionService);
    }

    // ---- GET /v1/planned-transactions/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(plannedTransactionService.findById(1L, USER_ID)).willReturn(mockPlanned());

        mockMvc.perform(get("/v1/planned-transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.label", is("Loyer mensuel")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(plannedTransactionService.findById(anyLong(), any()))
                .willThrow(new ResourceNotFoundException("Planned transaction not found"));

        mockMvc.perform(get("/v1/planned-transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/planned-transactions ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(plannedTransactionService.create(any(), any())).willReturn(mockPlanned());

        String body = """
                {
                    "label": "Loyer mensuel",
                    "amount": 900.00,
                    "type": "EXPENSE",
                    "categoryId": 5,
                    "frequency": "MONTHLY",
                    "nextDueDate": "2026-04-01",
                    "autoCreate": true
                }
                """;

        mockMvc.perform(post("/v1/planned-transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/planned-transactions/1"));
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/planned-transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(plannedTransactionService);
    }

    // ---- PUT /v1/planned-transactions/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(plannedTransactionService.update(anyLong(), any(), any())).willReturn(mockPlanned());

        String body = """
                {
                    "label": "Loyer mis à jour",
                    "amount": 950.00,
                    "type": "EXPENSE",
                    "categoryId": 5,
                    "frequency": "MONTHLY",
                    "nextDueDate": "2026-04-01",
                    "autoCreate": true
                }
                """;

        mockMvc.perform(put("/v1/planned-transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(plannedTransactionService.update(anyLong(), any(), any()))
                .willThrow(new ResourceNotFoundException("Planned transaction not found"));

        String body = """
                {
                    "label": "Loyer",
                    "amount": 900.00,
                    "type": "EXPENSE",
                    "categoryId": 5,
                    "frequency": "MONTHLY",
                    "nextDueDate": "2026-04-01",
                    "autoCreate": true
                }
                """;

        mockMvc.perform(put("/v1/planned-transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- PATCH /v1/planned-transactions/{id}/toggle ----

    @Test
    void toggle_found_returns200() throws Exception {
        given(plannedTransactionService.toggle(anyLong(), any())).willReturn(mockPlanned());

        mockMvc.perform(patch("/v1/planned-transactions/1/toggle")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void toggle_notFound_returns404() throws Exception {
        given(plannedTransactionService.toggle(anyLong(), any()))
                .willThrow(new ResourceNotFoundException("Planned transaction not found"));

        mockMvc.perform(patch("/v1/planned-transactions/999/toggle")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/planned-transactions/{id}/run-now ----

    @Test
    void runNow_found_returns200() throws Exception {
        PlannedTransactionRunResponse run = new PlannedTransactionRunResponse(
                1L,
                1L,
                LocalDate.of(2026, 3, 1),
                Instant.now(),
                "SUCCESS",
                42L,
                null
        );

        given(plannedTransactionService.runNow(anyLong(), any())).willReturn(run);

        mockMvc.perform(post("/v1/planned-transactions/1/run-now")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }

    @Test
    void runNow_notFound_returns404() throws Exception {
        given(plannedTransactionService.runNow(anyLong(), any()))
                .willThrow(new ResourceNotFoundException("Planned transaction not found"));

        mockMvc.perform(post("/v1/planned-transactions/999/run-now")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- GET /v1/planned-transactions/{id}/runs ----

    @Test
    void findRuns_found_returns200() throws Exception {
        PlannedTransactionRunResponse run = new PlannedTransactionRunResponse(
                1L,
                1L,
                LocalDate.of(2026, 3, 1),
                Instant.now(),
                "SUCCESS",
                null,
                null
        );

        given(plannedTransactionService.findRuns(anyLong(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(run)));

        mockMvc.perform(get("/v1/planned-transactions/1/runs")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ---- DELETE /v1/planned-transactions/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(plannedTransactionService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/planned-transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Planned transaction not found"))
                .given(plannedTransactionService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/planned-transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
