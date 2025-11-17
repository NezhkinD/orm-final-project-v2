package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Question entity representing a quiz question.
 * Contains the question text and type, with multiple answer options.
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "Question type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Column
    private Integer orderIndex; // Order within the quiz

    @Column
    private Integer points; // Points for this question (default 1)

    // Many-to-One: Many questions belong to one quiz
    @NotNull(message = "Quiz is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // One-to-Many: One question has many answer options
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnswerOption> options = new ArrayList<>();

    // Helper methods

    /**
     * Add an answer option to this question
     */
    public void addOption(AnswerOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    /**
     * Remove an answer option from this question
     */
    public void removeOption(AnswerOption option) {
        options.remove(option);
        option.setQuestion(null);
    }

    /**
     * Get all correct answer options
     */
    public List<AnswerOption> getCorrectOptions() {
        return options.stream()
                .filter(AnswerOption::getIsCorrect)
                .toList();
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", text='" + (text != null ? text.substring(0, Math.min(text.length(), 50)) : null) + "...'" +
                ", type=" + type +
                ", optionCount=" + (options != null ? options.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return id != null && id.equals(question.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
