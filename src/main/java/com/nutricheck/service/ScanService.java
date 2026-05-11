package com.nutricheck.service;

import com.nutricheck.dto.ScanResponse;
import com.nutricheck.entity.Scan;
import com.nutricheck.entity.ScanResult;
import com.nutricheck.entity.User;
import com.nutricheck.exceptions.ScanNotFoundException;
import com.nutricheck.mapper.interfaces.IScanMapper;
import com.nutricheck.repository.ScanRepository;
import com.nutricheck.repository.ScanResultRepository;
import com.nutricheck.service.interfaces.IScanReader;
import com.nutricheck.service.interfaces.IScanWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService implements IScanReader, IScanWriter {

    private final ScanRepository scanRepository;
    private final ScanResultRepository scanResultRepository;
    private final IScanMapper scanMapper; // ✅ Uses mapper interface (DIP)

    // ============ IScanReader ============

    @Override
    public ScanResponse getScanById(Long scanId) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new ScanNotFoundException(
                        "Scan not found with id: " + scanId));

        List<ScanResult> results = scanResultRepository.findByScanId(scanId);

        // ✅ Mapping delegated to ScanMapper (SRP)
        return scanMapper.toScanResponse(scan, results);
    }

    @Override
    public List<ScanResponse> getScansByUserId(Long userId) {
        List<Scan> scans = scanRepository.findByUserId(userId);

        return scans.stream()
                .map(scan -> {
                    List<ScanResult> results = scanResultRepository
                            .findByScanId(scan.getId());
                    return scanMapper.toScanResponse(scan, results);
                })
                .collect(Collectors.toList());
    }

    // ============ IScanWriter ============

    @Override
    @Transactional
    public Scan createScan(String productName, User user) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Scan scan = Scan.builder()
                .productName(productName)
                .scannedAt(LocalDateTime.now())
                .user(user)
                .build();

        Scan saved = scanRepository.save(scan);
        log.info("Created scan ID: {} for product: {}", saved.getId(), productName);
        return saved;
    }
}