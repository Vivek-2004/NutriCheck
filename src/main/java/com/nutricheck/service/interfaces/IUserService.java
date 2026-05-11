package com.nutricheck.service.interfaces;

import com.nutricheck.dto.UserRequest;
import com.nutricheck.dto.UserResponse;
import com.nutricheck.entity.User;

public interface IUserService {
    UserResponse createNewUser(UserRequest request);

    // For internal use by other services (returns Entity)
    User getUserById(Long id);

    // For API responses by ID (returns DTO)
    UserResponse getUserResponseById(Long id);
}