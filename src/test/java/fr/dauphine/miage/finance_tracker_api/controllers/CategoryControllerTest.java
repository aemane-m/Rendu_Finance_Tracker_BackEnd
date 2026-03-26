package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CategoryResponse;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ConflictException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.services.CategoryService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import(SecurityConfig.class)
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private CategoryResponse mockCategory() {
        return new CategoryResponse(
                1L,
                "Alimentation",
                "🍔",
                "#00FF00",
                false,
                Instant.now()
        );
    }

    // ---- GET /v1/categories ----

    @Test
    void findAll_authenticated_returns200() throws Exception {
        given(categoryService.findAll(USER_ID)).willReturn(List.of(mockCategory()));

        mockMvc.perform(get("/v1/categories")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alimentation")));
    }

    @Test
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/categories"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(categoryService);
    }

    // ---- GET /v1/categories/{id} ----

    @Test
    void findById_found_returns200() throws Exception {
        given(categoryService.findById(1L, USER_ID)).willReturn(mockCategory());

        mockMvc.perform(get("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alimentation")));
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        given(categoryService.findById(anyLong(), any())).willThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/v1/categories/999")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNotFound());
    }

    // ---- POST /v1/categories ----

    @Test
    void create_validRequest_returns201() throws Exception {
        given(categoryService.create(any(), any())).willReturn(mockCategory());

        String body = """
                {
                    "name": "Alimentation",
                    "icon": "🍔",
                    "color": "#00FF00"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/v1/categories/1"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        String body = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        given(categoryService.create(any(), any())).willThrow(new ConflictException("Category name already exists"));

        String body = """
                {
                    "name": "Alimentation"
                }
                """;

        mockMvc.perform(post("/v1/categories")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ---- PUT /v1/categories/{id} ----

    @Test
    void update_found_returns200() throws Exception {
        given(categoryService.update(anyLong(), any(), any())).willReturn(mockCategory());

        String body = """
                {
                    "name": "Alimentation"
                }
                """;

        mockMvc.perform(put("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void update_defaultCategory_returns400() throws Exception {
        given(categoryService.update(anyLong(), any(), any())).willThrow(new BadRequestException("Default categories cannot be updated"));

        String body = """
                {
                    "name": "Nouveau nom"
                }
                """;

        mockMvc.perform(put("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /v1/categories/{id} ----

    @Test
    void delete_found_returns204() throws Exception {
        willDoNothing().given(categoryService).delete(1L, USER_ID);

        mockMvc.perform(delete("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_defaultCategory_returns400() throws Exception {
        willThrow(new BadRequestException("Default categories cannot be deleted")).given(categoryService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_categoryStillUsed_returns409() throws Exception {
        willThrow(new ConflictException("Category is still used by a budget or transaction"))
                .given(categoryService).delete(anyLong(), any());

        mockMvc.perform(delete("/v1/categories/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID.toString()))))
                .andExpect(status().isConflict());
    }
}
