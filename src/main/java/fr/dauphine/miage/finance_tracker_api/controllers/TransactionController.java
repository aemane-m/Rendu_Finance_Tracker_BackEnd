package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.CreationTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.dto.TransactionResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateTransactionRequest;
import fr.dauphine.miage.finance_tracker_api.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/transactions")
@Tag(name = "Transactions", description = "Manage income and expense transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "List all transactions",
            description = "Retrieve a paginated list of all transactions for the authenticated user, sorted by date descending by default."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transactionService.findAll(userId, pageable));
    }

    @Operation(
            summary = "Search transactions",
            description = "Retrieve a paginated list of transactions filtered by any combination of: type (INCOME / EXPENSE), categoryId, startDate, endDate, and label (case-insensitive partial match). All filters are optional."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid filter value (e.g. unknown transaction type)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> search(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String label,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transactionService.search(userId, type, categoryId, startDate, endDate, label, pageable));
    }

    @Operation(
            summary = "Get a transaction by ID",
            description = "Retrieve a single transaction by its ID. The transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable Long id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transactionService.findById(id, userId));
    }

    @Operation(
            summary = "Create a transaction",
            description = "Create a new manual transaction (INCOME or EXPENSE). The source is automatically set to `MANUAL`."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreationTransactionRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        TransactionResponse created = transactionService.create(request, userId);
        return ResponseEntity.created(URI.create("/v1/transactions/" + created.id())).body(created);
    }

    @Operation(
            summary = "Update a transaction",
            description = "Replace all fields of an existing transaction. The transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction updated successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation error — one or more fields are invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Transaction or category not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateTransactionRequest request,
                                                      @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transactionService.update(id, request, userId));
    }

    @Operation(
            summary = "Delete a transaction",
            description = "Permanently delete a transaction. The transaction must belong to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        transactionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
