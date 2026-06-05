package com.inventory.api.service;

import com.inventory.api.dto.request.CategoryRequest;
import com.inventory.api.dto.response.CategoryResponse;
import com.inventory.api.exception.DuplicateResourceException;
import com.inventory.api.exception.ResourceNotFoundException;
import com.inventory.api.mapper.CategoryMapper;
import com.inventory.api.model.Category;
import com.inventory.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for CategoryService.
 *
 * UNIT TESTING CONCEPTS:
 * - We test the SERVICE in isolation from the database.
 * - @Mock creates fake versions of dependencies (repository, mapper).
 * - @InjectMocks creates the real CategoryService and injects the mocks.
 * - We tell mocks what to return (when...thenReturn) and verify they were called.
 *
 * WHY NOT TEST WITH REAL DB?
 * Unit tests should be fast (milliseconds) and independent.
 * DB tests are slow and require setup. We save those for integration tests.
 *
 * TESTING PYRAMID:
 *   Many unit tests (fast, isolated, test one thing)
 *   Some integration tests (test layers together)
 *   Few E2E tests (test the whole stack, slow)
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;
    private CategoryRequest sampleRequest;
    private CategoryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new CategoryRequest();
        sampleRequest.setName("Electronics");
        sampleRequest.setDescription("Electronic products");

        sampleResponse = CategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .active(true)
                .build();
    }

    @Test
    void getCategoryById_WhenExists_ReturnsResponse() {
        // ARRANGE: set up mock behavior
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryMapper.toResponse(sampleCategory)).thenReturn(sampleResponse);

        // ACT: call the method under test
        CategoryResponse result = categoryService.getCategoryById(1L);

        // ASSERT: verify the result
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");

        // Verify mocks were called as expected
        verify(categoryRepository).findById(1L);
        verify(categoryMapper).toResponse(sampleCategory);
    }

    @Test
    void getCategoryById_WhenNotFound_ThrowsNotFoundException() {
        // ARRANGE
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT: expect the exception to be thrown
        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCategory_WithValidRequest_ReturnsCreatedResponse() {
        // ARRANGE
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryMapper.toEntity(sampleRequest)).thenReturn(sampleCategory);
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);
        when(categoryMapper.toResponse(sampleCategory)).thenReturn(sampleResponse);

        // ACT
        CategoryResponse result = categoryService.createCategory(sampleRequest);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithDuplicateName_ThrowsDuplicateException() {
        // ARRANGE: category name already exists
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> categoryService.createCategory(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Electronics");

        // Verify save was never called (we stopped before reaching it)
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void toggleActive_FlipsActiveStatus() {
        // ARRANGE: category is currently active
        sampleCategory.setActive(true);
        Category savedCategory = Category.builder()
                .id(1L).name("Electronics").active(false) // after toggle: false
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        CategoryResponse inactiveResponse = CategoryResponse.builder()
                .id(1L).name("Electronics").active(false).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(inactiveResponse);

        // ACT
        CategoryResponse result = categoryService.toggleActive(1L);

        // ASSERT: active was flipped to false
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deleteCategory_WhenExists_DeletesSuccessfully() {
        // ARRANGE
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        doNothing().when(categoryRepository).delete(sampleCategory);

        // ACT
        categoryService.deleteCategory(1L);

        // ASSERT: delete was called once
        verify(categoryRepository).delete(sampleCategory);
    }
}
