package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Course entity representing an educational course in the platform.
 * Central entity with many relationships to other entities.
 */
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    private String duration; // e.g., "8 weeks", "3 months"

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    // Many-to-One: Many courses belong to one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Many-to-One: Many courses are taught by one teacher
    @NotNull(message = "Teacher is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // One-to-Many: One course has many modules
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    // One-to-Many: One course has many enrollments
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();

    // One-to-Many: One course has many reviews
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourseReview> reviews = new ArrayList<>();

    // Many-to-Many: Course can have many tags
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "course_tags",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for managing bidirectional relationships

    /**
     * Add a module to this course
     */
    public void addModule(Module module) {
        modules.add(module);
        module.setCourse(this);
    }

    /**
     * Remove a module from this course
     */
    public void removeModule(Module module) {
        modules.remove(module);
        module.setCourse(null);
    }

    /**
     * Add an enrollment to this course
     */
    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setCourse(this);
    }

    /**
     * Remove an enrollment from this course
     */
    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setCourse(null);
    }

    /**
     * Add a review to this course
     */
    public void addReview(CourseReview review) {
        reviews.add(review);
        review.setCourse(this);
    }

    /**
     * Add a tag to this course
     */
    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getCourses().add(this);
    }

    /**
     * Remove a tag from this course
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getCourses().remove(this);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", startDate=" + startDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return id != null && id.equals(course.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
