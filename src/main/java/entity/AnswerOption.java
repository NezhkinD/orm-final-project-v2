package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * AnswerOption entity representing a possible answer to a quiz question.
 * Marked as correct or incorrect.
 */
@Entity
@Table(name = "answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Answer option text is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @NotNull(message = "isCorrect flag is required")
    @Column(nullable = false)
    private Boolean isCorrect;

    @Column
    private Integer orderIndex; // Display order of this option

    // Many-to-One: Many options belong to one question
    @NotNull(message = "Question is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Override
    public String toString() {
        return "AnswerOption{" +
                "id=" + id +
                ", text='" + (text != null ? text.substring(0, Math.min(text.length(), 50)) : null) + "...'" +
                ", isCorrect=" + isCorrect +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnswerOption)) return false;
        AnswerOption that = (AnswerOption) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
