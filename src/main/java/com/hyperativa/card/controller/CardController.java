package com.hyperativa.card.controller;

import com.hyperativa.card.dto.CardDto;
import com.hyperativa.card.dto.UploadResultDto;
import com.hyperativa.card.service.CardService;
import com.hyperativa.card.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/cards")
@Tag(name = "Cards", description = "Card management API")
public class CardController {

    private final CardService service;
    private final FileUploadService fileUploadService;

    public CardController(CardService service, FileUploadService fileUploadService) {
        this.service = service;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping
    @Operation(
            summary = "Create new card",
            description = "Inserts a new card in the system. Returns the ID of the created card."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card created successfully",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    })
    public ResponseEntity<CardDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Card data to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CardDto.class))
            )
            @RequestBody CardDto dto) {
        CardDto saved = service.save(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload TXT file with multiple cards",
            description = "Processes file containing multiple card numbers. " +
                         "Processing is done in batches to optimize performance with large data volumes. " +
                         "Returns processing statistics including processed quantity, duplicates and errors."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File processed successfully",
                    content = @Content(schema = @Schema(implementation = UploadResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or processing error",
                    content = @Content)
    })
    public ResponseEntity<UploadResultDto> uploadFile(
            @Parameter(description = "TXT file", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            UploadResultDto error = new UploadResultDto();
            error.setStatus("ERROR");
            error.getErrors().add("Empty file");
            return ResponseEntity.badRequest().body(error);
        }

        UploadResultDto result = fileUploadService.processCardFile(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exists")
    @Operation(
            summary = "Check card existence",
            description = "Checks if a card exists in the system by its complete number. Returns the ID if found."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found"),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    public ResponseEntity<?> exists(
            @Parameter(description = "Complete card number", required = true, example = "4456897999999999")
            @RequestParam("cardNumber") Long cardNumber) {
        Long id = service.findIdByCardNumber(cardNumber);
        return ResponseEntity.ok(Map.of("id", id));
    }
}
