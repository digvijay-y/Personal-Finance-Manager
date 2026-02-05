package com.example.pfm.service;

import com.example.pfm.dto.CategoryListResponse;
import com.example.pfm.dto.CategoryRequest;
import com.example.pfm.dto.CategoryResponse;
import com.example.pfm.entity.Category;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import com.example.pfm.exception.BadRequestException;
import com.example.pfm.exception.ConflictException;
import com.example.pfm.exception.ForbiddenException;
import com.example.pfm.exception.ResourceNotFoundException;
import com.example.pfm.repository.CategoryRepository;
import com.example.pfm.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing transaction categories.
 * Handles default system categories and user-defined custom categories.
 * 
 * @author PFM Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Initializes default categories on application startup.
     * Creates system-wide categories like Salary, Food, Rent, etc. if they don't exist.
     */
    @PostConstruct
    public void initDefaultCategories() {
        // Create default categories if they don't exist
        createDefaultCategoryIfNotExists("Salary", TransactionType.INCOME);
        createDefaultCategoryIfNotExists("Food", TransactionType.EXPENSE);
        createDefaultCategoryIfNotExists("Rent", TransactionType.EXPENSE);
        createDefaultCategoryIfNotExists("Transportation", TransactionType.EXPENSE);
        createDefaultCategoryIfNotExists("Entertainment", TransactionType.EXPENSE);
        createDefaultCategoryIfNotExists("Healthcare", TransactionType.EXPENSE);
        createDefaultCategoryIfNotExists("Utilities", TransactionType.EXPENSE);
    }

    private void createDefaultCategoryIfNotExists(String name, TransactionType type) {
        if (!categoryRepository.existsByNameAndCustomFalse(name)) {
            Category category = new Category();
            category.setName(name);
            category.setType(type);
            category.setCustom(false);
            category.setUser(null);
            categoryRepository.save(category);
        }
    }

    /**
     * Retrieves all categories available to a user.
     * Includes both system default categories and user's custom categories.
     * 
     * @param user the user whose categories to retrieve
     * @return CategoryListResponse containing all available categories
     */
    public CategoryListResponse getAllCategories(User user) {
        List<Category> categories = categoryRepository.findAllForUser(user);
        List<CategoryResponse> responses = categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return new CategoryListResponse(responses);
    }

    /**
     * Creates a new custom category for a user.
     * 
     * @param request the category details including name and type (INCOME/EXPENSE)
     * @param user the user creating the category
     * @return CategoryResponse containing the created category details
     * @throws ConflictException if a category with the same name already exists
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        // Check if category name already exists for this user or as a default
        if (categoryRepository.existsByNameAndCustomFalse(request.getName())) {
            throw new ConflictException("Category with this name already exists");
        }
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ConflictException("Category with this name already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setCustom(true);
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    /**
     * Deletes a custom category.
     * 
     * @param name the name of the category to delete
     * @param user the user who owns the category
     * @throws ForbiddenException if attempting to delete a default category
     * @throws ResourceNotFoundException if the category is not found
     * @throws BadRequestException if the category is in use by transactions
     */
    @Transactional
    public void deleteCategory(String name, User user) {
        // Check if it's a default category
        if (categoryRepository.existsByNameAndCustomFalse(name)) {
            throw new ForbiddenException("Cannot delete default categories");
        }

        // Find the custom category
        Category category = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check if category is in use
        if (transactionRepository.existsByUserAndCategory(user, name)) {
            throw new BadRequestException("Cannot delete category that is in use by transactions");
        }

        categoryRepository.delete(category);
    }

    /**
     * Finds a category by name for a specific user.
     * Searches both default categories and user's custom categories.
     * 
     * @param name the category name to search for
     * @param user the user context for searching custom categories
     * @return the Category if found, null otherwise
     */
    public Category findCategoryByNameForUser(String name, User user) {
        return categoryRepository.findByNameForUser(name, user).orElse(null);
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getName(),
                category.getType(),
                category.isCustom()
        );
    }
}
