package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.TransactionResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.models.TransactionType;
import fr.dauphine.miage.finance_tracker_api.services.TransactionService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private TransactionResponse mockTransaction() {
        return new TransactionResponse(
                1L,
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                3L,
                "Alimentation",
                "Courses",
                LocalDate.of(2026, 3, 8),
                null,
                "MANUAL",
                Instant.now(),
                Instant.now()
        );
    }

    // ---- GET /v1/transactions ----

    @Test
    void findAll_authenticated_returns200() throws Exception {
        given(transactionService.findAll(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(mockTransaction())));

        mockMvc.perform(get("/v1/transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].label", is("Courses")));

        verify(transactionService).findAll(any(UUID.class), any(Pageable.class));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/transactions"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(transactionService);
    }

    // ---- GET /v1/transactions/search ----

    @Test
    void search_withFilters_returns200() throws Exception {
        given(transactionService.search(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(mockTransaction())));

        mockMvc.perform(get("/v1/transactions/search")
                        .param("type", "EXPENSE")
                        .param("label", "Courses")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].label", is("Courses")));

        verify(transactionService).search(
                eq(USER_ID),
                eq("EXPENSE"),
                isNull(),
                isNull(),
                isNull(),
                eq("Courses"),
                any(Pageable.class)
        );
    }

    @Test
    void search_noFilters_returns200() throws Exception {
        given(transactionService.search(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(mockTransaction())));

        mockMvc.perform(get("/v1/transactions/search")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk());

        verify(transactionService).search(
                eq(USER_ID),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void search_invalidType_returns400() throws Exception {
        given(transactionService.search(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .willThrow(new BadRequestException("Invalid transaction type"));

        mockMvc.perform(get("/v1/transactions/search")
                        .param("type", "INVALID")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/transactions/search"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(transactionService);
    }

    // ---- GET /v1/transactions/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(transactionService.findById(1L, USER_ID)).willReturn(mockTransaction());

        mockMvc.perform(get("/v1/transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.label", is("Courses")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(transactionService.findById(anyLong(), any()))
                .willThrow(new ResourceNotFoundException("Transaction not found"));

        mockMvc.perform(get("/v1/transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/transactions ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(transactionService.create(any(), any())).willReturn(mockTransaction());

        String body = """
                {
                    "amount": 50.00,
                    "type": "EXPENSE",
                    "categoryId": 3,
                    "label": "Courses",
                    "date": "2026-03-08"
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/transactions/1"));

        verify(transactionService).create(
                argThat(request ->
                        request.amount().compareTo(new BigDecimal("50.00")) == 0
                                && request.type() == TransactionType.EXPENSE
                                && request.categoryId().equals(3L)
                                && request.label().equals("Courses")
                                && request.date().equals(LocalDate.of(2026, 3, 8))
                                && request.note() == null
                ),
                eq(USER_ID)
        );
    }

    @Test
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }

    @Test
    void create_categoryNotFound_returns404() throws Exception {
        given(transactionService.create(any(), any()))
                .willThrow(new ResourceNotFoundException("Category not found"));

        String body = """
                {
                    "amount": 50.00,
                    "type": "EXPENSE",
                    "categoryId": 999,
                    "label": "Courses",
                    "date": "2026-03-08"
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- PUT /v1/transactions/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(transactionService.update(anyLong(), any(), any())).willReturn(mockTransaction());

        String body = """
                {
                    "amount": 60.00,
                    "type": "EXPENSE",
                    "categoryId": 3,
                    "label": "Courses mises à jour",
                    "date": "2026-03-08"
                }
                """;

        mockMvc.perform(put("/v1/transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(transactionService).update(
                eq(1L),
                argThat(request ->
                        request.amount().compareTo(new BigDecimal("60.00")) == 0
                                && request.type() == TransactionType.EXPENSE
                                && request.categoryId().equals(3L)
                                && request.label().equals("Courses mises à jour")
                                && request.date().equals(LocalDate.of(2026, 3, 8))
                                && request.note() == null
                ),
                eq(USER_ID)
        );
    }

    @Test
    void update_notFound_returns404() throws Exception {
        given(transactionService.update(anyLong(), any(), any()))
                .willThrow(new ResourceNotFoundException("Transaction not found"));

        String body = """
                {
                    "amount": 50.00,
                    "type": "EXPENSE",
                    "categoryId": 3,
                    "label": "Courses",
                    "date": "2026-03-08"
                }
                """;

        mockMvc.perform(put("/v1/transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---- DELETE /v1/transactions/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(transactionService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/transactions/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());

        verify(transactionService).delete(1L, USER_ID);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Transaction not found"))
                .given(transactionService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/transactions/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }
}
