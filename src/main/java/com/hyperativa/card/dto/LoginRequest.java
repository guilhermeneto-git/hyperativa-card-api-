package com.hyperativa.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {

    @Schema(description = "Username", example = "admin", required = true)
    private String username;

    @Schema(description = "Password", example = "admin123", required = true)
    private String password;
}

