package edu.demart_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Convenience component to extract the current authenticated user's
 * details from the SecurityContextHolder.
 *
 * Works because JwtAuthenticationFilter stores:
 *  - principal   = email (String)
 *  - credentials = userId (Long)
 */
@Component
public class SecurityUtils {

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (String) auth.getPrincipal();
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (Long) auth.getCredentials();
    }
}
