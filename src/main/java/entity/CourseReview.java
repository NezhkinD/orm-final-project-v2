package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CourseReview entity representing a student's review/rating of a course.
 * Students can leave feedback and rating after taking a course.
 */
@Entity
@Table(
        name = "course_reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "student_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many reviews belong to one course
    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Many-to-One: Many reviews belong to one student
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    @Column(nullable = false)
    private Integer rating; // Rating from 1 to 5

    @Column(columnDefinition = "TEXT")
    private String comment; // Optional review text

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if review has a comment
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CourseReview{" +
                "id=" + id +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                ", hasComment=" + hasComment() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseReview)) return false;
        CourseReview that = (CourseReview) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
