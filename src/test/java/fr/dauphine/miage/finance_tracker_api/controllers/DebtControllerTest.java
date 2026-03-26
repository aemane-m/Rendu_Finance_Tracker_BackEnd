package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.DebtService;
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
@WebMvcTest(DebtController.class)
class DebtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DebtService debtService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private DebtResponse mockDebt() {
        return new DebtResponse(
                1L,
                "Banque Postale",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(2.5),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2027, 12, 31),
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/debts ----

    @Test
    void findAll_authenticated_returns200() throws Exception {
        given(debtService.findAll(USER_ID)).willReturn(List.of(mockDebt()));

        mockMvc.perform(get("/v1/debts")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].creditorName", is("Banque Postale")));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/debts"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(debtService);
    }

    // ---- GET /v1/debts/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(debtService.findById(1L, USER_ID)).willReturn(mockDebt());

        mockMvc.perform(get("/v1/debts/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.creditorName", is("Banque Postale")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(debtService.findById(anyLong(), any())).willThrow(new ResourceNotFoundException("Debt not found"));

        mockMvc.perform(get("/v1/debts/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/debts ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(debtService.create(any(), any())).willReturn(mockDebt());

        String body = """
                {
                    "creditorName": "Banque Postale",
                    "totalAmount": 10000.00,
                    "monthlyPayment": 300.00,
                    "startDate": "2025-01-01",
                    "dueDate": "2027-12-31"
                }
                """;

        mockMvc.perform(post("/v1/debts")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/debts/1"));
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/debts")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(debtService);
    }

    // ---- PUT /v1/debts/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(debtService.update(anyLong(), any(), any())).willReturn(mockDebt());

        String body = """
                {
                    "creditorName": "Banque Postale",
                    "totalAmount": 10000.00,
                    "monthlyPayment": 300.00,
                    "startDate": "2025-01-01",
                    "dueDate": "2027-12-31"
                }
                """;

        mockMvc.perform(put("/v1/debts/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(debtService.update(anyLong(), any(), any())).willThrow(new ResourceNotFoundException("Debt not found"));

        String body = """
                {
                    "creditorName": "Banque Postale",
                    "totalAmount": 10000.00,
                    "monthlyPayment": 300.00,
                    "startDate": "2025-01-01",
                    "dueDate": "2027-12-31"
                }
                """;

        mockMvc.perform(put("/v1/debts/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- PATCH /v1/debts/{id}/payment ----

    @Test
    void registerPayment_active_returns200() throws Exception {
        given(debtService.registerPayment(anyLong(), any(), any())).willReturn(mockDebt());

        String body = """
                {
                    "amount": 300.00,
                    "paymentDate": "2026-03-01"
                }
                """;

        mockMvc.perform(patch("/v1/debts/1/payment")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void registerPayment_inactiveDebt_returns400() throws Exception {
        given(debtService.registerPayment(anyLong(), any(), any()))
                .willThrow(new BadRequestException("Debt is not ACTIVE"));

        String body = """
                {
                    "amount": 300.00,
                    "paymentDate": "2026-03-01"
                }
                """;

        mockMvc.perform(patch("/v1/debts/1/payment")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- GET /v1/debts/{id}/payments ----

    @Test
    void findPayments_found_returns200() throws Exception {
        DebtPaymentResponse payment = new DebtPaymentResponse(
                1L,
                1L,
                LocalDate.of(2026, 3, 1),
                BigDecimal.valueOf(300),
                null,
                null,
                Instant.now()
        );

        given(debtService.findPayments(anyLong(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(payment)));

        mockMvc.perform(get("/v1/debts/1/payments")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ---- GET /v1/debts/summary ----

    @Test
    void summary_authenticated_returns200() throws Exception {
        DebtSummaryResponse summary = new DebtSummaryResponse(
                2,
                1,
                BigDecimal.valueOf(16000),
                BigDecimal.valueOf(600)
        );

        given(debtService.summary(USER_ID)).willReturn(summary);

        mockMvc.perform(get("/v1/debts/summary")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActiveDebts", is(2)));
    }

    // ---- DELETE /v1/debts/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(debtService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/debts/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Debt not found")).given(debtService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/debts/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
