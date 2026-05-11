package com.nutricheck.mapper;

import com.nutricheck.dto.ScanResponse;
import com.nutricheck.dto.ScanResultDto;
import com.nutricheck.dto.ScanSummary;
import com.nutricheck.entity.Scan;
import com.nutricheck.entity.ScanResult;
import com.nutricheck.mapper.interfaces.IScanMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScanMapper implements IScanMapper {

    @Override
    public ScanResponse toScanResponse(Scan scan, List<ScanResult> results) {
        if (scan == null) {
            throw new IllegalArgumentException("Scan cannot be null");
        }

        List<ScanResultDto> resultDtos = results.stream()
                .map(this::toScanResultDto)
                .collect(Collectors.toList());

        ScanSummary summary = toScanSummary(results);

        return ScanResponse.builder()
                .scanId(scan.getId())
                .productName(scan.getProductName())
                .scannedAt(scan.getScannedAt())
                .userId(scan.getUser().getId())
                .userName(scan.getUser().getName())
                .results(resultDtos)
                .summary(summary)
                .build();
    }

    @Override
    public ScanResultDto toScanResultDto(ScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }

        return ScanResultDto.builder()
                .resultId(scanResult.getId())
                .ingredientName(scanResult.getIngredient().getName())
                .risk(scanResult.getRisk())
                .severity(scanResult.getSeverity())
                .explanation(scanResult.getExplanation())
                .description(scanResult.getIngredient().getDescription())
                .category(scanResult.getIngredient().getCategory())
                .sideEffects(scanResult.getIngredient().getSideEffects())
                .build();
    }

    @Override
    public ScanSummary toScanSummary(List<ScanResult> results) {
        if (results == null || results.isEmpty()) {
            return ScanSummary.builder()
                    .totalIngredients(0)
                    .lowRiskCount(0)
                    .mediumRiskCount(0)
                    .highRiskCount(0)
                    .overallRisk("LOW")
                    .build();
        }

        int lowCount = (int) results.stream()
                .filter(sr -> "LOW".equalsIgnoreCase(sr.getRisk()))
                .count();

        int mediumCount = (int) results.stream()
                .filter(sr -> "MEDIUM".equalsIgnoreCase(sr.getRisk()))
                .count();

        int highCount = (int) results.stream()
                .filter(sr -> "HIGH".equalsIgnoreCase(sr.getRisk()))
                .count();

        // Determine overall risk: if ANY ingredient is HIGH, overall is HIGH
        String overallRisk = highCount > 0 ? "HIGH"
                : mediumCount > 0 ? "MEDIUM"
                : "LOW";

        return ScanSummary.builder()
                .totalIngredients(results.size())
                .lowRiskCount(lowCount)
                .mediumRiskCount(mediumCount)
                .highRiskCount(highCount)
                .overallRisk(overallRisk)
                .build();
    }
}