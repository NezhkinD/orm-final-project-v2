package repository;

import entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Quiz entity.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * Find quiz by module ID
     */
    @Query("SELECT q FROM Quiz q WHERE q.module.id = :moduleId")
    Optional<Quiz> findByModuleId(@Param("moduleId") Long moduleId);

    /**
     * Find quiz with questions loaded
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Find quiz with questions loaded (step 1 of full structure load)
     * Note: To get full structure with options, need a second query due to  Hibernate limitation
     * with multiple collection fetches
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions qn " +
           "WHERE q.id = :id")
    Optional<Quiz> findByIdWithFullStructure(@Param("id") Long id);

    /**
     * Find all questions with their options for a given quiz
     * Used as second query to load answer options after loading questions
     */
    @Query("SELECT DISTINCT qn FROM Question qn " +
           "LEFT JOIN FETCH qn.options " +
           "WHERE qn.quiz.id = :quizId")
    List<entity.Question> findQuestionsWithOptionsByQuizId(@Param("quizId") Long quizId);

    /**
     * Find quiz with submissions loaded
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.submissions WHERE q.id = :id")
    Optional<Quiz> findByIdWithSubmissions(@Param("id") Long id);

    /**
     * Find quiz with module loaded
     */
    @Query("SELECT q FROM Quiz q JOIN FETCH q.module WHERE q.id = :id")
    Optional<Quiz> findByIdWithModule(@Param("id") Long id);

    /**
     * Find quizzes by course ID (through module)
     */
    @Query("SELECT q FROM Quiz q WHERE q.module.course.id = :courseId")
    List<Quiz> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Count quizzes in a course
     */
    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.module.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);
}
