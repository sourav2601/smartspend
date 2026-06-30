package com.smartspend.backend.service;

import com.smartspend.backend.dto.UserDtos;
import com.smartspend.backend.entity.User;
import com.smartspend.backend.exception.ResourceNotFoundException;
import com.smartspend.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDtos.UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserDtos.UserResponse.from(user);
    }

    @Transactional
    public UserDtos.UserResponse updateIncome(Long userId, UserDtos.UpdateIncomeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setMonthlyIncome(request.monthlyIncome());
        return UserDtos.UserResponse.from(user);
    }
}
