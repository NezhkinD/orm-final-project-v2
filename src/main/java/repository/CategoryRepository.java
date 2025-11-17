package repository;

import org.example.learningplatform.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category entity.
 * Provides CRUD operations and custom queries for course categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name (unique field)
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);

    /**
     * Find category with all courses loaded
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.courses WHERE c.id = :id")
    Optional<Category> findByIdWithCourses(@Param("id") Long id);

    /**
     * Count courses in a category
     */
    @Query("SELECT COUNT(co) FROM Course co WHERE co.category.id = :categoryId")
    long countCoursesByCategory(@Param("categoryId") Long categoryId);
}
