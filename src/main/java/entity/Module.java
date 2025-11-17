package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Module entity representing a thematic section within a course.
 * One course contains many modules in ordered sequence.
 */
@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Module title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Order index is required")
    @Column(nullable = false)
    private Integer orderIndex;

    // Many-to-One: Many modules belong to one course
    @NotNull(message = "Course is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // One-to-Many: One module has many lessons
    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    // One-to-One: Module can have one quiz (optional)
    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    // Helper methods

    /**
     * Add a lesson to this module
     */
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setModule(this);
    }

    /**
     * Remove a lesson from this module
     */
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setModule(null);
    }

    /**
     * Set the quiz for this module
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        if (quiz != null && quiz.getModule() != this) {
            quiz.setModule(this);
        }
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", orderIndex=" + orderIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module)) return false;
        Module module = (Module) o;
        return id != null && id.equals(module.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
