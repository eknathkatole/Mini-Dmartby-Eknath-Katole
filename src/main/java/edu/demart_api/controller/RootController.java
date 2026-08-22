package edu.demart_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root Controller for the base URL /
 * Returns 200 OK with application metadata and links to Swagger UI and Health check.
 * This prevents 403 Access Denied or HTTP redirect issues when visiting the base domain.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootInfo() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Mini D-Mart Grocery Store REST API",
                "version", "1.0.0",
                "swaggerUi", "/swagger-ui.html",
                "health", "/api/v1/health",
                "message", "Welcome to Mini D-Mart API. Open /swagger-ui.html to test interactive endpoints."
        ));
    }
}
