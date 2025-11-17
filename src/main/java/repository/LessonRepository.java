package repository;

import entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Lesson entity.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Find all lessons by module ID (ordered by index)
     */
    @Query("SELECT l FROM Lesson l WHERE l.module.id = :moduleId ORDER BY l.orderIndex ASC")
    List<Lesson> findByModuleId(@Param("moduleId") Long moduleId);

    /**
     * Find lesson with assignments loaded
     */
    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.assignments WHERE l.id = :id")
    Optional<Lesson> findByIdWithAssignments(@Param("id") Long id);

    /**
     * Find lesson with module loaded
     */
    @Query("SELECT l FROM Lesson l JOIN FETCH l.module WHERE l.id = :id")
    Optional<Lesson> findByIdWithModule(@Param("id") Long id);

    /**
     * Find lessons by course ID (through module)
     */
    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId ORDER BY l.module.orderIndex, l.orderIndex")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Count lessons in a module
     */
    long countByModuleId(Long moduleId);
}
