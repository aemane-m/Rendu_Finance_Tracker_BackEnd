package fr.dauphine.miage.finance_tracker_api.services.impl;

import fr.dauphine.miage.finance_tracker_api.dto.CategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCategoryRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCategoryRequest;
import fr.dauphine.miage.finance_tracker_api.exceptions.BadRequestException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ConflictException;
import fr.dauphine.miage.finance_tracker_api.exceptions.ResourceNotFoundException;
import fr.dauphine.miage.finance_tracker_api.mapper.CategoryMapper;
import fr.dauphine.miage.finance_tracker_api.models.Category;
import fr.dauphine.miage.finance_tracker_api.repository.CategoryRepository;
import fr.dauphine.miage.finance_tracker_api.services.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                                CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryResponse> findAll(UUID userId) {
        return categoryRepository.findAllVisibleByUserId(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findById(Long id, UUID userId) {
        Category category = categoryRepository.findVisibleByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(CreationCategoryRequest request, UUID userId) {
        String normalizedName = request.name().trim();

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, normalizedName)) {
            throw new ConflictException("A category with this name already exists");
        }

        Category category = Category.builder()
                .userId(userId)
                .name(normalizedName)
                .icon(normalizeOptional(request.icon()))
                .color(normalizeOptional(request.color()))
                .isDefault(false)
                .createdAt(Instant.now())
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request, UUID userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (category.isDefault()) {
            throw new BadRequestException("Default categories cannot be updated");
        }

        String normalizedName = request.name().trim();

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, normalizedName, id)) {
            throw new ConflictException("A category with this name already exists");
        }

        category.setName(normalizedName);
        category.setIcon(normalizeOptional(request.icon()));
        category.setColor(normalizeOptional(request.color()));

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id, UUID userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (category.isDefault()) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Category is still used by a budget or transaction");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
