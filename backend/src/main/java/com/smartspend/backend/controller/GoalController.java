package com.smartspend.backend.controller;

import com.smartspend.backend.dto.GoalDtos;
import com.smartspend.backend.security.CurrentUser;
import com.smartspend.backend.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Creates a new goal AND immediately generates its first AI savings
     * plan in one call - this is the main "wow" endpoint of the app.
     */
    @PostMapping
    public ResponseEntity<GoalDtos.GoalResponse> create(@Valid @RequestBody GoalDtos.CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoalWithPlan(CurrentUser.id(), request));
    }

    /**
     * Recomputes the plan for an existing goal - useful after the user
     * has logged a month of new expenses and wants an updated plan.
     */
    @PostMapping("/{id}/regenerate-plan")
    public ResponseEntity<GoalDtos.GoalResponse> regeneratePlan(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.regeneratePlan(CurrentUser.id(), id));
    }

    @GetMapping
    public ResponseEntity<List<GoalDtos.GoalResponse>> listAll() {
        return ResponseEntity.ok(goalService.listGoals(CurrentUser.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        goalService.deleteGoal(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }
}
