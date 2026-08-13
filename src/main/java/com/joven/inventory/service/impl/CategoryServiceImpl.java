package com.joven.inventory.service.impl;

import com.joven.inventory.common.PageResponse;
import com.joven.inventory.dto.request.CategoryRequest;
import com.joven.inventory.dto.response.CategoryResponse;
import com.joven.inventory.entity.Category;
import com.joven.inventory.exception.DuplicateResourceException;
import com.joven.inventory.exception.ResourceNotFoundException;
import com.joven.inventory.mapper.CategoryMapper;
import com.joven.inventory.repository.CategoryRepository;
import com.joven.inventory.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link CategoryService} providing category management operations.
 * Handles business logic, validation, and persistence for categories.
 *
 * @author Joven Q. Divinagracia Jr.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(Pageable pageable) {
        log.debug("Fetching all categories with pageable: {}", pageable);
        Page<Category> page = categoryRepository.findAll(pageable);
        return CategoryMapper.toPageResponse(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActive() {
        log.debug("Fetching all active categories");
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        log.debug("Fetching category by id: {}", id);
        Category category = findCategoryById(id);
        return CategoryMapper.toResponse(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating new category with name: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = new Category();
        CategoryMapper.updateEntity(category, request);
        category.setActive(true);

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        return CategoryMapper.toResponse(savedCategory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Updating category with id: {}", id);

        Category category = findCategoryById(id);

        categoryRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Category", "name", request.getName());
                });

        CategoryMapper.updateEntity(category, request);

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());
        return CategoryMapper.toResponse(updatedCategory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoryResponse updateStatus(Long id, boolean active) {
        log.info("Updating category status for id: {} to active: {}", id, active);

        Category category = findCategoryById(id);
        category.setActive(active);

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category status updated successfully for id: {}", updatedCategory.getId());
        return CategoryMapper.toResponse(updatedCategory);
    }

    /**
     * Finds a category by ID or throws {@link ResourceNotFoundException}.
     *
     * @param id the category ID
     * @return the found category entity
     * @throws ResourceNotFoundException if no category exists with the given ID
     */
    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}
