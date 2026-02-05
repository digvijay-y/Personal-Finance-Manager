package com.example.pfm.controller;

import com.example.pfm.dto.*;
import com.example.pfm.entity.User;
import com.example.pfm.service.AuthService;
import com.example.pfm.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for goal operations.
 * Provides endpoints for creating, listing, updating, and deleting financial goals.
 * 
 * @author PFM Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;

    /**
     * Creates a new financial goal.
     * 
     * @param request the goal details
     * @return GoalResponse with created goal and progress (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.createGoal(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all goals for the current user.
     * 
     * @return GoalListResponse with all goals and progress (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals() {
        User user = authService.getCurrentUser();
        GoalListResponse response = goalService.getAllGoals(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific goal by ID.
     * 
     * @param id the goal ID
     * @return GoalResponse with goal details and progress (HTTP 200)
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.getGoal(id, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing goal.
     * 
     * @param id the goal ID
     * @param request the update details
     * @return GoalResponse with updated goal and progress (HTTP 200)
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @RequestBody GoalUpdateRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a goal.
     * 
     * @param id the goal ID to delete
     * @return MessageResponse confirming deletion (HTTP 200)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        goalService.deleteGoal(id, user);
        return ResponseEntity.ok(new MessageResponse("Goal deleted successfully"));
    }
}
