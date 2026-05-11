package com.nutricheck.service.interfaces;

import com.nutricheck.dto.ScanResponse;
import java.util.List;

public interface IScanReader {

    ScanResponse getScanById(Long scanId);

    List<ScanResponse> getScansByUserId(Long userId);
}