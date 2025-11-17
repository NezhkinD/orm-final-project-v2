package repository;

import org.example.learningplatform.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Tag entity.
 * Provides CRUD operations and custom queries for course tags.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find tag by name (unique field)
     */
    Optional<Tag> findByName(String name);

    /**
     * Check if tag exists by name
     */
    boolean existsByName(String name);

    /**
     * Find tags by name containing (case-insensitive search)
     */
    List<Tag> findByNameContainingIgnoreCase(String name);

    /**
     * Find tag with all courses loaded
     */
    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.courses WHERE t.id = :id")
    Optional<Tag> findByIdWithCourses(@Param("id") Long id);

    /**
     * Find all tags used in at least one course
     */
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.courses")
    List<Tag> findTagsInUse();

    /**
     * Count courses using this tag
     */
    @Query("SELECT COUNT(c) FROM Course c JOIN c.tags t WHERE t.id = :tagId")
    long countCoursesByTag(@Param("tagId") Long tagId);
}
