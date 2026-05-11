package com.nutricheck.service.interfaces;

import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.enums.ProductCategory;

public interface ITextAnalyzer {
    AiAnalysisResponse analyzeText(String ingredients, ProductCategory category);
}