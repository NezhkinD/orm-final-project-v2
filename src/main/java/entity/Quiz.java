package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.lang.Module;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz entity representing a test/exam for a module.
 * Contains multiple questions to assess student knowledge.
 */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Quiz title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Integer timeLimit; // Time limit in minutes

    @Column
    private Integer passingScore; // Minimum score to pass (percentage or points)

    // One-to-One: One module has one quiz (optional)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", unique = true)
    private Module module;

    // One-to-Many: One quiz has many questions
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // One-to-Many: One quiz has many submissions
    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizSubmission> submissions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods

    /**
     * Add a question to this quiz
     */
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    /**
     * Remove a question from this quiz
     */
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }

    /**
     * Add a submission to this quiz
     */
    public void addSubmission(QuizSubmission submission) {
        submissions.add(submission);
        submission.setQuiz(this);
    }

    /**
     * Get total possible points for this quiz
     */
    public int getTotalPoints() {
        return questions.size(); // Assuming each question is worth 1 point
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", timeLimit=" + timeLimit +
                ", questionCount=" + (questions != null ? questions.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quiz)) return false;
        Quiz quiz = (Quiz) o;
        return id != null && id.equals(quiz.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
