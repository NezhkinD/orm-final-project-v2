package entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Tag entity for course tagging and filtering.
 * Many-to-Many relationship with Course.
 * Examples: Java, Hibernate, Beginner, Advanced, etc.
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // Many-to-Many relationship with Course (inverse side)
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    /**
     * Add a course to this tag
     */
    public void addCourse(Course course) {
        courses.add(course);
        course.getTags().add(this);
    }

    /**
     * Remove a course from this tag
     */
    public void removeCourse(Course course) {
        courses.remove(course);
        course.getTags().remove(this);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return id != null && id.equals(tag.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
