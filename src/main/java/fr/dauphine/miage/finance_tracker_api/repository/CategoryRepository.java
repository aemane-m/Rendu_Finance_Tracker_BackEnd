package fr.dauphine.miage.finance_tracker_api.repository;

import fr.dauphine.miage.finance_tracker_api.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
        select c
        from Category c
        where c.userId = :userId or c.userId is null
        order by c.name asc
    """)
    List<Category> findAllVisibleByUserId(UUID userId);

    @Query("""
        select c
        from Category c
        where c.id = :id
          and (c.userId = :userId or c.userId is null)
    """)
    Optional<Category> findVisibleByIdAndUserId(Long id, UUID userId);

    Optional<Category> findByIdAndUserId(Long id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(UUID userId, String name, Long id);
}