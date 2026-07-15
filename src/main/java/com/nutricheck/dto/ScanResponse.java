package com.nutricheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for scan operations
 * Returns complete scan information with all analysis results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private Long scanId;
    private String productName;
    private LocalDateTime scannedAt;
    private List<ScanResultDto> results;
    private ScanSummary summary;
    private Integer safetyScore;
    private String overallAssessment;
    private List<String> warningsFor;
}