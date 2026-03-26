package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CreationDebtRequest;
import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentRequest;
import fr.dauphine.miage.finance_tracker_api.dto.DebtPaymentResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DebtSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateDebtRequest;
import fr.dauphine.miage.finance_tracker_api.services.DebtService;
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
@RequestMapping("/v1/debts")
@Tag(name = "Debts", description = "Manage debts and track repayment payments")
@SecurityRequirement(name = "bearerAuth")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @Operation(
            summary = "List all debts",
            description = "Retrieve all debts of the authenticated user regardless of status (ACTIVE, PAID, CANCELLED)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Debts retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = DebtResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DebtResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.findAll(userId));
    }

    @Operation(
            summary = "Get a debt by ID",
            description = "Retrieve a single debt by its ID. The debt must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Debt retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DebtResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> findById(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.findById(id, userId));
    }

    @Operation(
            summary = "Create a debt",
            description = "Register a new debt for the authenticated user. Status defaults to ACTIVE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Debt created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DebtResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DebtResponse> create(@Valid @RequestBody CreationDebtRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        DebtResponse created = debtService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/debts/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a debt",
            description = "Replace all fields of an existing debt. The debt must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Debt updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DebtResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateDebtRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.update(id, request, userId));
    }

    @Operation(
            summary = "Register a payment",
            description = "Record a repayment for an active debt. Reduces the remaining amount accordingly. The debt is automatically marked as PAID when the remaining amount reaches zero."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment registered successfully — returns the updated debt",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DebtResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error, or debt is not ACTIVE, or payment exceeds remaining amount", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Debt or linked transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PatchMapping("/{id}/payment")
    public ResponseEntity<DebtResponse> registerPayment(@PathVariable Long id,
                                                        @Valid @RequestBody DebtPaymentRequest request,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.registerPayment(id, request, userId));
    }

    @Operation(
            summary = "List payments for a debt",
            description = "Retrieve the paginated history of repayments for a given debt. The debt must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}/payments")
    public ResponseEntity<Page<DebtPaymentResponse>> findPayments(@PathVariable Long id,
                                                                   @AuthenticationPrincipal Jwt jwt,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.findPayments(id, userId, pageable));
    }

    @Operation(
            summary = "Debt summary",
            description = "Return aggregated statistics: number of active and paid debts, total remaining amount, and total monthly payment across all active debts."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary computed successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DebtSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<DebtSummaryResponse> summary(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(debtService.summary(userId));
    }

    @Operation(
            summary = "Delete a debt",
            description = "Permanently delete a debt and all its associated payments. The debt must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Debt deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Debt not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        debtService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
