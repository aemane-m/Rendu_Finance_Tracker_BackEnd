package fr.dauphine.miage.finance_tracker_api.services;

import fr.dauphine.miage.finance_tracker_api.dto.CategoryResponse;
import fr.dauphine.miage.finance_tracker_api.dto.CreationCategoryRequest;
import fr.dauphine.miage.finance_tracker_api.dto.UpdateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> findAll(UUID userId);
    CategoryResponse findById(Long id, UUID userId);
    CategoryResponse create(CreationCategoryRequest request, UUID userId);
    CategoryResponse update(Long id, UpdateCategoryRequest request, UUID userId);
    void delete(Long id, UUID userId);
}
