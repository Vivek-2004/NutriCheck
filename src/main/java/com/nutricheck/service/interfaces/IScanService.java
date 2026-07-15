package com.nutricheck.service.interfaces;

import com.nutricheck.dto.ScanResponse;
import java.util.List;

public interface IScanService {
    ScanResponse processTextScan(String ingredients);
    ScanResponse processImageScan(byte[] imageBytes, String contentType);
    List<ScanResponse> getScanHistory();
}