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
@Tag(name = "Cards", description = "API para gerenciamento de cartões")
public class CardController {

    private final CardService service;
    private final FileUploadService fileUploadService;

    public CardController(CardService service, FileUploadService fileUploadService) {
        this.service = service;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping
    @Operation(
            summary = "Criar novo cartão",
            description = "Insere um novo cartão no sistema. Retorna o ID do cartão criado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão criado com sucesso",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<CardDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do cartão a ser criado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CardDto.class))
            )
            @RequestBody CardDto dto) {
        CardDto saved = service.save(dto);
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload de arquivo TXT com múltiplos cartões",
            description = "Processa arquivo contendo múltiplos números de cartão. " +
                         "O processamento é feito em lotes (batch) para otimizar performance com grandes volumes de dados. " +
                         "Retorna estatísticas do processamento incluindo quantidade processada, duplicados e erros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo processado com sucesso",
                    content = @Content(schema = @Schema(implementation = UploadResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou erro no processamento",
                    content = @Content)
    })
    public ResponseEntity<UploadResultDto> uploadFile(
            @Parameter(description = "Arquivo TXT", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            UploadResultDto error = new UploadResultDto();
            error.setStatus("ERROR");
            error.getErrors().add("Arquivo vazio");
            return ResponseEntity.badRequest().body(error);
        }

        UploadResultDto result = fileUploadService.processCardFile(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exists")
    @Operation(
            summary = "Verificar existência de cartão",
            description = "Consulta se um cartão existe no sistema pelo número completo. Retorna o ID se encontrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão encontrado"),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    public ResponseEntity<?> exists(
            @Parameter(description = "Número completo do cartão", required = true, example = "4456897999999999")
            @RequestParam("cardNumber") Long cardNumber) {
        Long id = service.findIdByCardNumber(cardNumber);
        return ResponseEntity.ok(Map.of("id", id));
    }
}
