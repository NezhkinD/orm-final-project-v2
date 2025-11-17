package repository;

import org.example.learningplatform.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Assignment entity.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Find all assignments by lesson ID
     */
    List<Assignment> findByLessonId(Long lessonId);

    /**
     * Find assignment with submissions loaded
     */
    @Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.submissions WHERE a.id = :id")
    Optional<Assignment> findByIdWithSubmissions(@Param("id") Long id);

    /**
     * Find assignment with lesson loaded
     */
    @Query("SELECT a FROM Assignment a JOIN FETCH a.lesson WHERE a.id = :id")
    Optional<Assignment> findByIdWithLesson(@Param("id") Long id);

    /**
     * Find assignments by course ID (through lesson and module)
     */
    @Query("SELECT a FROM Assignment a WHERE a.lesson.module.course.id = :courseId")
    List<Assignment> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Find assignments with due date before a specific date
     */
    List<Assignment> findByDueDateBefore(LocalDateTime dueDate);

    /**
     * Find assignments with due date after a specific date
     */
    List<Assignment> findByDueDateAfter(LocalDateTime dueDate);

    /**
     * Find overdue assignments (due date in the past)
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate < :now")
    List<Assignment> findOverdueAssignments(@Param("now") LocalDateTime now);

    /**
     * Count assignments in a lesson
     */
    long countByLessonId(Long lessonId);
}
