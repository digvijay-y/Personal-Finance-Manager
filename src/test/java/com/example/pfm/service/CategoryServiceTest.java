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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setType(TransactionType.INCOME);
        defaultCategory.setCustom(false);
        defaultCategory.setUser(null);

        customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("Freelance");
        customCategory.setType(TransactionType.INCOME);
        customCategory.setCustom(true);
        customCategory.setUser(user);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("NewCategory");
        categoryRequest.setType(TransactionType.INCOME);
    }

    @Test
    @DisplayName("Should get all categories for user")
    void getAllCategories_Success() {
        List<Category> categories = Arrays.asList(defaultCategory, customCategory);
        when(categoryRepository.findAllForUser(user)).thenReturn(categories);

        CategoryListResponse response = categoryService.getAllCategories(user);

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
    }

    @Test
    @DisplayName("Should create custom category successfully")
    void createCategory_Success() {
        when(categoryRepository.existsByNameAndCustomFalse(anyString())).thenReturn(false);
        when(categoryRepository.existsByNameAndUser(anyString(), any(User.class))).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(categoryRequest, user);

        assertNotNull(response);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when category name exists as default")
    void createCategory_DefaultExists_ThrowsConflict() {
        when(categoryRepository.existsByNameAndCustomFalse("Salary")).thenReturn(true);
        categoryRequest.setName("Salary");

        assertThrows(ConflictException.class, 
                () -> categoryService.createCategory(categoryRequest, user));
    }

    @Test
    @DisplayName("Should throw ConflictException when custom category name exists for user")
    void createCategory_CustomExists_ThrowsConflict() {
        when(categoryRepository.existsByNameAndCustomFalse(anyString())).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(true);
        categoryRequest.setName("Freelance");

        assertThrows(ConflictException.class, 
                () -> categoryService.createCategory(categoryRequest, user));
    }

    @Test
    @DisplayName("Should delete custom category successfully")
    void deleteCategory_Success() {
        when(categoryRepository.existsByNameAndCustomFalse("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByUserAndCategory(user, "Freelance")).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory("Freelance", user));
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when deleting default category")
    void deleteCategory_DefaultCategory_ThrowsForbidden() {
        when(categoryRepository.existsByNameAndCustomFalse("Salary")).thenReturn(true);

        assertThrows(ForbiddenException.class, 
                () -> categoryService.deleteCategory("Salary", user));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category not found")
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.existsByNameAndCustomFalse("Unknown")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Unknown", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> categoryService.deleteCategory("Unknown", user));
    }

    @Test
    @DisplayName("Should throw BadRequestException when category is in use")
    void deleteCategory_InUse_ThrowsException() {
        when(categoryRepository.existsByNameAndCustomFalse("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByUserAndCategory(user, "Freelance")).thenReturn(true);

        assertThrows(BadRequestException.class, 
                () -> categoryService.deleteCategory("Freelance", user));
    }

    @Test
    @DisplayName("Should find category by name for user")
    void findCategoryByNameForUser_Success() {
        when(categoryRepository.findByNameForUser("Salary", user)).thenReturn(Optional.of(defaultCategory));

        Category result = categoryService.findCategoryByNameForUser("Salary", user);

        assertNotNull(result);
        assertEquals("Salary", result.getName());
    }

    @Test
    @DisplayName("Should return null when category not found")
    void findCategoryByNameForUser_NotFound_ReturnsNull() {
        when(categoryRepository.findByNameForUser("Unknown", user)).thenReturn(Optional.empty());

        Category result = categoryService.findCategoryByNameForUser("Unknown", user);

        assertNull(result);
    }
}
