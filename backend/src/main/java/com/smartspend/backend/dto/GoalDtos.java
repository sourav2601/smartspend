package com.smartspend.backend.dto;

import com.smartspend.backend.entity.Goal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Map;

public class GoalDtos {

    public record CreateGoalRequest(
            @NotBlank String title,
            @Positive Double targetAmount,
            // Optional - if omitted, the AI plan aims for "as soon as reasonably possible"
            LocalDate targetDate
    ) {}

    public record GoalResponse(
            Long id,
            String title,
            Double targetAmount,
            LocalDate targetDate,
            Double currentSaved,
            Map<String, Object> plan
    ) {
        public static GoalResponse from(Goal g) {
            return new GoalResponse(
                    g.getId(), g.getTitle(), g.getTargetAmount(),
                    g.getTargetDate(), g.getCurrentSaved(), g.getPlan()
            );
        }
    }
}
