package org.example.learningplatform.service;

import org.example.learningplatform.LearningPlatformApplication;
import entity.*;
import exception.BusinessException;
import exception.ResourceNotFoundException;
import repository.*;
import service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.LazyInitializationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CourseService
 */
@SpringBootTest(classes = LearningPlatformApplication.class)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application.properties")
class CourseServiceIntegrationTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User teacher;
    private User student;
    private Category category;

    @BeforeEach
    void setUp() {
        // Clean up
        courseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        tagRepository.deleteAll();

        // Create test teacher
        teacher = User.builder()
                .email("teacher@example.com")
                .name("Teacher Test")
                .role(UserRole.TEACHER)
                .build();
        teacher = userService.createUser(teacher);

        // Create test student
        student = User.builder()
                .email("student@example.com")
                .name("Student Test")
                .role(UserRole.STUDENT)
                .build();
        student = userService.createUser(student);

        // Create test category
        category = Category.builder()
                .name("Programming")
                .description("Programming courses")
                .build();
        category = categoryRepository.save(category);
    }

    @Test
    void testCreateCourse_Success() {
        // Given
        Course course = Course.builder()
                .title("Java Programming")
                .description("Learn Java from scratch")
                .duration("40 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        // When
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // Then
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getTitle()).isEqualTo("Java Programming");
        assertThat(savedCourse.getTeacher()).isEqualTo(teacher);
        assertThat(savedCourse.getCategory()).isEqualTo(category);
    }

    @Test
    void testCreateCourse_OnlyTeacherCanCreate_ThrowsException() {
        // Given
        Course course = Course.builder()
                .title("Test Course")
                .description("Test")
                .duration("10 hours")
                .build();

        // When/Then
        assertThatThrownBy(() -> courseService.createCourse(course, student.getId(), category.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only teachers can create courses");
    }

    @Test
    void testGetCourseById_Success() {
        // Given
        Course course = createTestCourse("Find Me Course");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // When
        Course foundCourse = courseService.getCourseById(savedCourse.getId());

        // Then
        assertThat(foundCourse).isNotNull();
        assertThat(foundCourse.getTitle()).isEqualTo("Find Me Course");
    }

    @Test
    void testGetCourseWithModules_Success() {
        // Given
        Course course = createTestCourse("Course with Modules");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        entity.Module module1 = entity.Module.builder()
                .title("Module 1")
                .description("First module")
                .orderIndex(1)
                .build();
        courseService.addModuleToCourse(savedCourse.getId(), module1);

        entity.Module module2 = entity.Module.builder()
                .title("Module 2")
                .description("Second module")
                .orderIndex(2)
                .build();
        courseService.addModuleToCourse(savedCourse.getId(), module2);

        // When
        Course courseWithModules = courseService.getCourseWithModules(savedCourse.getId());

        // Then
        assertThat(courseWithModules.getModules()).hasSize(2);
        assertThat(courseWithModules.getModules()).extracting("title")
                .containsExactlyInAnyOrder("Module 1", "Module 2");
    }

    @Test
    void testGetCourseWithFullStructure_Success() {
        // Given
        Course course = createTestCourse("Full Structure Course");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // Add module
        entity.Module module = entity.Module.builder()
                .title("Module 1")
                .description("First module")
                .orderIndex(1)
                .build();
        entity.Module savedModule = courseService.addModuleToCourse(savedCourse.getId(), module);

        // Add lesson to module
        Lesson lesson = Lesson.builder()
                .title("Lesson 1")
                .content("Lesson content")
                .orderIndex(1)
                .durationMinutes(60)
                .build();
        courseService.addLessonToModule(savedModule.getId(), lesson);

        // When
        Course fullCourse = courseService.getCourseWithFullStructure(savedCourse.getId());

        // Then
        assertThat(fullCourse.getModules()).hasSize(1);
        assertThat(fullCourse.getModules().get(0).getLessons()).hasSize(1);
        assertThat(fullCourse.getModules().get(0).getLessons().get(0).getTitle()).isEqualTo("Lesson 1");
    }

    @Test
    void testGetCoursesByCategory_Success() {
        // Given
        courseService.createCourse(createTestCourse("Course 1"), teacher.getId(), category.getId());
        courseService.createCourse(createTestCourse("Course 2"), teacher.getId(), category.getId());

        Category otherCategory = Category.builder()
                .name("Other")
                .description("Other category")
                .build();
        otherCategory = categoryRepository.save(otherCategory);
        courseService.createCourse(createTestCourse("Course 3"), teacher.getId(), otherCategory.getId());

        // When
        List<Course> courses = courseService.getCoursesByCategory(category.getId());

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).allMatch(c -> c.getCategory().equals(category));
    }

    @Test
    void testGetCoursesByTeacher_Success() {
        // Given
        courseService.createCourse(createTestCourse("Teacher Course 1"), teacher.getId(), category.getId());
        courseService.createCourse(createTestCourse("Teacher Course 2"), teacher.getId(), category.getId());

        // Create another teacher
        User anotherTeacher = User.builder()
                .email("another@example.com")
                .name("Another Teacher")
                .role(UserRole.TEACHER)
                .build();
        anotherTeacher = userService.createUser(anotherTeacher);
        courseService.createCourse(createTestCourse("Another Course"), anotherTeacher.getId(), category.getId());

        // When
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getId());

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).allMatch(c -> c.getTeacher().equals(teacher));
    }

    @Test
    void testSearchCoursesByTitle_Success() {
        // Given
        courseService.createCourse(createTestCourse("Java Basics"), teacher.getId(), category.getId());
        courseService.createCourse(createTestCourse("Advanced Java"), teacher.getId(), category.getId());
        courseService.createCourse(createTestCourse("Python Basics"), teacher.getId(), category.getId());

        // When
        List<Course> javaCourses = courseService.searchCoursesByTitle("Java");

        // Then
        assertThat(javaCourses).hasSize(2);
        assertThat(javaCourses).allMatch(c -> c.getTitle().contains("Java"));
    }

    @Test
    void testUpdateCourse_Success() {
        // Given
        Course course = createTestCourse("Original Title");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // When
        savedCourse.setTitle("Updated Title");
        savedCourse.setDescription("Updated description");
        Course updatedCourse = courseService.updateCourse(savedCourse.getId(), savedCourse);

        // Then
        assertThat(updatedCourse.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedCourse.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void testDeleteCourse_Success() {
        // Given
        Course course = createTestCourse("To Delete");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());
        Long courseId = savedCourse.getId();

        // When
        courseService.deleteCourse(courseId);

        // Then
        assertThatThrownBy(() -> courseService.getCourseById(courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testAddModuleToCourse_Success() {
        // Given
        Course course = createTestCourse("Course for Module");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        entity.Module module = entity.Module.builder()
                .title("New Module")
                .description("Module description")
                .orderIndex(1)
                .build();

        // When
        entity.Module savedModule = courseService.addModuleToCourse(savedCourse.getId(), module);

        // Then
        assertThat(savedModule.getId()).isNotNull();
        assertThat(savedModule.getCourse()).isEqualTo(savedCourse);
        assertThat(savedModule.getTitle()).isEqualTo("New Module");
    }

    @Test
    void testAddLessonToModule_Success() {
        // Given
        Course course = createTestCourse("Course for Lesson");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        entity.Module module = entity.Module.builder()
                .title("Module")
                .description("Module desc")
                .orderIndex(1)
                .build();
        entity.Module savedModule = courseService.addModuleToCourse(savedCourse.getId(), module);

        Lesson lesson = Lesson.builder()
                .title("New Lesson")
                .content("Lesson content")
                .orderIndex(1)
                .durationMinutes(45)
                .build();

        // When
        Lesson savedLesson = courseService.addLessonToModule(savedModule.getId(), lesson);

        // Then
        assertThat(savedLesson.getId()).isNotNull();
        assertThat(savedLesson.getModule()).isEqualTo(savedModule);
        assertThat(savedLesson.getTitle()).isEqualTo("New Lesson");
    }

    @Test
    void testAddTagsToCourse_Success() {
        // Given
        Course course = createTestCourse("Course for Tags");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // When
        Course courseWithTags = courseService.addTagsToCourse(savedCourse.getId(), Set.of("java", "beginner", "oop"));

        // Then
        assertThat(courseWithTags.getTags()).hasSize(3);
        assertThat(courseWithTags.getTags()).extracting("name")
                .containsExactlyInAnyOrder("java", "beginner", "oop");
    }

    @Test
    void testGetCoursesByTag_Success() {
        // Given
        Course course1 = createTestCourse("Java Course 1");
        Course savedCourse1 = courseService.createCourse(course1, teacher.getId(), category.getId());
        courseService.addTagsToCourse(savedCourse1.getId(), Set.of("java", "beginner"));

        Course course2 = createTestCourse("Java Course 2");
        Course savedCourse2 = courseService.createCourse(course2, teacher.getId(), category.getId());
        courseService.addTagsToCourse(savedCourse2.getId(), Set.of("java", "advanced"));

        Course course3 = createTestCourse("Python Course");
        Course savedCourse3 = courseService.createCourse(course3, teacher.getId(), category.getId());
        courseService.addTagsToCourse(savedCourse3.getId(), Set.of("python"));

        // When
        List<Course> javaCourses = courseService.getCoursesByTag("java");

        // Then
        assertThat(javaCourses).hasSize(2);
    }

    // ==============================================
    // LAZY LOADING DEMONSTRATION TESTS
    // ==============================================

    /**
     * Test 1: Demonstrates LazyInitializationException
     * This test shows what happens when trying to access a lazy collection outside of transaction context.
     * The modules collection is LAZY, so accessing it after the transaction is closed will throw exception.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Disable transaction for this test
    void testLazyLoading_AccessModulesOutsideTransaction_ThrowsException() {
        // Given - create a course with modules in a separate transaction
        Long courseId;
        try {
            // Manually open transaction to create data
            Course course = createTestCourse("Course with Lazy Modules");
            Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

            entity.Module module1 = entity.Module.builder()
                    .title("Lazy Module 1")
                    .description("First module")
                    .orderIndex(1)
                    .build();
            courseService.addModuleToCourse(savedCourse.getId(), module1);

            courseId = savedCourse.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - try to access lazy collection outside transaction
        Course courseOutsideTransaction = courseRepository.findById(courseId).orElseThrow();

        // Then - accessing lazy collection should throw LazyInitializationException
        assertThatThrownBy(() -> courseOutsideTransaction.getModules().size())
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    /**
     * Test 2: Demonstrates solution using JOIN FETCH
     * This test shows how to solve lazy loading by using JOIN FETCH in repository query.
     * With JOIN FETCH, the modules are loaded eagerly and can be accessed outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessModulesWithJoinFetch_Success() {
        // Given - create a course with modules
        Long courseId;
        try {
            Course course = createTestCourse("Course for JOIN FETCH");
            Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

            entity.Module module1 = entity.Module.builder()
                    .title("Module 1")
                    .description("First module")
                    .orderIndex(1)
                    .build();
            courseService.addModuleToCourse(savedCourse.getId(), module1);

            entity.Module module2 = entity.Module.builder()
                    .title("Module 2")
                    .description("Second module")
                    .orderIndex(2)
                    .build();
            courseService.addModuleToCourse(savedCourse.getId(), module2);

            courseId = savedCourse.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - use repository method with JOIN FETCH
        Course courseWithModules = courseRepository.findByIdWithModules(courseId).orElseThrow();

        // Then - can access modules outside transaction because they were eagerly loaded
        assertThat(courseWithModules.getModules()).isNotNull();
        assertThat(courseWithModules.getModules()).hasSize(2);
        assertThat(courseWithModules.getModules()).extracting("title")
                .containsExactlyInAnyOrder("Module 1", "Module 2");
    }

    /**
     * Test 3: Demonstrates solution using @Transactional
     * This test shows that accessing lazy collections within transaction context works fine.
     * The transaction keeps the Hibernate session open, allowing lazy loading to work.
     */
    @Test
    @Transactional // This test runs within transaction
    void testLazyLoading_AccessModulesWithinTransaction_Success() {
        // Given - create a course with modules
        Course course = createTestCourse("Course within Transaction");
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        entity.Module module1 = entity.Module.builder()
                .title("Transactional Module 1")
                .description("First module")
                .orderIndex(1)
                .build();
        courseService.addModuleToCourse(savedCourse.getId(), module1);

        entity.Module module2 = entity.Module.builder()
                .title("Transactional Module 2")
                .description("Second module")
                .orderIndex(2)
                .build();
        courseService.addModuleToCourse(savedCourse.getId(), module2);

        // When - access course normally within transaction
        Course foundCourse = courseRepository.findById(savedCourse.getId()).orElseThrow();

        // Then - can access lazy modules because we're still within transaction
        assertThat(foundCourse.getModules()).isNotNull();
        assertThat(foundCourse.getModules()).hasSize(2);
        assertThat(foundCourse.getModules()).extracting("title")
                .containsExactlyInAnyOrder("Transactional Module 1", "Transactional Module 2");
    }

    /**
     * Test 4: Demonstrates N+1 query problem and its solution
     * This test shows the N+1 problem: loading N courses, then accessing modules triggers N additional queries.
     * The solution is to use JOIN FETCH to load everything in a single query.
     */
    @Test
    @Transactional
    void testNPlusOneProblem_ModulesAndLessons_Demonstration() {
        // Given - create multiple courses with modules and lessons
        for (int i = 1; i <= 3; i++) {
            Course course = createTestCourse("N+1 Course " + i);
            Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

            for (int j = 1; j <= 2; j++) {
                entity.Module module = entity.Module.builder()
                        .title("Module " + j + " of Course " + i)
                        .description("Module description")
                        .orderIndex(j)
                        .build();
                entity.Module savedModule = courseService.addModuleToCourse(savedCourse.getId(), module);

                for (int k = 1; k <= 2; k++) {
                    Lesson lesson = Lesson.builder()
                            .title("Lesson " + k + " of Module " + j)
                            .content("Lesson content")
                            .orderIndex(k)
                            .durationMinutes(30)
                            .build();
                    courseService.addLessonToModule(savedModule.getId(), lesson);
                }
            }
        }

        // When - demonstrate N+1 problem: loading courses then accessing modules
        // This will trigger: 1 query to load courses + N queries to load modules for each course
        List<Course> courses = courseRepository.findAll();

        // Accessing modules triggers additional queries (N+1 problem)
        long totalModules = courses.stream()
                .mapToLong(c -> c.getModules().size())
                .sum();

        // Then - verify data is correct despite N+1 queries
        assertThat(courses).hasSize(3);
        assertThat(totalModules).isEqualTo(6); // 3 courses * 2 modules each

        // Now demonstrate the SOLUTION using service method with JOIN FETCH
        // This uses the service method which internally uses JOIN FETCH
        List<Course> coursesWithFullStructure = courses.stream()
                .map(c -> courseService.getCourseWithFullStructure(c.getId()))
                .toList();

        // Accessing modules and lessons now doesn't trigger additional queries
        long totalLessons = coursesWithFullStructure.stream()
                .flatMap(c -> c.getModules().stream())
                .mapToLong(m -> m.getLessons().size())
                .sum();

        // Then - verify same data with better performance
        assertThat(coursesWithFullStructure).hasSize(3);
        assertThat(totalLessons).isEqualTo(12); // 3 courses * 2 modules * 2 lessons
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    // Helper method
    private Course createTestCourse(String title) {
        return Course.builder()
                .title(title)
                .description("Test course description")
                .duration("30 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .build();
    }
}
