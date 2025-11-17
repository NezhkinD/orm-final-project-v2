package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.lang.Module;
import java.util.ArrayList;
import java.util.List;

/**
 * Lesson entity representing individual lessons within a module.
 * Contains learning materials, videos, and assignments.
 */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Lesson title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String videoUrl;

    @NotNull(message = "Order index is required")
    @Column(nullable = false)
    private Integer orderIndex;

    @Column
    private Integer durationMinutes; // Duration in minutes

    // Many-to-One: Many lessons belong to one module
    @NotNull(message = "Module is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    // One-to-Many: One lesson can have many assignments
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Assignment> assignments = new ArrayList<>();

    // Helper methods

    /**
     * Add an assignment to this lesson
     */
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        assignment.setLesson(this);
    }

    /**
     * Remove an assignment from this lesson
     */
    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
        assignment.setLesson(null);
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                ", durationMinutes=" + durationMinutes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        Lesson lesson = (Lesson) o;
        return id != null && id.equals(lesson.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
