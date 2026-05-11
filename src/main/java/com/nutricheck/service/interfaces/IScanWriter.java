package com.nutricheck.service.interfaces;

import com.nutricheck.entity.Scan;
import com.nutricheck.entity.User;

public interface IScanWriter {
    Scan createScan(String productName, User user);
}