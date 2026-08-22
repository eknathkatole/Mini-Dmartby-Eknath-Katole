package edu.demart_api.service;

import edu.demart_api.dto.request.LoginRequest;
import edu.demart_api.dto.request.RegisterRequest;
import edu.demart_api.dto.response.AuthResponse;

public interface AuthService {

    /** Public registration — always creates a CUSTOMER account */
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    /**
     * Admin-only — creates an account with the specified role (STAFF or ADMIN).
     * Callable only from a secured admin endpoint.
     */
    AuthResponse createUserWithRole(RegisterRequest request, String role);
}
