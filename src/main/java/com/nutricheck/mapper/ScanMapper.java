package com.nutricheck.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.ScanResponse;
import com.nutricheck.dto.ScanResultDto;
import com.nutricheck.dto.ScanSummary;
import com.nutricheck.entity.Scan;
import com.nutricheck.mapper.interfaces.IScanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanMapper implements IScanMapper {

    private final ObjectMapper objectMapper;

    @Override
    public ScanResponse toScanResponse(Scan scan) {
        if (scan == null) {
            throw new IllegalArgumentException("Scan cannot be null");
        }

        AiAnalysisResponse aiResponse = null;
        if (scan.getAiAnalysisResponse() != null && !scan.getAiAnalysisResponse().isEmpty()) {
            try {
                aiResponse = objectMapper.readValue(scan.getAiAnalysisResponse(), AiAnalysisResponse.class);
            } catch (Exception e) {
                log.error("Failed to deserialize AI response for scan id: {}", scan.getId(), e);
            }
        }

        List<ScanResultDto> resultDtos = new ArrayList<>();
        ScanSummary summary = null;

        if (aiResponse != null) {
            if (aiResponse.getResults() != null) {
                for (int i = 0; i < aiResponse.getResults().size(); i++) {
                    var ingredientAnalysis = aiResponse.getResults().get(i);
                    resultDtos.add(ScanResultDto.builder()
                            .resultId((long) (i + 1))
                            .ingredientName(ingredientAnalysis.getIngredientName())
                            .risk(ingredientAnalysis.getRisk())
                            .severity(ingredientAnalysis.getSeverity())
                            .explanation(ingredientAnalysis.getExplanation())
                            .description(ingredientAnalysis.getDescription())
                            .category(ingredientAnalysis.getCategory())
                            .sideEffects(ingredientAnalysis.getSideEffects() != null ? String.join(", ", ingredientAnalysis.getSideEffects()) : null)
                            .build());
                }
            }

            int lowCount = 0;
            int mediumCount = 0;
            int highCount = 0;
            if (aiResponse.getResults() != null) {
                for (var r : aiResponse.getResults()) {
                    if ("LOW".equalsIgnoreCase(r.getRisk())) {
                        lowCount++;
                    } else if ("MEDIUM".equalsIgnoreCase(r.getRisk())) {
                        mediumCount++;
                    } else if ("HIGH".equalsIgnoreCase(r.getRisk())) {
                        highCount++;
                    }
                }
            }

            String overallRisk = highCount > 0 ? "HIGH"
                    : mediumCount > 0 ? "MEDIUM"
                    : "LOW";

            summary = ScanSummary.builder()
                    .totalIngredients(aiResponse.getResults() != null ? aiResponse.getResults().size() : 0)
                    .lowRiskCount(lowCount)
                    .mediumRiskCount(mediumCount)
                    .highRiskCount(highCount)
                    .overallRisk(overallRisk)
                    .build();
        } else {
            summary = ScanSummary.builder()
                    .totalIngredients(0)
                    .lowRiskCount(0)
                    .mediumRiskCount(0)
                    .highRiskCount(0)
                    .overallRisk("LOW")
                    .build();
        }

        return ScanResponse.builder()
                .scanId(scan.getId())
                .productName(scan.getProductName())
                .scannedAt(scan.getScannedAt())
                .results(resultDtos)
                .summary(summary)
                .safetyScore(aiResponse != null ? aiResponse.getSafetyScore() : null)
                .overallAssessment(aiResponse != null ? aiResponse.getOverallAssessment() : null)
                .warningsFor(aiResponse != null ? aiResponse.getWarningsFor() : null)
                .build();
    }
}