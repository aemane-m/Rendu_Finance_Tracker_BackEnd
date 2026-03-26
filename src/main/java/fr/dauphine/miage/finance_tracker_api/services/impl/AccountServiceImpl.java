package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.config.SupabaseAdminClient;
import fr.dauphine.miage.finance_tracker_api.services.AccountService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Only logoutAll is handled server-side: it requires the Supabase service_role key
 * which must never be exposed to the frontend.
 *
 * All other account operations (profile read/update, password change, single logout)
 * are performed client-side via the Supabase JS SDK.
 *
 * The userId passed to logoutAll always comes from the authenticated JWT (sub claim),
 * never from a client-supplied parameter — see AccountController.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final SupabaseAdminClient supabaseAdminClient;

    public AccountServiceImpl(SupabaseAdminClient supabaseAdminClient) {
        this.supabaseAdminClient = supabaseAdminClient;
    }

    @Override
    public void logoutAll(UUID userId) {
        supabaseAdminClient.revokeAllSessions(userId);
    }
}
