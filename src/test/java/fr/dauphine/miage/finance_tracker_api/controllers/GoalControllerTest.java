package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.GoalService;
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
@WebMvcTest(GoalController.class)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoalService goalService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private GoalResponse mockGoal() {
        return new GoalResponse(
                1L,
                "Vacances",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(500),
                LocalDate.of(2026, 12, 31),
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/goals ----

    @Test
    void findAll_authenticated_returns200() throws Exception {
        given(goalService.findAll(USER_ID)).willReturn(List.of(mockGoal()));

        mockMvc.perform(get("/v1/goals")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Vacances")));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/goals"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(goalService);
    }

    // ---- GET /v1/goals/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(goalService.findById(1L, USER_ID)).willReturn(mockGoal());

        mockMvc.perform(get("/v1/goals/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Vacances")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(goalService.findById(anyLong(), any())).willThrow(new ResourceNotFoundException("Goal not found"));

        mockMvc.perform(get("/v1/goals/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/goals ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(goalService.create(any(), any())).willReturn(mockGoal());

        String body = """
                {
                    "name": "Vacances",
                    "targetAmount": 3000.00,
                    "deadline": "2026-12-31"
                }
                """;

        mockMvc.perform(post("/v1/goals")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/goals/1"));
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/goals")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(goalService);
    }

    // ---- PUT /v1/goals/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(goalService.update(anyLong(), any(), any())).willReturn(mockGoal());

        String body = """
                {
                    "name": "Vacances été",
                    "targetAmount": 4000.00,
                    "deadline": "2026-12-31"
                }
                """;

        mockMvc.perform(put("/v1/goals/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(goalService.update(anyLong(), any(), any())).willThrow(new ResourceNotFoundException("Goal not found"));

        String body = """
                {
                    "name": "Vacances",
                    "targetAmount": 3000.00,
                    "deadline": "2026-12-31"
                }
                """;

        mockMvc.perform(put("/v1/goals/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- PATCH /v1/goals/{id}/contribute ----

    @Test
    void contribute_active_returns200() throws Exception {
        given(goalService.contribute(anyLong(), any(), any())).willReturn(mockGoal());

        String body = """
                {
                    "amount": 200.00,
                    "contributionDate": "2026-03-08"
                }
                """;

        mockMvc.perform(patch("/v1/goals/1/contribute")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void contribute_inactiveGoal_returns400() throws Exception {
        given(goalService.contribute(anyLong(), any(), any()))
                .willThrow(new BadRequestException("Goal is not ACTIVE"));

        String body = """
                {
                    "amount": 200.00,
                    "contributionDate": "2026-03-08"
                }
                """;

        mockMvc.perform(patch("/v1/goals/1/contribute")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /v1/goals/{id}/contributions ----

    @Test
    void findContributions_found_returns200() throws Exception {
        GoalContributionResponse contribution = new GoalContributionResponse(
                1L,
                1L,
                BigDecimal.valueOf(200),
                LocalDate.of(2026, 3, 8),
                null,
                null,
                Instant.now()
        );

        given(goalService.findContributions(anyLong(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(contribution)));

        mockMvc.perform(get("/v1/goals/1/contributions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ---- DELETE /v1/goals/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(goalService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/goals/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Goal not found")).given(goalService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/goals/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
