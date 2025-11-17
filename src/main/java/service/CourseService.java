package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.entity.*;
import org.example.learningplatform.exception.BusinessException;
import org.example.learningplatform.exception.ResourceNotFoundException;
import org.example.learningplatform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service for managing courses and their structure (modules, lessons).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    /**
     * Create a new course
     */
    @Transactional
    public Course createCourse(Course course, Long teacherId, Long categoryId) {
        log.info("Creating course: {} by teacher id: {}", course.getTitle(), teacherId);

        // Validate and set teacher
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", teacherId));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new BusinessException("Only teachers can create courses");
        }

        course.setTeacher(teacher);

        // Set category if provided
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            course.setCategory(category);
        }

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with id: {}", savedCourse.getId());
        return savedCourse;
    }

    /**
     * Get course by ID
     */
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    /**
     * Get course with modules loaded (avoiding lazy loading exception)
     */
    public Course getCourseWithModules(Long id) {
        return courseRepository.findByIdWithModules(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    /**
     * Get course with full structure (modules and lessons) loaded
     */
    public Course getCourseWithFullStructure(Long id) {
        Course course = courseRepository.findByIdWithFullStructure(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        // Initialize lessons collections within transaction to avoid LazyInitializationException
        course.getModules().forEach(m -> m.getLessons().size());

        return course;
    }

    /**
     * Get all courses
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get all courses with teachers loaded (efficient for listing)
     */
    public List<Course> getAllCoursesWithTeachers() {
        return courseRepository.findAllWithTeachers();
    }

    /**
     * Get courses by category
     */
    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    /**
     * Get courses by teacher
     */
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    /**
     * Search courses by title
     */
    public List<Course> searchCoursesByTitle(String title) {
        return courseRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Get courses by tag
     */
    public List<Course> getCoursesByTag(String tagName) {
        return courseRepository.findByTagName(tagName);
    }

    /**
     * Update course
     */
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        log.info("Updating course with id: {}", id);

        Course course = getCourseById(id);

        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setDuration(courseDetails.getDuration());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated successfully with id: {}", updatedCourse.getId());
        return updatedCourse;
    }

    /**
     * Delete course
     */
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with id: {}", id);

        Course course = getCourseById(id);
        courseRepository.delete(course);

        log.info("Course deleted successfully with id: {}", id);
    }

    /**
     * Add module to course
     */
    @Transactional
    public org.example.learningplatform.entity.Module addModuleToCourse(Long courseId, org.example.learningplatform.entity.Module module) {
        log.info("Adding module to course id: {}", courseId);

        Course course = getCourseById(courseId);
        module.setCourse(course);

        org.example.learningplatform.entity.Module savedModule = moduleRepository.save(module);
        log.info("Module added successfully with id: {}", savedModule.getId());
        return savedModule;
    }

    /**
     * Add lesson to module
     */
    @Transactional
    public Lesson addLessonToModule(Long moduleId, Lesson lesson) {
        log.info("Adding lesson to module id: {}", moduleId);

        org.example.learningplatform.entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));
        lesson.setModule(module);

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson added successfully with id: {}", savedLesson.getId());
        return savedLesson;
    }

    /**
     * Add tags to course
     */
    @Transactional
    public Course addTagsToCourse(Long courseId, Set<String> tagNames) {
        log.info("Adding tags to course id: {}", courseId);

        Course course = getCourseById(courseId);

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder().name(tagName).build();
                        return tagRepository.save(newTag);
                    });
            course.addTag(tag);
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Tags added successfully to course id: {}", courseId);
        return updatedCourse;
    }

    /**
     * Remove tag from course
     */
    @Transactional
    public void removeTagFromCourse(Long courseId, Long tagId) {
        log.info("Removing tag {} from course {}", tagId, courseId);

        Course course = getCourseById(courseId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));

        course.removeTag(tag);
        courseRepository.save(course);

        log.info("Tag removed successfully from course id: {}", courseId);
    }

    /**
     * Get popular courses (most enrollments)
     */
    public List<Course> getPopularCourses() {
        return courseRepository.findPopularCourses();
    }

    /**
     * Get average rating for a course
     */
    public Double getCourseAverageRating(Long courseId) {
        return courseRepository.getAverageRating(courseId);
    }

    /**
     * Count active enrollments in a course
     */
    public long countActiveEnrollments(Long courseId) {
        return courseRepository.countActiveEnrollments(courseId);
    }
}
