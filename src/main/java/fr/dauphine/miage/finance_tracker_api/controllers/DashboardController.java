package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.dto.BudgetAlertResponse;
import fr.dauphine.miage.finance_tracker_api.dto.DashboardSummaryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.GoalProgressResponse;
import fr.dauphine.miage.finance_tracker_api.dto.SpendingByCategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.UpcomingEventResponse;
import fr.dauphine.miage.finance_tracker_api.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/dashboard")
@Tag(name = "Dashboard", description = "Aggregated financial insights for the authenticated user")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(
            summary = "Monthly summary",
            description = "Return total income, total expenses, net balance and transaction count for the given month (format: `YYYY-MM`)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Summary computed successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = DashboardSummaryResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid month format (expected YYYY-MM)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> summary(@RequestParam YearMonth month,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getSummary(userId, month));
    }

    @Operation(
            summary = "Spending by category",
            description = "Return total expenses grouped by category for the given month. Includes budget limit and usage percentage when a budget exists for the category."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Spending breakdown retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SpendingByCategoryResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid month format (expected YYYY-MM)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<SpendingByCategoryResponse>> spendingByCategory(@RequestParam YearMonth month,
                                                                               @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getSpendingByCategory(userId, month));
    }

    @Operation(
            summary = "Upcoming planned transactions",
            description = "Return the next N upcoming planned transactions ordered by due date. Defaults to 3 if `limit` is not specified."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Upcoming events retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UpcomingEventResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid limit value", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingEventResponse>> upcoming(@RequestParam(defaultValue = "3") int limit,
                                                                @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getUpcoming(userId, limit));
    }

    @Operation(
            summary = "Goals progress",
            description = "Return all active savings goals with their current progress percentage."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goals progress retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = GoalProgressResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/goals-progress")
    public ResponseEntity<List<GoalProgressResponse>> goalsProgress(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getGoalsProgress(userId));
    }

    @Operation(
            summary = "Budget alerts",
            description = "Return budgets whose spending has reached or exceeded the alert threshold for the given month."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Budget alerts retrieved successfully",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BudgetAlertResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid month format (expected YYYY-MM)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @GetMapping("/budget-alerts")
    public ResponseEntity<List<BudgetAlertResponse>> budgetAlerts(@RequestParam YearMonth month,
                                                                   @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardService.getBudgetAlerts(userId, month));
    }
}
