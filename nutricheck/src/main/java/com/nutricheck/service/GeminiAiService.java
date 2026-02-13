package com.nutricheck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.ScanRequest;
import com.nutricheck.dto.enums.ProductCategory;
import com.nutricheck.exceptions.AiProcessingException;
import com.nutricheck.service.interfaces.IImageAnalyzer;
import com.nutricheck.service.interfaces.ITextAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService implements ITextAnalyzer, IImageAnalyzer {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    // ============ ITextAnalyzer ============

    @Override
    public AiAnalysisResponse analyzeText(String ingredients, ProductCategory category) {
        if (ingredients == null || ingredients.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }

        String prompt = buildTextPrompt(ingredients, category);
        log.info("Calling Gemini for text analysis - Category: {}", category);

        try {
            String response = chatModel.call(prompt);
            return parseAiResponse(response);
        } catch (Exception e) {
            log.error("Text analysis failed: {}", e.getMessage(), e);
            throw new AiProcessingException("Failed to analyze ingredients: " + e.getMessage(), e);
        }
    }

    // ============ IImageAnalyzer ============

    @Override
    public AiAnalysisResponse analyzeImage(byte[] imageBytes, String mimeType, ProductCategory category) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image data cannot be empty");
        }

        String promptText = buildImagePrompt(category);

        var media = new Media(MimeTypeUtils.parseMimeType(mimeType),
                new ByteArrayResource(imageBytes));

        var userMessage = UserMessage.builder()
                .text(promptText)
                .media(List.of(media))
                .build();

        log.info("Calling Gemini for image analysis - Category: {}", category);

        try {
            ChatResponse response = chatModel.call(new Prompt(userMessage));
            String resultJson = response.getResult().getOutput().getText();
            return parseAiResponse(resultJson);
        } catch (Exception e) {
            log.error("Image analysis failed: {}", e.getMessage(), e);
            throw new AiProcessingException("Failed to analyze image: " + e.getMessage(), e);
        }
    }

    // ============ Private Helpers ============

    private AiAnalysisResponse parseAiResponse(String jsonResponse) {
        try {
            String cleanJson = jsonResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();
            return objectMapper.readValue(cleanJson, AiAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", jsonResponse, e);
            throw new AiProcessingException("Invalid AI response format", e);
        }
    }

    private String buildTextPrompt(String ingredientList, ProductCategory category) {
        return String.format("""
        You are a Nutritionist and Product Safety Expert analyzing a %s product.

        Ingredients:
        %s

        Analyze each ingredient and respond with ONLY valid JSON. No markdown, no extra text.

        Use this exact structure:
        {
          "productName": "Unknown Product",
          "results": [
            {
              "ingredientName": "string",
              "risk": "LOW | MEDIUM | HIGH",
              "severity": "string",
              "explanation": "string",
              "description": "string",
              "category": "string",
              "sideEffects": ["string array"]
            }
          ],
          "safetyScore": number,
          "overallAssessment": "string",
          "warningsFor": ["string array"]
        }
        Rules:
        - Sort ingredients from LEAST to MOST harmful
        - Be factual and concise
        - Risk levels: LOW, MEDIUM, or HIGH only
        """, category.name(), ingredientList);
    }

    private String buildImagePrompt(ProductCategory category) {
        return String.format("""
        You are a Nutritionist and Product Safety Expert analyzing a %s product.

        Extract the product name and all ingredients from this image, then analyze each ingredient.

        Respond with ONLY valid JSON. No markdown, no extra text.

        Use this exact structure:
        {
          "productName": "string",
          "results": [
            {
              "ingredientName": "string",
              "risk": "LOW | MEDIUM | HIGH",
              "severity": "string",
              "explanation": "string",
              "description": "string",
              "category": "string",
              "sideEffects": ["string array"]
            }
          ],
          "safetyScore": number,
          "overallAssessment": "string",
          "warningsFor": ["string array"]
        }
        Rules:
        - Extract ALL visible ingredients from the image
        - Sort from LEAST to MOST harmful
        - Risk levels: LOW, MEDIUM, or HIGH only
        """, category.name());
    }
}