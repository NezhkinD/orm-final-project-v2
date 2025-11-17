package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * QuizSubmission entity representing a student's attempt at taking a quiz.
 * Stores the score and completion time.
 */
@Entity
@Table(
        name = "quiz_submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many submissions belong to one quiz
    @NotNull(message = "Quiz is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Many-to-One: Many submissions belong to one student
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Integer score; // Score achieved (can be points or percentage)

    @Column
    private Integer totalQuestions; // Total number of questions in the quiz

    @Column
    private Integer correctAnswers; // Number of correct answers

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime takenAt;

    @Column
    private Integer timeSpentMinutes; // Time taken to complete the quiz

    /**
     * Calculate percentage score
     */
    public double getPercentageScore() {
        if (totalQuestions == null || totalQuestions == 0) {
            return 0.0;
        }
        return (correctAnswers * 100.0) / totalQuestions;
    }

    /**
     * Check if the student passed the quiz
     */
    public boolean isPassed(Integer passingScore) {
        if (passingScore == null) {
            return true; // No passing score requirement
        }
        return score >= passingScore;
    }

    @Override
    public String toString() {
        return "QuizSubmission{" +
                "id=" + id +
                ", score=" + score +
                ", correctAnswers=" + correctAnswers +
                "/" + totalQuestions +
                ", takenAt=" + takenAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuizSubmission)) return false;
        QuizSubmission that = (QuizSubmission) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
