package fr.dauphine.miage.finance_tracker_api.services;

import java.util.UUID;

/**
 * Profile, password and single-session logout are handled client-side via the Supabase SDK.
 * Only logoutAll requires a server-side Admin API call with the service_role key.
 */
public interface AccountService {
    void logoutAll(UUID userId);
}
