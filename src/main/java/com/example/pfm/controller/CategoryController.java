package com.example.pfm.controller;

import com.example.pfm.dto.CategoryListResponse;
import com.example.pfm.dto.CategoryRequest;
import com.example.pfm.dto.CategoryResponse;
import com.example.pfm.dto.MessageResponse;
import com.example.pfm.entity.User;
import com.example.pfm.service.AuthService;
import com.example.pfm.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for category operations.
 * Provides endpoints for listing, creating, and deleting categories.
 * 
 * @author PFM Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    /**
     * Retrieves all categories available to the current user.
     * 
     * @return CategoryListResponse with all categories (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories() {
        User user = authService.getCurrentUser();
        CategoryListResponse response = categoryService.getAllCategories(user);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new custom category.
     * 
     * @param request the category details
     * @return CategoryResponse with created category (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        User user = authService.getCurrentUser();
        CategoryResponse response = categoryService.createCategory(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a custom category.
     * 
     * @param name the category name to delete
     * @return MessageResponse confirming deletion (HTTP 200)
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        User user = authService.getCurrentUser();
        categoryService.deleteCategory(name, user);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
