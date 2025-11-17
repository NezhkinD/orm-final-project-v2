package org.example.learningplatform.service;

import org.example.learningplatform.LearningPlatformApplication;
import entity.*;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.*;
import service.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserService
 */
@SpringBootTest(classes = LearningPlatformApplication.class)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application.properties")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateUser_Success() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .name("John Doe")
                .role(UserRole.STUDENT)
                .build();

        // When
        User savedUser = userService.createUser(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsException() {
        // Given
        User user1 = User.builder()
                .email("duplicate@example.com")
                .name("John Doe")
                .role(UserRole.STUDENT)
                .build();
        userService.createUser(user1);

        User user2 = User.builder()
                .email("duplicate@example.com")
                .name("Jane Smith")
                .role(UserRole.TEACHER)
                .build();

        // When/Then
        assertThatThrownBy(() -> userService.createUser(user2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("duplicate@example.com");
    }

    @Test
    void testCreateUserWithProfile_Success() {
        // Given
        User user = User.builder()
                .email("profile@example.com")
                .name("Alice Johnson")
                .role(UserRole.STUDENT)
                .build();
        User savedUser = userService.createUser(user);

        Profile profile = Profile.builder()
                .user(savedUser)
                .bio("Student bio")
                .build();

        // Manually set profile (in real app would use service method)
        savedUser.setProfile(profile);
        User updatedUser = userService.updateUser(savedUser.getId(), savedUser);

        // Then
        assertThat(updatedUser.getId()).isNotNull();
        assertThat(updatedUser.getProfile()).isNotNull();
        assertThat(updatedUser.getProfile().getBio()).isEqualTo("Student bio");
    }

    @Test
    void testGetUserById_Success() {
        // Given
        User user = createTestUser("find@example.com", UserRole.STUDENT);
        User savedUser = userService.createUser(user);

        // When
        User foundUser = userService.getUserById(savedUser.getId());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("find@example.com");
    }

    @Test
    void testGetUserById_NotFound_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void testGetUserByEmail_Success() {
        // Given
        User user = createTestUser("email@example.com", UserRole.TEACHER);
        userService.createUser(user);

        // When
        User foundUser = userService.getUserByEmail("email@example.com");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("email@example.com");
    }

    @Test
    void testGetUsersByRole_Success() {
        // Given
        userService.createUser(createTestUser("student1@example.com", UserRole.STUDENT));
        userService.createUser(createTestUser("student2@example.com", UserRole.STUDENT));
        userService.createUser(createTestUser("teacher@example.com", UserRole.TEACHER));

        // When
        List<User> students = userService.getUsersByRole(UserRole.STUDENT);

        // Then
        assertThat(students).hasSize(2);
        assertThat(students).allMatch(u -> u.getRole() == UserRole.STUDENT);
    }

    @Test
    void testSearchUsersByName_Success() {
        // Given
        userService.createUser(createTestUser("john.doe@example.com", "John Doe", UserRole.STUDENT));
        userService.createUser(createTestUser("john.smith@example.com", "John Smith", UserRole.STUDENT));
        userService.createUser(createTestUser("jane.doe@example.com", "Jane Doe", UserRole.TEACHER));

        // When
        List<User> johns = userService.searchUsersByName("John");

        // Then
        assertThat(johns).hasSize(2);
        assertThat(johns).allMatch(u -> u.getName().contains("John"));
    }

    @Test
    void testUpdateUser_Success() {
        // Given
        User user = createTestUser("update@example.com", UserRole.STUDENT);
        User savedUser = userService.createUser(user);

        // When
        savedUser.setName("Updated Name");
        User updatedUser = userService.updateUser(savedUser.getId(), savedUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        User user = createTestUser("delete@example.com", UserRole.STUDENT);
        User savedUser = userService.createUser(user);
        Long userId = savedUser.getId();

        // When
        userService.deleteUser(userId);

        // Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    // ==============================================
    // LAZY LOADING DEMONSTRATION TESTS
    // ==============================================

    /**
     * Test 1: Demonstrates LazyInitializationException for User → Enrollments
     * This test shows what happens when trying to access lazy enrollments outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessEnrollmentsOutsideTransaction_ThrowsException() {
        // Given - create user with enrollments in a separate transaction
        Long studentId;
        try {
            User student = createTestUser("lazy.student@example.com", UserRole.STUDENT);
            User savedStudent = userService.createUser(student);

            User teacher = createTestUser("lazy.teacher@example.com", UserRole.TEACHER);
            User savedTeacher = userService.createUser(teacher);

            Category category = Category.builder()
                    .name("Lazy Category")
                    .description("Category for lazy loading test")
                    .build();
            Category savedCategory = categoryRepository.save(category);

            Course course = Course.builder()
                    .title("Lazy Course")
                    .description("Course for lazy loading")
                    .duration("20 hours")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(2))
                    .build();
            Course savedCourse = courseService.createCourse(course, savedTeacher.getId(), savedCategory.getId());

            enrollmentService.enrollStudent(savedStudent.getId(), savedCourse.getId());

            studentId = savedStudent.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - try to access lazy enrollments outside transaction
        User userOutsideTransaction = userRepository.findById(studentId).orElseThrow();

        // Then - accessing lazy collection should throw LazyInitializationException
        assertThatThrownBy(() -> userOutsideTransaction.getEnrollments().size())
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    /**
     * Test 2: Demonstrates solution using JOIN FETCH for User → Enrollments → Courses
     * Shows how JOIN FETCH allows accessing lazy collections outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessEnrollmentsWithJoinFetch_Success() {
        // Given - create user with multiple enrollments
        Long studentId;
        try {
            User student = createTestUser("fetch.student@example.com", UserRole.STUDENT);
            User savedStudent = userService.createUser(student);

            User teacher = createTestUser("fetch.teacher@example.com", UserRole.TEACHER);
            User savedTeacher = userService.createUser(teacher);

            Category category = Category.builder()
                    .name("Fetch Category")
                    .description("Category for fetch test")
                    .build();
            Category savedCategory = categoryRepository.save(category);

            // Create 2 courses and enroll
            for (int i = 1; i <= 2; i++) {
                Course course = Course.builder()
                        .title("Course " + i)
                        .description("Description " + i)
                        .duration("30 hours")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusMonths(3))
                        .build();
                Course savedCourse = courseService.createCourse(course, savedTeacher.getId(), savedCategory.getId());
                enrollmentService.enrollStudent(savedStudent.getId(), savedCourse.getId());
            }

            studentId = savedStudent.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - use repository method with JOIN FETCH for enrollments and courses
        User userWithEnrollments = userRepository.findByIdWithEnrollmentsAndCourses(studentId).orElseThrow();

        // Then - can access enrollments and courses outside transaction
        assertThat(userWithEnrollments.getEnrollments()).isNotNull();
        assertThat(userWithEnrollments.getEnrollments()).hasSize(2);

        // Can even access nested Course entities
        assertThat(userWithEnrollments.getEnrollments())
                .extracting(enrollment -> enrollment.getCourse().getTitle())
                .containsExactlyInAnyOrder("Course 1", "Course 2");
    }

    /**
     * Test 3: Demonstrates solution using @Transactional for User → Enrollments
     * Shows that lazy loading works within transaction context.
     */
    @Test
    @Transactional
    void testLazyLoading_AccessEnrollmentsWithinTransaction_Success() {
        // Given - create user with enrollments
        User student = createTestUser("trans.student@example.com", UserRole.STUDENT);
        User savedStudent = userService.createUser(student);

        User teacher = createTestUser("trans.teacher@example.com", UserRole.TEACHER);
        User savedTeacher = userService.createUser(teacher);

        Category category = Category.builder()
                .name("Trans Category")
                .description("Category for transaction test")
                .build();
        Category savedCategory = categoryRepository.save(category);

        Course course1 = Course.builder()
                .title("Transactional Course 1")
                .description("First course")
                .duration("25 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .build();
        Course savedCourse1 = courseService.createCourse(course1, savedTeacher.getId(), savedCategory.getId());

        Course course2 = Course.builder()
                .title("Transactional Course 2")
                .description("Second course")
                .duration("35 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();
        Course savedCourse2 = courseService.createCourse(course2, savedTeacher.getId(), savedCategory.getId());

        enrollmentService.enrollStudent(savedStudent.getId(), savedCourse1.getId());
        enrollmentService.enrollStudent(savedStudent.getId(), savedCourse2.getId());

        // When - access user within transaction
        User foundUser = userRepository.findById(savedStudent.getId()).orElseThrow();

        // Then - can access lazy enrollments because we're within transaction
        assertThat(foundUser.getEnrollments()).isNotNull();
        assertThat(foundUser.getEnrollments()).hasSize(2);
        assertThat(foundUser.getEnrollments())
                .extracting(enrollment -> enrollment.getCourse().getTitle())
                .containsExactlyInAnyOrder("Transactional Course 1", "Transactional Course 2");
    }

    /**
     * Test 4: Demonstrates N+1 problem for User → Enrollments
     * Shows how loading multiple users and accessing their enrollments triggers N+1 queries.
     */
    @Test
    @Transactional
    void testNPlusOneProblem_UserEnrollments_Demonstration() {
        // Given - create multiple students with enrollments
        User teacher = createTestUser("n1.teacher@example.com", UserRole.TEACHER);
        User savedTeacher = userService.createUser(teacher);

        Category category = Category.builder()
                .name("N+1 Category")
                .description("Category for N+1 test")
                .build();
        Category savedCategory = categoryRepository.save(category);

        Course course1 = Course.builder()
                .title("N+1 Course 1")
                .description("First course")
                .duration("30 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .build();
        Course savedCourse1 = courseService.createCourse(course1, savedTeacher.getId(), savedCategory.getId());

        Course course2 = Course.builder()
                .title("N+1 Course 2")
                .description("Second course")
                .duration("40 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();
        Course savedCourse2 = courseService.createCourse(course2, savedTeacher.getId(), savedCategory.getId());

        // Create 3 students, each enrolled in both courses
        for (int i = 1; i <= 3; i++) {
            User student = createTestUser("n1.student" + i + "@example.com", UserRole.STUDENT);
            User savedStudent = userService.createUser(student);
            enrollmentService.enrollStudent(savedStudent.getId(), savedCourse1.getId());
            enrollmentService.enrollStudent(savedStudent.getId(), savedCourse2.getId());
        }

        // When - demonstrate N+1 problem: loading users then accessing enrollments
        // This triggers: 1 query for users + N queries for enrollments
        List<User> students = userRepository.findByRole(UserRole.STUDENT);

        // Accessing enrollments triggers additional queries (N+1 problem)
        long totalEnrollments = students.stream()
                .mapToLong(s -> s.getEnrollments().size())
                .sum();

        // Then - verify data is correct despite N+1 queries
        assertThat(students).hasSize(3);
        assertThat(totalEnrollments).isEqualTo(6); // 3 students * 2 courses each

        // Now demonstrate the SOLUTION using repository method with JOIN FETCH
        // Load each student with enrollments using JOIN FETCH
        List<User> studentsWithEnrollments = students.stream()
                .map(s -> userRepository.findByIdWithEnrollments(s.getId()).orElseThrow())
                .toList();

        // Accessing enrollments and courses now doesn't trigger additional queries
        long totalCourses = studentsWithEnrollments.stream()
                .flatMap(s -> s.getEnrollments().stream())
                .map(Enrollment::getCourse)
                .distinct()
                .count();

        // Then - verify same data with better performance
        assertThat(studentsWithEnrollments).hasSize(3);
        assertThat(totalCourses).isEqualTo(2); // 2 distinct courses
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    // Helper methods

    private User createTestUser(String email, UserRole role) {
        return User.builder()
                .email(email)
                .name("Test User")
                .role(role)
                .build();
    }

    private User createTestUser(String email, String name, UserRole role) {
        return User.builder()
                .email(email)
                .name(name)
                .role(role)
                .build();
    }
}
