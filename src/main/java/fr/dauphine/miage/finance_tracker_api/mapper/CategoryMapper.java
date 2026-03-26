package fr.dauphine.miage.finance_tracker_api.mapper;

import fr.dauphine.miage.finance_tracker_api.dto.CategoryResponse;
import fr.dauphine.miage.finance_tracker_api.models.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getIcon(),
                category.getColor(),
                category.isDefault(),
                category.getCreatedAt()
        );
    }
}
