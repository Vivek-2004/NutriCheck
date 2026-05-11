package com.nutricheck.controller;

import com.nutricheck.dto.UserRequest;
import com.nutricheck.dto.UserResponse;
import com.nutricheck.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // ✅ Depends on interface, not concrete UserService (DIP)
    private final IUserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createNewUser(@RequestBody UserRequest request) {
        // ✅ No try/catch - GlobalExceptionHandler handles errors
        UserResponse response = userService.createNewUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserResponseById(id);
        return ResponseEntity.ok(response);
    }
}