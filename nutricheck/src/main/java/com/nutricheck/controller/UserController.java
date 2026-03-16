package com.nutricheck.controller;

import com.nutricheck.dto.UserRequest;
import com.nutricheck.dto.UserResponse;
import com.nutricheck.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createNewUser(@RequestBody UserRequest request){
        return ResponseEntity.ok(userService.createNewUser(request));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserAndHistory(@PathVariable Long userId) {
        userService.deleteUserAndHistory(userId);
        return ResponseEntity.noContent().build();
    }
}