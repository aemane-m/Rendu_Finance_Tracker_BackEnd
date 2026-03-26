package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.ConflictException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.BudgetService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import(SecurityConfig.class)
@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private BudgetResponse mockBudget() {
        return new BudgetResponse(
                1L,
                10L,
                "Food",
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(80),
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/budgets ----

    @Test
    void findAll_authenticated_returns200WithList() throws Exception {
        given(budgetService.findAll(USER_ID)).willReturn(List.of(mockBudget()));

        mockMvc.perform(get("/v1/budgets")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/budgets"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(budgetService);
    }

    // ---- GET /v1/budgets/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(budgetService.findById(1L, USER_ID)).willReturn(mockBudget());

        mockMvc.perform(get("/v1/budgets/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.categoryName", is("Food")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(budgetService.findById(anyLong(), any())).willThrow(new ResourceNotFoundException("Budget not found"));

        mockMvc.perform(get("/v1/budgets/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/budgets ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(budgetService.create(any(), any())).willReturn(mockBudget());

        String body = """
                {
                    "categoryId": 10,
                    "monthlyLimit": 500.00,
                    "alertThreshold": 80.00
                }
                """;

        mockMvc.perform(post("/v1/budgets")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/budgets/1"))
                .andExpect(jsonPath("$.id", is(1)));

        verify(budgetService).create(any(), org.mockito.ArgumentMatchers.eq(USER_ID));
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/budgets")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(budgetService);
    }

    @Test
    void create_duplicateCategory_returns409() throws Exception {
        given(budgetService.create(any(), any())).willThrow(new ConflictException("Budget already exists for this category"));

        String body = """
                {
                    "categoryId": 10,
                    "monthlyLimit": 500.00,
                    "alertThreshold": 80.00
                }
                """;

        mockMvc.perform(post("/v1/budgets")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ---- PUT /v1/budgets/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(budgetService.update(anyLong(), any(), any())).willReturn(mockBudget());

        String body = """
                {
                    "categoryId": 10,
                    "monthlyLimit": 600.00,
                    "alertThreshold": 90.00
                }
                """;

        mockMvc.perform(put("/v1/budgets/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(budgetService).update(anyLong(), any(), org.mockito.ArgumentMatchers.eq(USER_ID));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(budgetService.update(anyLong(), any(), any())).willThrow(new ResourceNotFoundException("Budget not found"));

        String body = """
                {
                    "categoryId": 10,
                    "monthlyLimit": 600.00,
                    "alertThreshold": 90.00
                }
                """;

        mockMvc.perform(put("/v1/budgets/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /v1/budgets/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(budgetService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/budgets/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());

        verify(budgetService).delete(1L, USER_ID);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Budget not found")).given(budgetService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/budgets/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
