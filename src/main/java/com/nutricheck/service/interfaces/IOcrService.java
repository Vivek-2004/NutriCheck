package com.nutricheck.service.interfaces;

import com.nutricheck.dto.enums.ProductCategory;
import com.nutricheck.entity.Scan;

public interface IOcrService {
    Scan processImageScan(
            byte[] imageBytes,
            String contentType,
            Long userId,
            ProductCategory category
    );
}