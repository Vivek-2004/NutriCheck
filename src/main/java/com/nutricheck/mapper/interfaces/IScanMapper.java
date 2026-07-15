package com.nutricheck.mapper.interfaces;

import com.nutricheck.dto.ScanResponse;
import com.nutricheck.entity.Scan;

public interface IScanMapper {

    // Converts Scan entity into full ScanResponse DTO
    ScanResponse toScanResponse(Scan scan);
}