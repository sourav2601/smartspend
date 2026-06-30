package com.smartspend.backend.controller;

import com.smartspend.backend.dto.UserDtos;
import com.smartspend.backend.security.CurrentUser;
import com.smartspend.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDtos.UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(CurrentUser.id()));
    }

    @PatchMapping("/me/income")
    public ResponseEntity<UserDtos.UserResponse> updateIncome(@Valid @RequestBody UserDtos.UpdateIncomeRequest request) {
        return ResponseEntity.ok(userService.updateIncome(CurrentUser.id(), request));
    }
}
