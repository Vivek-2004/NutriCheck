package com.nutricheck.service.interfaces;

import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.enums.ProductCategory;

public interface IImageAnalyzer {
    AiAnalysisResponse analyzeImage(byte[] imageBytes, String mimeType, ProductCategory category);
}