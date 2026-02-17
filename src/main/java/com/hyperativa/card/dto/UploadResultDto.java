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
@Schema(description = "File upload processing result")
public class UploadResultDto {

    @Schema(description = "Processed batch name", example = "LOTE0001000010")
    private String loteName;

    @Schema(description = "Batch date (YYYYMMDD)", example = "20180524")
    private String loteDate;

    @Schema(description = "Number of records declared in the file", example = "10")
    private Integer declaredCount;

    @Schema(description = "Number of successfully processed records", example = "8")
    private Integer processedCount;

    @Schema(description = "Number of duplicate records (already existing)", example = "2")
    private Integer duplicatedCount;

    @Schema(description = "Number of records with errors", example = "0")
    private Integer errorCount;

    @Schema(description = "List of errors found during processing")
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Processing status", example = "SUCCESS")
    private String status;
}

