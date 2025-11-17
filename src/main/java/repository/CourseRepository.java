package repository;

import entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity.
 * Provides CRUD operations and custom queries with JOIN FETCH to avoid N+1 problems.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find courses by category ID
     */
    @Query("SELECT c FROM Course c WHERE c.category.id = :categoryId")
    List<Course> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find courses by teacher ID
     */
    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find courses by title containing (case-insensitive search)
     */
    List<Course> findByTitleContainingIgnoreCase(String title);

    /**
     * Find course with modules loaded (avoiding lazy loading exception)
     * IMPORTANT: This demonstrates JOIN FETCH to solve N+1 problem
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.modules WHERE c.id = :id")
    Optional<Course> findByIdWithModules(@Param("id") Long id);

    /**
     * Find course with modules loaded
     * Note: To get full structure with lessons, need a second query due to Hibernate limitation
     */
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.modules m " +
           "WHERE c.id = :id")
    Optional<Course> findByIdWithFullStructure(@Param("id") Long id);

    /**
     * Find all modules with their lessons for a given course
     * Used as second query to load lessons after loading modules
     */
    @Query("SELECT DISTINCT m FROM Module m " +
           "LEFT JOIN FETCH m.lessons " +
           "WHERE m.course.id = :courseId " +
           "ORDER BY m.orderIndex ASC")
    List<entity.Module> findModulesWithLessonsByCourseId(@Param("courseId") Long courseId);

    /**
     * Find course with teacher loaded
     */
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE c.id = :id")
    Optional<Course> findByIdWithTeacher(@Param("id") Long id);

    /**
     * Find course with category loaded
     */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.category WHERE c.id = :id")
    Optional<Course> findByIdWithCategory(@Param("id") Long id);

    /**
     * Find course with tags loaded
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.tags WHERE c.id = :id")
    Optional<Course> findByIdWithTags(@Param("id") Long id);

    /**
     * Find course with enrollments loaded
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.enrollments WHERE c.id = :id")
    Optional<Course> findByIdWithEnrollments(@Param("id") Long id);

    /**
     * Find course with reviews loaded
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.reviews WHERE c.id = :id")
    Optional<Course> findByIdWithReviews(@Param("id") Long id);

    /**
     * Find all courses with their teachers loaded (for listing)
     */
    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.teacher")
    List<Course> findAllWithTeachers();

    /**
     * Find courses by tag name
     */
    @Query("SELECT DISTINCT c FROM Course c JOIN c.tags t WHERE t.name = :tagName")
    List<Course> findByTagName(@Param("tagName") String tagName);

    /**
     * Find courses by tag ID
     */
    @Query("SELECT DISTINCT c FROM Course c JOIN c.tags t WHERE t.id = :tagId")
    List<Course> findByTagId(@Param("tagId") Long tagId);

    /**
     * Count courses by teacher
     */
    long countByTeacherId(Long teacherId);

    /**
     * Count enrolled students in a course
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    long countActiveEnrollments(@Param("courseId") Long courseId);

    /**
     * Find popular courses (with most enrollments)
     */
    @Query("SELECT c FROM Course c " +
           "LEFT JOIN c.enrollments e " +
           "GROUP BY c.id " +
           "ORDER BY COUNT(e) DESC")
    List<Course> findPopularCourses();

    /**
     * Calculate average rating for a course
     */
    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double getAverageRating(@Param("courseId") Long courseId);
}
