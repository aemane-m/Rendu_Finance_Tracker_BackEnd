package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetAlertResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DashboardSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalProgressResponse;
import fr.dauphine.miage.finance_tracker_api.dto.SpendingByCategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpcomingEventResponse;
import fr.dauphine.miage.finance_tracker_api.services.DashboardService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import(SecurityConfig.class)
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // ---- GET /v1/dashboard/summary ----

    @Test
    void summary_authenticated_returns200() throws Exception {
        YearMonth month = YearMonth.of(2026, 3);
        DashboardSummaryResponse response = new DashboardSummaryResponse(
                month,
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(1500),
                10,
                null
        );

        given(dashboardService.getSummary(any(), any())).willReturn(response);

        mockMvc.perform(get("/v1/dashboard/summary")
                        .param("month", "2026-03")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionCount", is(10)));

        verify(dashboardService).getSummary(USER_ID, month);
    }

    @Test
    void summary_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/dashboard/summary").param("month", "2026-03"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(dashboardService);
    }

    @Test
    void summary_missingMonthParam_returns400() throws Exception {
        mockMvc.perform(get("/v1/dashboard/summary")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /v1/dashboard/spending-by-category ----

    @Test
    void spendingByCategory_returns200() throws Exception {
        YearMonth month = YearMonth.of(2026, 3);
        SpendingByCategoryResponse item = new SpendingByCategoryResponse(
                1L,
                "Food",
                BigDecimal.valueOf(300),
                null,
                null
        );

        given(dashboardService.getSpendingByCategory(any(), any())).willReturn(List.of(item));

        mockMvc.perform(get("/v1/dashboard/spending-by-category")
                        .param("month", "2026-03")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName", is("Food")));

        verify(dashboardService).getSpendingByCategory(USER_ID, month);
    }

    // ---- GET /v1/dashboard/upcoming ----

    @Test
    void upcoming_returns200() throws Exception {
        UpcomingEventResponse item = new UpcomingEventResponse(
                1L,
                "Abonnement Netflix",
                LocalDate.of(2026, 3, 15),
                BigDecimal.valueOf(15),
                null
        );

        given(dashboardService.getUpcoming(any(), anyInt())).willReturn(List.of(item));

        mockMvc.perform(get("/v1/dashboard/upcoming")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Abonnement Netflix")));

        verify(dashboardService).getUpcoming(USER_ID, 3);
    }

    @Test
    void upcoming_customLimit_callsServiceWithGivenLimit() throws Exception {
        given(dashboardService.getUpcoming(any(), anyInt())).willReturn(List.of());

        mockMvc.perform(get("/v1/dashboard/upcoming")
                        .param("limit", "5")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk());

        verify(dashboardService).getUpcoming(USER_ID, 5);
    }

    @Test
    void upcoming_invalidLimit_returns400() throws Exception {
        mockMvc.perform(get("/v1/dashboard/upcoming")
                        .param("limit", "abc")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /v1/dashboard/goals-progress ----

    @Test
    void goalsProgress_returns200() throws Exception {
        GoalProgressResponse item = new GoalProgressResponse(
                1L,
                "Vacances",
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(25),
                null,
                "ACTIVE"
        );

        given(dashboardService.getGoalsProgress(any())).willReturn(List.of(item));

        mockMvc.perform(get("/v1/dashboard/goals-progress")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Vacances")));

        verify(dashboardService).getGoalsProgress(USER_ID);
    }

    // ---- GET /v1/dashboard/budget-alerts ----

    @Test
    void budgetAlerts_returns200() throws Exception {
        YearMonth month = YearMonth.of(2026, 3);
        BudgetAlertResponse alert = new BudgetAlertResponse(
                1L,
                10L,
                "Food",
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(450),
                BigDecimal.valueOf(50),
                BigDecimal.ZERO,
                BigDecimal.valueOf(90),
                false
        );

        given(dashboardService.getBudgetAlerts(any(), any())).willReturn(List.of(alert));

        mockMvc.perform(get("/v1/dashboard/budget-alerts")
                        .param("month", "2026-03")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName", is("Food")));

        verify(dashboardService).getBudgetAlerts(USER_ID, month);
    }

    @Test
    void summary_invalidMonthFormat_returns400() throws Exception {
        mockMvc.perform(get("/v1/dashboard/summary")
                        .param("month", "2026/03")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }
}
