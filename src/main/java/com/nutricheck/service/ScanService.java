package com.nutricheck.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricheck.dto.AiAnalysisResponse;
import com.nutricheck.dto.ScanResponse;
import com.nutricheck.entity.Scan;
import com.nutricheck.exceptions.AiProcessingException;
import com.nutricheck.mapper.interfaces.IScanMapper;
import com.nutricheck.repository.ScanRepository;
import com.nutricheck.service.interfaces.IScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService implements IScanService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ScanRepository scanRepository;
    private final IScanMapper scanMapper;

    // ============ Public IScanService Workflows ============

    @Override
    @Transactional
    public ScanResponse processTextScan(String ingredients) {
        // Step 1: Perform AI analysis
        AiAnalysisResponse aiResponse = this.analyzeText(ingredients);

        // Step 2: Serialize response to JSON
        String aiResponseJson = serializeToJson(aiResponse);

        // Step 3: Save to database
        Scan scan = this.createScan(aiResponse.getProductName(), aiResponseJson);

        // Step 4: Map and return DTO
        return scanMapper.toScanResponse(scan);
    }

    @Override
    @Transactional
    public ScanResponse processImageScan(byte[] imageBytes, String contentType) {
        // Step 1: Get AI analysis
        AiAnalysisResponse aiResponse = this.analyzeImage(imageBytes, contentType);

        log.info("AI Analysis completed - Product: {}, Ingredients: {}",
                aiResponse.getProductName(),
                aiResponse.getResults() != null ? aiResponse.getResults().size() : 0);

        // Step 2: Serialize response to JSON
        String aiResponseJson = serializeToJson(aiResponse);

        // Step 3: Create scan
        Scan scan = this.createScan(aiResponse.getProductName(), aiResponseJson);

        // Step 4: Map and return DTO
        return scanMapper.toScanResponse(scan);
    }

    @Override
    public List<ScanResponse> getScanHistory() {
        List<Scan> allScans = scanRepository.findAll();
        return allScans.stream()
                .map(scanMapper::toScanResponse)
                .collect(Collectors.toList());
    }

    // ============ Private Service Methods ============

    private AiAnalysisResponse analyzeText(String ingredients) {
        if (ingredients == null || ingredients.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be empty");
        }

        String prompt = buildTextPrompt(ingredients);
        log.info("Calling Gemini for text analysis");

        try {
            String response = chatModel.call(prompt);
            return parseAiResponse(response);
        } catch (Exception e) {
            log.error("Text analysis failed: {}", e.getMessage(), e);
            throw new AiProcessingException("Failed to analyze ingredients: " + e.getMessage(), e);
        }
    }

    private AiAnalysisResponse analyzeImage(byte[] imageBytes, String mimeType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image data cannot be empty");
        }

        String promptText = buildImagePrompt();

        var media = new Media(MimeTypeUtils.parseMimeType(mimeType),
                new ByteArrayResource(imageBytes));

        var userMessage = UserMessage.builder()
                .text(promptText)
                .media(List.of(media))
                .build();

        log.info("Calling Gemini for image analysis");

        try {
            ChatResponse response = chatModel.call(new Prompt(userMessage));
            String resultJson = response.getResult().getOutput().getText();
            return parseAiResponse(resultJson);
        } catch (Exception e) {
            log.error("Image analysis failed: {}", e.getMessage(), e);
            throw new AiProcessingException("Failed to analyze image: " + e.getMessage(), e);
        }
    }

    private Scan createScan(String productName, String aiAnalysisResponseJson) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        Scan scan = Scan.builder()
                .productName(productName)
                .scannedAt(LocalDateTime.now())
                .aiAnalysisResponse(aiAnalysisResponseJson)
                .build();

        Scan saved = scanRepository.save(scan);
        log.info("Created scan ID: {} for product: {}", saved.getId(), productName);
        return saved;
    }

    private String serializeToJson(AiAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed to serialize AI analysis response for product: {}", response.getProductName(), e);
            return "";
        }
    }

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

    private String buildTextPrompt(String ingredientList) {
        return String.format("""
        You are a Nutritionist and Product Safety Expert.

        Ingredients:
        %s

        Analyze each ingredient and respond with ONLY valid JSON. No markdown, no extra text.

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
        - Predict/infer the name of the product or food item from the provided ingredient list, and return it in the "productName" field.
        - Sort ingredients from LEAST to MOST harmful
        - Be factual and concise
        - Risk levels: LOW, MEDIUM, or HIGH only
        """, ingredientList);
    }

    private String buildImagePrompt() {
        return """
        You are a Nutritionist and Product Safety Expert.

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
        """;
    }
}