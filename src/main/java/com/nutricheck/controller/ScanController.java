package com.nutricheck.controller;

import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.ScanRequest;
import com.nutricheck.service.interfaces.ITextAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScanController {

    // ✅ Depends on interface, not concrete GeminiAiService (DIP)
    private final ITextAnalyzer textAnalyzer;

    @PostMapping("/ingredients")
    public ResponseEntity<AiAnalysisResponse> analyzeIngredients(
            @RequestBody ScanRequest scanRequest) {

        log.info("Analyzing ingredients - Category: {}", scanRequest.getProductCategory());

        // ✅ No try/catch - GlobalExceptionHandler handles errors
        AiAnalysisResponse response = textAnalyzer.analyzeText(
                scanRequest.getIngredients(),
                scanRequest.getProductCategory()
        );

        return ResponseEntity.ok(response);
    }
}