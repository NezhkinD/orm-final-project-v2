package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment entity representing homework or tasks for students.
 * Associated with a specific lesson.
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Assignment title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDateTime dueDate;

    @Column
    private Integer maxScore; // Maximum score for this assignment (e.g., 100)

    // Many-to-One: Many assignments belong to one lesson
    @NotNull(message = "Lesson is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // One-to-Many: One assignment has many submissions
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Helper methods

    /**
     * Add a submission to this assignment
     */
    public void addSubmission(Submission submission) {
        submissions.add(submission);
        submission.setAssignment(this);
    }

    /**
     * Remove a submission from this assignment
     */
    public void removeSubmission(Submission submission) {
        submissions.remove(submission);
        submission.setAssignment(null);
    }

    /**
     * Check if the assignment is past due date
     */
    public boolean isPastDue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dueDate=" + dueDate +
                ", maxScore=" + maxScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment)) return false;
        Assignment that = (Assignment) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
