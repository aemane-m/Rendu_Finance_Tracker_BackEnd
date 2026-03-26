package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CreationPlannedTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.PlannedTransactionRunResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdatePlannedTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.services.PlannedTransactionService;
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
@RequestMapping("/v1/planned-transactions")
@Tag(name = "Planned Transactions", description = "Manage recurring transactions and trigger manual executions")
@SecurityRequirement(name = "bearerAuth")
public class PlannedTransactionController {

    private final PlannedTransactionService plannedTransactionService;

    public PlannedTransactionController(PlannedTransactionService plannedTransactionService) {
        this.plannedTransactionService = plannedTransactionService;
    }

    @Operation(
            summary = "List all planned transactions",
            description = "Retrieve all planned (recurring) transactions of the authenticated user, both active and inactive."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Planned transactions retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlannedTransactionResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<PlannedTransactionResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.findAll(userId));
    }

    @Operation(
            summary = "Get a planned transaction by ID",
            description = "Retrieve a single planned transaction by its ID. The planned transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Planned transaction retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlannedTransactionResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlannedTransactionResponse> findById(@PathVariable Long id,
                                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.findById(id, userId));
    }

    @Operation(
            summary = "Create a planned transaction",
            description = "Create a new recurring transaction. Frequency can be DAILY, WEEKLY, MONTHLY or YEARLY. The transaction is active by default."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Planned transaction created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlannedTransactionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PlannedTransactionResponse> create(@Valid @RequestBody CreationPlannedTransactionRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        PlannedTransactionResponse created = plannedTransactionService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/planned-transactions/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a planned transaction",
            description = "Replace all fields of an existing planned transaction. The planned transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Planned transaction updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlannedTransactionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction or category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlannedTransactionResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody UpdatePlannedTransactionRequest request,
                                                             @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.update(id, request, userId));
    }

    @Operation(
            summary = "Toggle active state",
            description = "Enable or disable a planned transaction. A disabled transaction will not be executed automatically by the scheduler."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Active state toggled successfully — returns the updated planned transaction",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlannedTransactionResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PlannedTransactionResponse> toggle(@PathVariable Long id,
                                                             @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.toggle(id, userId));
    }

    @Operation(
            summary = "Execute immediately",
            description = "Manually trigger the execution of a planned transaction right now. Creates a real transaction and advances the next due date by one frequency period."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Planned transaction executed successfully — returns the execution run record",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlannedTransactionRunResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping("/{id}/run-now")
    public ResponseEntity<PlannedTransactionRunResponse> runNow(@PathVariable Long id,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.runNow(id, userId));
    }

    @Operation(
            summary = "List execution runs",
            description = "Retrieve the paginated history of execution runs for a given planned transaction."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Runs retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}/runs")
    public ResponseEntity<Page<PlannedTransactionRunResponse>> findRuns(@PathVariable Long id,
                                                                        @AuthenticationPrincipal Jwt jwt,
                                                                        @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(plannedTransactionService.findRuns(id, userId, pageable));
    }

    @Operation(
            summary = "Delete a planned transaction",
            description = "Permanently delete a planned transaction and all its execution runs. The planned transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Planned transaction deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Planned transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        plannedTransactionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
