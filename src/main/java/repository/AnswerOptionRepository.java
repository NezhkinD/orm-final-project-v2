package repository;

import entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AnswerOption entity.
 */
@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    /**
     * Find all answer options by question ID (ordered by index)
     */
    @Query("SELECT a FROM AnswerOption a WHERE a.question.id = :questionId ORDER BY a.orderIndex ASC")
    List<AnswerOption> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * Find correct answer options for a question
     */
    @Query("SELECT a FROM AnswerOption a WHERE a.question.id = :questionId AND a.isCorrect = true")
    List<AnswerOption> findCorrectAnswersByQuestionId(@Param("questionId") Long questionId);

    /**
     * Count answer options for a question
     */
    long countByQuestionId(Long questionId);

    /**
     * Count correct answers for a question
     */
    @Query("SELECT COUNT(a) FROM AnswerOption a WHERE a.question.id = :questionId AND a.isCorrect = true")
    long countCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
}
