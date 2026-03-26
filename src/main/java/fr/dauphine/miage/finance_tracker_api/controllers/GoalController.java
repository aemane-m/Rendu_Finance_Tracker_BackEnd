package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CreationGoalRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.GoalContributionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateGoalRequest;
import fr.dauphine.miage.finance_tracker_api.services.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/goals")
@Tag(name = "Goals", description = "Manage savings goals and track contributions")
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(
            summary = "List all goals",
            description = "Retrieve all savings goals of the authenticated user regardless of status (ACTIVE, COMPLETED, ARCHIVED)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goals retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = GoalResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<GoalResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(goalService.findAll(userId));
    }

    @Operation(
            summary = "Get a goal by ID",
            description = "Retrieve a single savings goal by its ID. The goal must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goal retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoalResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> findById(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(goalService.findById(id, userId));
    }

    @Operation(
            summary = "Create a goal",
            description = "Create a new savings goal for the authenticated user. Status defaults to ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Goal created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody CreationGoalRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        GoalResponse created = goalService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/goals/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a goal",
            description = "Replace all fields of an existing savings goal. The goal must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goal updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateGoalRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(goalService.update(id, request, userId));
    }

    @Operation(
            summary = "Add a contribution",
            description = "Record a contribution towards an active savings goal. The current amount is incremented accordingly. The goal is automatically marked as COMPLETED when the current amount reaches the target amount."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contribution recorded successfully — returns the updated goal",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = GoalResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error, or goal is not ACTIVE", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal or linked transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PatchMapping("/{id}/contribute")
    public ResponseEntity<GoalResponse> contribute(@PathVariable Long id,
                                                   @Valid @RequestBody GoalContributionRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(goalService.contribute(id, request, userId));
    }

    @Operation(
            summary = "List contributions for a goal",
            description = "Retrieve the paginated history of contributions for a given savings goal. The goal must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Contributions retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}/contributions")
    public ResponseEntity<Page<GoalContributionResponse>> findContributions(@PathVariable Long id,
                                                                            @AuthenticationPrincipal Jwt jwt,
                                                                            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(goalService.findContributions(id, userId, pageable));
    }

    @Operation(
            summary = "Delete a goal",
            description = "Permanently delete a savings goal and all its contributions. The goal must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Goal deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        goalService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
