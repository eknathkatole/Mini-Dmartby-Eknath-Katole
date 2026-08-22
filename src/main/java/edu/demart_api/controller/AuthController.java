package edu.demart_api.controller;

import edu.demart_api.dto.request.LoginRequest;
import edu.demart_api.dto.request.RegisterRequest;
import edu.demart_api.dto.response.ApiResponse;
import edu.demart_api.dto.response.AuthResponse;
import edu.demart_api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── Public Endpoints ────────────────────────────────────────────────────

    /** Customer self-registration — always assigns CUSTOMER role */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    // ─── Admin-only Endpoints ────────────────────────────────────────────────

    /**
     * Admin creates a STAFF or ADMIN account.
     * Protected by both route-level security (/api/v1/admin/**) and @PreAuthorize.
     *
     * Example: POST /api/v1/admin/users?role=STAFF
     */
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> createUser(
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(defaultValue = "STAFF") String role) {
        AuthResponse authResponse = authService.createUserWithRole(request, role);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created with role " + role.toUpperCase(), authResponse));
    }
}
