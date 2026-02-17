package com.hyperativa.card.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Insert JWT token. Example: Bearer eyJhbGci...")
                                )
                )
                .info(new Info()
                        .title("Card API - Hyperativa Challenge")
                        .version("1.0.0")
                        .description("REST API for card management with JWT authentication. " +
                                "Allows inserting cards individually, via batch file, and querying their existence by number." +
                                "\n\n**To authenticate:**\n" +
                                "1. Use one of the pre-registered users:\n" +
                                "   - **admin** / **admin123** (Role: ADMIN)\n" +
                                "   - **user** / **user123** (Role: USER)\n" +
                                "2. Login at `/auth/login`\n" +
                                "3. Copy the returned token\n" +
                                "4. Click the 'Authorize' button (🔒) and paste: `Bearer <token>`")
                        .contact(new Contact()
                                .name("Hyperativa")
                                .url("https://hyperativa.com")));
    }
}

