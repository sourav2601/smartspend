package com.smartspend.backend.dto;

import com.smartspend.backend.entity.User;
import jakarta.validation.constraints.PositiveOrZero;

public class UserDtos {

    public record UserResponse(
            Long id,
            String name,
            String email,
            Double monthlyIncome
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getMonthlyIncome());
        }
    }

    public record UpdateIncomeRequest(
            @PositiveOrZero Double monthlyIncome
    ) {}
}
