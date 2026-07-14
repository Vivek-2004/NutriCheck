package com.nutricheck.controller;

import com.nutricheck.dto.ScanResponse;
import com.nutricheck.dto.enums.ProductCategory;
import com.nutricheck.entity.Scan;
import com.nutricheck.exceptions.InvalidCategoryException;
import com.nutricheck.service.interfaces.IOcrService;
import com.nutricheck.service.interfaces.IScanReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OcrController {

    private final IOcrService ocrService;
    private final IScanReader scanReader;

    @PostMapping("/image")
    public ResponseEntity<ScanResponse> uploadScan(
            @RequestParam("image") MultipartFile file,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "category", defaultValue = "FOOD") String categoryStr
    ) throws IOException {

        ProductCategory category = parseCategory(categoryStr);
        Scan scan = ocrService.processImageScan(
                file.getBytes(),
                file.getContentType(),
                userId,
                category
        );

        ScanResponse response = scanReader.getScanById(scan.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scanId}")
    public ResponseEntity<ScanResponse> getScan(@PathVariable Long scanId) {
        // ✅ Clean - no try/catch needed
        ScanResponse response = scanReader.getScanById(scanId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScanResponse>> getFullScanHistory() {
        return ResponseEntity.ok(scanReader.getScanHistory());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ScanResponse>> getUserScans(@PathVariable Long userId) {
        List<ScanResponse> scans = scanReader.getScansByUserId(userId);
        return ResponseEntity.ok(scans);
    }

    // ============ Private Helpers ============
    private ProductCategory parseCategory(String categoryStr) {
        try {
            return ProductCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException(
                    "Invalid category: " + categoryStr + ". Must be: FOOD, COSMETICS, or BEVERAGES");
        }
    }
}