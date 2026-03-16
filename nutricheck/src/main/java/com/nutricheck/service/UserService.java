package com.nutricheck.service;

import com.nutricheck.dto.UserRequest;
import com.nutricheck.dto.UserResponse;
import com.nutricheck.entity.Scan;
import com.nutricheck.entity.User;
import com.nutricheck.repository.ScanRepository;
import com.nutricheck.repository.ScanResultRepository;
import com.nutricheck.repository.UserRepository;
import com.nutricheck.service.interfaces.IUserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final ScanRepository scanRepository;
    private final ScanResultRepository scanResultRepository;
    private final ModelMapper modelMapper;

    public UserResponse createNewUser(UserRequest request) {
        User user = modelMapper.map(request, User.class);
        user = userRepository.save(user);
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public User getUserById(Long id) {
        // This is what OcrService calls to get the actual User entity
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public UserResponse getUserResponseById(Long id) {
        // This is used for API responses (DTOs)
        User user = getUserById(id);
        return modelMapper.map(user, UserResponse.class);
    }

    public UserResponse updateUser(Long userId, UserRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail());

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserResponse.class);
    }

    @Transactional
    public void deleteUserAndHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Scan> userScans = scanRepository.findByUserId(userId);

        for (Scan scan : userScans) {
            scanResultRepository.deleteByScanId(scan.getId());
        }

        scanRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
}
