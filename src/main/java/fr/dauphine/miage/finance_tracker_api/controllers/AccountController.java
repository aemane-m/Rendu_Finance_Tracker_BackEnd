package fr.dauphine.miage.finance_tracker_api.controllers;

import fr.dauphine.miage.finance_tracker_api.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Profile, password and session management are handled directly by the frontend
 * via the Supabase JS SDK (supabase.auth.getUser / updateUser / signOut).
 *
 * Only logoutAll requires a server-side call with the service_role key
 * because Supabase does not expose session revocation to the client SDK.
 */
@RestController
@RequestMapping("/v1/account")
@Tag(name = "Account", description = "Server-side account operations that cannot be performed client-side")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "Logout from all devices",
            description = "Revoke all active sessions for the authenticated user via the Supabase Admin API. " +
                          "Use this when the user wants to sign out every device at once. " +
                          "Password, profile and single-session logout are handled client-side via the Supabase SDK."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All sessions revoked successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content)
    })
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.logoutAll(userId);
        return ResponseEntity.noContent().build();
    }
}
