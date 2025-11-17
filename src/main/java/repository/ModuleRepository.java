package repository;

import entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Module entity.
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    /**
     * Find all modules by course ID (ordered by index)
     */
    @Query("SELECT m FROM Module m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC")
    List<Module> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Find module with lessons loaded
     */
    @Query("SELECT m FROM Module m LEFT JOIN FETCH m.lessons WHERE m.id = :id")
    Optional<Module> findByIdWithLessons(@Param("id") Long id);

    /**
     * Find module with quiz loaded
     */
    @Query("SELECT m FROM Module m LEFT JOIN FETCH m.quiz WHERE m.id = :id")
    Optional<Module> findByIdWithQuiz(@Param("id") Long id);

    /**
     * Find module with course loaded
     */
    @Query("SELECT m FROM Module m JOIN FETCH m.course WHERE m.id = :id")
    Optional<Module> findByIdWithCourse(@Param("id") Long id);

    /**
     * Count modules in a course
     */
    long countByCourseId(Long courseId);
}
