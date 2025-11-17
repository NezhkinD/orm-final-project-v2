package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Enrollment entity linking students to courses.
 * Represents the Many-to-Many relationship between User and Course
 * with additional metadata (enrollment date, status, progress).
 */
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One: Many enrollments belong to one student
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Many-to-One: Many enrollments belong to one course
    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Column
    private LocalDateTime completedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private Integer progressPercentage; // Progress in the course (0-100)

    @Column
    private Double finalGrade; // Final grade for the course

    /**
     * Mark enrollment as completed
     */
    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = 100;
    }

    /**
     * Mark enrollment as dropped
     */
    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    /**
     * Check if enrollment is active
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    /**
     * Check if course is completed
     */
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", status=" + status +
                ", enrolledAt=" + enrolledAt +
                ", progressPercentage=" + progressPercentage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment that = (Enrollment) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
