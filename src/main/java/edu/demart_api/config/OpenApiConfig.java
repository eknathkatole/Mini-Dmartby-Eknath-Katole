package edu.demart_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI configuration.
 *
 * Swagger UI: http://localhost:8081/swagger-ui.html
 * OpenAPI JSON: http://localhost:8081/v3/api-docs
 *
 * The "bearerAuth" security scheme lets you paste a JWT token
 * in Swagger UI's "Authorize" button and test protected endpoints directly.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("D-Mart Grocery Store API")
                        .description("""
                                Mini D-Mart REST API — Round 2 Full Stack Developer Assessment.
                                
                                **Authentication:** Use `POST /api/v1/auth/login` to get a JWT token,
                                then click **Authorize** and enter: `<your-token>` (without the Bearer prefix — Swagger adds it automatically).
                                
                                **Roles:**
                                - `CUSTOMER` — Place orders, submit returns
                                - `STAFF` — Order fulfillment, stock updates, return processing
                                - `ADMIN` — Full access + user/category/product management
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("D-Mart API")
                                .email("admin@dmart.com")))

                // Register the Bearer JWT security scheme
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here (without 'Bearer ' prefix)")))

                // Apply bearer auth globally — all endpoints show the lock icon
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
