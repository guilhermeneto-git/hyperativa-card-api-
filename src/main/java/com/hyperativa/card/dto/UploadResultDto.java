package com.hyperativa.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado do processamento do upload de arquivo")
public class UploadResultDto {

    @Schema(description = "Nome do lote processado", example = "LOTE0001000010")
    private String loteName;

    @Schema(description = "Data do lote (YYYYMMDD)", example = "20180524")
    private String loteDate;

    @Schema(description = "Quantidade de registros declarada no arquivo", example = "10")
    private Integer declaredCount;

    @Schema(description = "Quantidade de registros processados com sucesso", example = "8")
    private Integer processedCount;

    @Schema(description = "Quantidade de registros duplicados (já existentes)", example = "2")
    private Integer duplicatedCount;

    @Schema(description = "Quantidade de registros com erro", example = "0")
    private Integer errorCount;

    @Schema(description = "Lista de erros encontrados durante o processamento")
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Status do processamento", example = "SUCCESS")
    private String status;
}

