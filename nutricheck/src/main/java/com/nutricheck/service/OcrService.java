package com.nutricheck.service;

import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.enums.ProductCategory;
import com.nutricheck.entity.Scan;
import com.nutricheck.entity.ScanResult;
import com.nutricheck.entity.Ingredient;
import com.nutricheck.entity.User;
import com.nutricheck.exceptions.AiProcessingException;
import com.nutricheck.repository.ScanResultRepository;
import com.nutricheck.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService implements IOcrService {

    // ✅ Depends on INTERFACES, not concrete implementations (DIP)
    private final IImageAnalyzer imageAnalyzer;
    private final IUserService userService;
    private final IScanWriter scanWriter;
    private final IIngredientWriter ingredientWriter;
    private final ScanResultRepository scanResultRepository; // Acceptable - no service layer needed here yet

    @Override
    @Transactional
    public Scan processImageScan(
            byte[] imageBytes,
            String contentType,
            Long userId,
            ProductCategory category
    ) {
        // Step 1: Get AI analysis
        AiAnalysisResponse aiResponse = imageAnalyzer.analyzeImage(
                imageBytes, contentType, category);

        log.info("AI Analysis completed - Product: {}, Ingredients: {}",
                aiResponse.getProductName(),
                aiResponse.getResults() != null ? aiResponse.getResults().size() : 0);

        // Step 2: Get user via IUserService (not repository directly - DIP)
        User user = userService.getUserById(userId);

        // Step 3: Create scan via IScanWriter (not repository directly - DIP)
        Scan scan = scanWriter.createScan(aiResponse.getProductName(), user);

        log.info("Created scan ID: {} for product: {}", scan.getId(), scan.getProductName());

        // Step 4: Process each ingredient
        if (aiResponse.getResults() != null && !aiResponse.getResults().isEmpty()) {
            for (var analysis : aiResponse.getResults()) {
                try {
                    // Find or create ingredient via IIngredientWriter (DIP)
                    Ingredient ingredient = ingredientWriter.getOrCreate(
                            analysis.getIngredientName(),
                            analysis.getDescription(),
                            analysis.getCategory(),
                            analysis.getRisk(),
                            analysis.getSideEffects()
                    );

                    // Save scan result
                    ScanResult scanResult = ScanResult.builder()
                            .scan(scan)
                            .ingredient(ingredient)
                            .risk(analysis.getRisk())
                            .severity(analysis.getSeverity())
                            .explanation(analysis.getExplanation())
                            .build();

                    scanResultRepository.save(scanResult);

                    log.debug("Saved ingredient: {} (Risk: {})",
                            ingredient.getName(), analysis.getRisk());

                } catch (Exception e) {
                    // Log and continue - don't fail entire scan for one ingredient
                    log.error("Error processing ingredient: {}",
                            analysis.getIngredientName(), e);
                }
            }
        } else {
            log.warn("No ingredients found in AI response for: {}",
                    aiResponse.getProductName());
        }

        return scan;
    }
}