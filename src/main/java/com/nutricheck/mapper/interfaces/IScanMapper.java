package com.nutricheck.mapper.interfaces;

import com.nutricheck.dto.ScanResponse;
import com.nutricheck.dto.ScanResultDto;
import com.nutricheck.dto.ScanSummary;
import com.nutricheck.entity.Scan;
import com.nutricheck.entity.ScanResult;
import java.util.List;

public interface IScanMapper {

    // Converts Scan entity + results into full ScanResponse DTO
    ScanResponse toScanResponse(Scan scan, List<ScanResult> results);

    // Converts single ScanResult entity to DTO
    ScanResultDto toScanResultDto(ScanResult scanResult);

    // Calculates summary statistics from results
    ScanSummary toScanSummary(List<ScanResult> results);
}