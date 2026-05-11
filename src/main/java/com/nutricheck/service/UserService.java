package com.nutricheck.service;

import com.nutricheck.dto.UserRequest;
import com.nutricheck.dto.UserResponse;
import com.nutricheck.entity.User;
import com.nutricheck.exceptions.UserNotFoundException;
import com.nutricheck.repository.UserRepository;
import com.nutricheck.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    // ============ IUserService ============

    @Override
    @Transactional
    public UserResponse createNewUser(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("User request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .build();

        User saved = userRepository.save(user);
        log.info("Created new user: {} (ID: {})", saved.getName(), saved.getId());

        return toUserResponse(saved);
    }

    @Override
    public User getUserById(Long id) {
        // Returns entity for internal use by other services
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));
    }

    @Override
    public UserResponse getUserResponseById(Long id) {
        // Returns DTO for API responses
        User user = getUserById(id);
        return toUserResponse(user);
    }

    // ============ Private Helpers ============

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}