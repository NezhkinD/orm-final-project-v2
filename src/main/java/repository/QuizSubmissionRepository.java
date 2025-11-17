package repository;

import org.example.learningplatform.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for QuizSubmission entity.
 */
@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    /**
     * Find all submissions by quiz ID
     */
    List<QuizSubmission> findByQuizId(Long quizId);

    /**
     * Find all submissions by student ID
     */
    List<QuizSubmission> findByStudentId(Long studentId);

    /**
     * Find submission by quiz and student (should be unique)
     */
    Optional<QuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);

    /**
     * Check if submission exists for quiz and student
     */
    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);

    /**
     * Find submission with quiz loaded
     */
    @Query("SELECT qs FROM QuizSubmission qs JOIN FETCH qs.quiz WHERE qs.id = :id")
    Optional<QuizSubmission> findByIdWithQuiz(@Param("id") Long id);

    /**
     * Find submission with student loaded
     */
    @Query("SELECT qs FROM QuizSubmission qs JOIN FETCH qs.student WHERE qs.id = :id")
    Optional<QuizSubmission> findByIdWithStudent(@Param("id") Long id);

    /**
     * Find submissions by course ID (through quiz and module)
     */
    @Query("SELECT qs FROM QuizSubmission qs WHERE qs.quiz.module.course.id = :courseId")
    List<QuizSubmission> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Find student's submissions for a specific course
     */
    @Query("SELECT qs FROM QuizSubmission qs WHERE qs.student.id = :studentId " +
           "AND qs.quiz.module.course.id = :courseId")
    List<QuizSubmission> findByStudentIdAndCourseId(@Param("studentId") Long studentId,
                                                      @Param("courseId") Long courseId);

    /**
     * Find submissions that passed (score >= passing score)
     */
    @Query("SELECT qs FROM QuizSubmission qs WHERE qs.quiz.id = :quizId " +
           "AND qs.score >= qs.quiz.passingScore")
    List<QuizSubmission> findPassedSubmissionsByQuizId(@Param("quizId") Long quizId);

    /**
     * Calculate average score for a quiz
     */
    @Query("SELECT AVG(qs.score) FROM QuizSubmission qs WHERE qs.quiz.id = :quizId")
    Double getAverageScoreByQuiz(@Param("quizId") Long quizId);

    /**
     * Calculate average score for a student
     */
    @Query("SELECT AVG(qs.score) FROM QuizSubmission qs WHERE qs.student.id = :studentId")
    Double getAverageScoreByStudent(@Param("studentId") Long studentId);

    /**
     * Count submissions by student
     */
    long countByStudentId(Long studentId);

    /**
     * Count passed submissions for a student
     */
    @Query("SELECT COUNT(qs) FROM QuizSubmission qs WHERE qs.student.id = :studentId " +
           "AND qs.score >= qs.quiz.passingScore")
    long countPassedSubmissionsByStudent(@Param("studentId") Long studentId);
}
