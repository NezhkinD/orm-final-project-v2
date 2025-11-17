package repository;

import entity.Question;
import entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Question entity.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all questions by quiz ID (ordered by index)
     */
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId ORDER BY q.orderIndex ASC")
    List<Question> findByQuizId(@Param("quizId") Long quizId);

    /**
     * Find questions by type
     */
    List<Question> findByType(QuestionType type);

    /**
     * Find question with answer options loaded
     */
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);

    /**
     * Find question with quiz loaded
     */
    @Query("SELECT q FROM Question q JOIN FETCH q.quiz WHERE q.id = :id")
    Optional<Question> findByIdWithQuiz(@Param("id") Long id);

    /**
     * Count questions in a quiz
     */
    long countByQuizId(Long quizId);

    /**
     * Count questions by type in a quiz
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId AND q.type = :type")
    long countByQuizIdAndType(@Param("quizId") Long quizId, @Param("type") QuestionType type);
}
