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
@Schema(description = "Card data transfer object")
public class CardDto {

    @Schema(description = "Unique card ID in the system", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Complete card number", example = "4456897999999999", required = true)
    private Long cardNumber;
}

