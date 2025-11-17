package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Submission entity representing a student's submitted work for an assignment.
 * Links student, assignment, and grading information.
 */
@Entity
@Table(
        name = "submissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many submissions belong to one assignment
    @NotNull(message = "Assignment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    // Many-to-One: Many submissions belong to one student
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(columnDefinition = "TEXT")
    private String content; // Text content or file path/URL

    @Column
    private Integer score; // Actual score received (null if not graded yet)

    @Column(columnDefinition = "TEXT")
    private String feedback; // Teacher's feedback

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime gradedAt; // When the teacher graded it

    /**
     * Check if this submission has been graded
     */
    public boolean isGraded() {
        return score != null;
    }

    /**
     * Check if submission was late
     */
    public boolean isLate() {
        if (assignment == null || assignment.getDueDate() == null) {
            return false;
        }
        return submittedAt.isAfter(assignment.getDueDate());
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", score=" + score +
                ", submittedAt=" + submittedAt +
                ", isGraded=" + isGraded() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Submission)) return false;
        Submission that = (Submission) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
