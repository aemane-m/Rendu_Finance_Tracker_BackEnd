package fr.dauphine.miage.finance_tracker_api.config;

import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Low-level HTTP client for the Supabase Auth Admin API.
 * Authenticates with the service_role key — must never be called from the frontend.
 */
@Component
public class SupabaseAdminClient {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAdminClient.class);

    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String serviceRoleKey;

    public SupabaseAdminClient(
            RestTemplate restTemplate,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this.restTemplate = restTemplate;
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
    }

    /**
     * Revokes all active sessions for the given user.
     * POST /auth/v1/admin/users/{userId}/logout
     */
    public void revokeAllSessions(UUID userId) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + userId + "/logout";
        log.info("Revoking all sessions for user {}", userId);
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(adminHeaders()), Void.class);
            log.info("All sessions revoked for user {}", userId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new BadRequestException("User not found in Supabase Auth: " + userId);
        } catch (HttpClientErrorException e) {
            log.error("Supabase Admin API error for user {}: {} {}", userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to revoke sessions: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Network error calling Supabase Admin API for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to reach Supabase Auth service");
        }
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", serviceRoleKey);
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        return headers;
    }
}
