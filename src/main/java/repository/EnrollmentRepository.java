package repository;

import org.example.learningplatform.entity.Enrollment;
import org.example.learningplatform.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Enrollment entity.
 * Manages Many-to-Many relationship between User and Course.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments by student ID
     */
    List<Enrollment> findByStudentId(Long studentId);

    /**
     * Find all enrollments by course ID
     */
    List<Enrollment> findByCourseId(Long courseId);

    /**
     * Find enrollment by student and course (should be unique)
     */
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * Check if enrollment exists for student and course
     */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * Find enrollments by status
     */
    List<Enrollment> findByStatus(EnrollmentStatus status);

    /**
     * Find active enrollments by student ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find active enrollments by course ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);

    /**
     * Find completed enrollments by student ID
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Find enrollment with student loaded
     */
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student WHERE e.id = :id")
    Optional<Enrollment> findByIdWithStudent(@Param("id") Long id);

    /**
     * Find enrollment with course loaded
     */
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.id = :id")
    Optional<Enrollment> findByIdWithCourse(@Param("id") Long id);

    /**
     * Find enrollment with both student and course loaded
     */
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course WHERE e.id = :id")
    Optional<Enrollment> findByIdWithStudentAndCourse(@Param("id") Long id);

    /**
     * Count enrollments by student
     */
    long countByStudentId(Long studentId);

    /**
     * Count active enrollments by student
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    /**
     * Count enrollments by course
     */
    long countByCourseId(Long courseId);

    /**
     * Count enrollments by status
     */
    long countByStatus(EnrollmentStatus status);

    /**
     * Count students enrolled in courses by teacher
     */
    @Query("SELECT COUNT(DISTINCT e.student) FROM Enrollment e WHERE e.course.teacher.id = :teacherId")
    long countStudentsByTeacherId(@Param("teacherId") Long teacherId);
}
