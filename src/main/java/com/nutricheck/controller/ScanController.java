package com.nutricheck.controller;

import com.nutricheck.dto.ScanRequest;
import com.nutricheck.dto.ScanResponse;
import com.nutricheck.service.interfaces.IScanService;
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
public class ScanController {

    private final IScanService scanService;

    @PostMapping("/ingredients")
    public ResponseEntity<ScanResponse> analyzeIngredients(
            @RequestBody ScanRequest scanRequest) {

        log.info("Analyzing ingredients");
        ScanResponse response = scanService.processTextScan(scanRequest.getIngredients());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image")
    public ResponseEntity<ScanResponse> uploadScan(
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        log.info("Uploading and processing image scan");
        ScanResponse response = scanService.processImageScan(
                file.getBytes(),
                file.getContentType()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ScanResponse>> getFullScanHistory() {
        log.info("Retrieving all scan history");
        return ResponseEntity.ok(scanService.getScanHistory());
    }
}