package repository;

import org.example.learningplatform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Submission entity.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Find all submissions by assignment ID
     */
    List<Submission> findByAssignmentId(Long assignmentId);

    /**
     * Find all submissions by student ID
     */
    List<Submission> findByStudentId(Long studentId);

    /**
     * Find submission by assignment and student (should be unique)
     */
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * Check if submission exists for assignment and student
     */
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    /**
     * Find submission with assignment loaded
     */
    @Query("SELECT s FROM Submission s JOIN FETCH s.assignment WHERE s.id = :id")
    Optional<Submission> findByIdWithAssignment(@Param("id") Long id);

    /**
     * Find submission with student loaded
     */
    @Query("SELECT s FROM Submission s JOIN FETCH s.student WHERE s.id = :id")
    Optional<Submission> findByIdWithStudent(@Param("id") Long id);

    /**
     * Find all graded submissions
     */
    @Query("SELECT s FROM Submission s WHERE s.score IS NOT NULL")
    List<Submission> findGradedSubmissions();

    /**
     * Find all ungraded submissions
     */
    @Query("SELECT s FROM Submission s WHERE s.score IS NULL")
    List<Submission> findUngradedSubmissions();

    /**
     * Find ungraded submissions for a specific assignment
     */
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NULL")
    List<Submission> findUngradedSubmissionsByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * Find submissions by course ID (through assignment, lesson, module)
     */
    @Query("SELECT s FROM Submission s WHERE s.assignment.lesson.module.course.id = :courseId")
    List<Submission> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Find student's submissions for a specific course
     */
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId " +
           "AND s.assignment.lesson.module.course.id = :courseId")
    List<Submission> findByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                                  @Param("courseId") Long courseId);

    /**
     * Calculate average score for a student
     */
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.student.id = :studentId AND s.score IS NOT NULL")
    Double getAverageScoreByStudent(@Param("studentId") Long studentId);

    /**
     * Count submissions by student
     */
    long countByStudentId(Long studentId);

    /**
     * Count graded submissions by student
     */
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId AND s.score IS NOT NULL")
    long countGradedSubmissionsByStudent(@Param("studentId") Long studentId);
}
