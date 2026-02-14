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
                                                .description("Insira o JWT token. Exemplo: Bearer eyJhbGci...")
                                )
                )
                .info(new Info()
                        .title("Card API - Hyperativa Challenge")
                        .version("1.0.0")
                        .description("API REST para gerenciamento de cartões com autenticação JWT. " +
                                "Permite inserir cartões individualmente, via arquivo em lote, e consultar sua existência por número." +
                                "\n\n**Para autenticar:**\n" +
                                "1. Use um dos usuários pré-cadastrados:\n" +
                                "   - **admin** / **admin123** (Role: ADMIN)\n" +
                                "   - **user** / **user123** (Role: USER)\n" +
                                "2. Faça login em `/auth/login`\n" +
                                "3. Copie o token retornado\n" +
                                "4. Clique no botão 'Authorize' (🔒) e cole: `Bearer <token>`")
                        .contact(new Contact()
                                .name("Hyperativa")
                                .url("https://hyperativa.com")));
    }
}

