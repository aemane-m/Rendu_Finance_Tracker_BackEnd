package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCategoryRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCategoryRequest;
import fr.dauphine.miage.finance_tracker_api.services.CategoryService;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Categories", description = "Manage transaction categories (default and user-defined)")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "List all categories",
            description = "Retrieve all categories visible to the authenticated user: default (shared) categories and their own custom categories."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(categoryService.findAll(userId));
    }

    @Operation(
            summary = "Get a category by ID",
            description = "Retrieve a single category by its ID. The category must be a default category or belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id,
                                                     @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(categoryService.findById(id, userId));
    }

    @Operation(
            summary = "Create a category",
            description = "Create a new custom category for the authenticated user. The name must be unique for this user (case-insensitive)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "409", description = "A category with this name already exists for this user", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreationCategoryRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CategoryResponse created = categoryService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/categories/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a category",
            description = "Replace all fields of an existing custom category. Default categories cannot be updated. The new name must be unique for this user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid or category is a default category", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "A category with this name already exists for this user", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateCategoryRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(categoryService.update(id, request, userId));
    }

    @Operation(
            summary = "Delete a category",
            description = "Permanently delete a custom category. Default categories cannot be deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Category is a default category and cannot be deleted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category is still referenced by other data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        categoryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
