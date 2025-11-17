package org.example.learningplatform.service;

import entity.*;
import exception.BusinessException;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.*;
import service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EnrollmentService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application.properties")
class EnrollmentServiceIntegrationTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User student;
    private User teacher;
    private Course course;
    private Category category;

    @BeforeEach
    void setUp() {
        // Clean up
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create student
        student = User.builder()
                .email("student@example.com")
                .name("Student Test")
                .role(UserRole.STUDENT)
                .build();
        student = userService.createUser(student);

        // Create teacher
        teacher = User.builder()
                .email("teacher@example.com")
                .name("Teacher Test")
                .role(UserRole.TEACHER)
                .build();
        teacher = userService.createUser(teacher);

        // Create category
        category = Category.builder()
                .name("Programming")
                .description("Programming courses")
                .build();
        category = categoryRepository.save(category);

        // Create course
        course = Course.builder()
                .title("Test Course")
                .description("Test course description")
                .duration("30 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .build();
        course = courseService.createCourse(course, teacher.getId(), category.getId());
    }

    @Test
    void testEnrollStudent_Success() {
        // When
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // Then
        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollment.getStudent()).isEqualTo(student);
        assertThat(enrollment.getCourse()).isEqualTo(course);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getProgressPercentage()).isEqualTo(0);
    }

    @Test
    void testEnrollStudent_OnlyStudentsCanEnroll_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> enrollmentService.enrollStudent(teacher.getId(), course.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only students can be enrolled");
    }

    @Test
    void testEnrollStudent_DuplicateEnrollment_ThrowsException() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        // When/Then
        assertThatThrownBy(() -> enrollmentService.enrollStudent(student.getId(), course.getId()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Enrollment already exists");
    }

    @Test
    void testUnenrollStudent_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        enrollmentService.unenrollStudent(student.getId(), course.getId());

        // Then
        Enrollment enrollment = enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId());
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.DROPPED);
    }

    @Test
    void testGetEnrollmentById_Success() {
        // Given
        Enrollment savedEnrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        Enrollment foundEnrollment = enrollmentService.getEnrollmentById(savedEnrollment.getId());

        // Then
        assertThat(foundEnrollment).isNotNull();
        assertThat(foundEnrollment.getId()).isEqualTo(savedEnrollment.getId());
    }

    @Test
    void testGetEnrollmentByStudentAndCourse_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        Enrollment enrollment = enrollmentService.getEnrollmentByStudentAndCourse(student.getId(), course.getId());

        // Then
        assertThat(enrollment).isNotNull();
        assertThat(enrollment.getStudent()).isEqualTo(student);
        assertThat(enrollment.getCourse()).isEqualTo(course);
    }

    @Test
    void testGetEnrollmentsByStudent_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        Course anotherCourse = Course.builder()
                .title("Another Course")
                .description("Another course description")
                .duration("20 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        anotherCourse = courseService.createCourse(anotherCourse, teacher.getId(), category.getId());
        enrollmentService.enrollStudent(student.getId(), anotherCourse.getId());

        // When
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student.getId());

        // Then
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).allMatch(e -> e.getStudent().equals(student));
    }

    @Test
    void testGetActiveEnrollmentsByStudent_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        Course anotherCourse = Course.builder()
                .title("Another Course")
                .description("Another course description")
                .duration("20 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        anotherCourse = courseService.createCourse(anotherCourse, teacher.getId(), category.getId());
        enrollmentService.enrollStudent(student.getId(), anotherCourse.getId());
        enrollmentService.unenrollStudent(student.getId(), anotherCourse.getId());

        // When
        List<Enrollment> activeEnrollments = enrollmentService.getActiveEnrollmentsByStudent(student.getId());

        // Then
        assertThat(activeEnrollments).hasSize(1);
        assertThat(activeEnrollments).allMatch(e -> e.getStatus() == EnrollmentStatus.ACTIVE);
    }

    @Test
    void testGetCompletedEnrollmentsByStudent_Success() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());
        enrollmentService.completeEnrollment(enrollment.getId(), 95.0);

        // When
        List<Enrollment> completedEnrollments = enrollmentService.getCompletedEnrollmentsByStudent(student.getId());

        // Then
        assertThat(completedEnrollments).hasSize(1);
        assertThat(completedEnrollments).allMatch(e -> e.getStatus() == EnrollmentStatus.COMPLETED);
    }

    @Test
    void testGetEnrollmentsByCourse_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        User anotherStudent = User.builder()
                .email("student2@example.com")
                .name("Another Student")
                .role(UserRole.STUDENT)
                .build();
        anotherStudent = userService.createUser(anotherStudent);
        enrollmentService.enrollStudent(anotherStudent.getId(), course.getId());

        // When
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(course.getId());

        // Then
        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).allMatch(e -> e.getCourse().equals(course));
    }

    @Test
    void testUpdateProgress_Success() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        Enrollment updatedEnrollment = enrollmentService.updateProgress(enrollment.getId(), 50);

        // Then
        assertThat(updatedEnrollment.getProgressPercentage()).isEqualTo(50);
        assertThat(updatedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    void testUpdateProgress_AutoCompleteAt100Percent() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        Enrollment updatedEnrollment = enrollmentService.updateProgress(enrollment.getId(), 100);

        // Then
        assertThat(updatedEnrollment.getProgressPercentage()).isEqualTo(100);
        assertThat(updatedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void testUpdateProgress_InvalidPercentage_ThrowsException() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // When/Then
        assertThatThrownBy(() -> enrollmentService.updateProgress(enrollment.getId(), 150))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Progress percentage must be between 0 and 100");

        assertThatThrownBy(() -> enrollmentService.updateProgress(enrollment.getId(), -10))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Progress percentage must be between 0 and 100");
    }

    @Test
    void testCompleteEnrollment_Success() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        Enrollment completedEnrollment = enrollmentService.completeEnrollment(enrollment.getId(), 88.5);

        // Then
        assertThat(completedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(completedEnrollment.getFinalGrade()).isEqualTo(88.5);
        assertThat(completedEnrollment.getCompletedAt()).isNotNull();
    }

    @Test
    void testCompleteEnrollment_AlreadyCompleted_ThrowsException() {
        // Given
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());
        enrollmentService.completeEnrollment(enrollment.getId(), 90.0);

        // When/Then
        assertThatThrownBy(() -> enrollmentService.completeEnrollment(enrollment.getId(), 95.0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Enrollment is already completed");
    }

    @Test
    void testIsStudentEnrolled_Success() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        boolean isEnrolled = enrollmentService.isStudentEnrolled(student.getId(), course.getId());

        // Then
        assertThat(isEnrolled).isTrue();
    }

    @Test
    void testIsStudentEnrolled_NotEnrolled() {
        // When
        boolean isEnrolled = enrollmentService.isStudentEnrolled(student.getId(), course.getId());

        // Then
        assertThat(isEnrolled).isFalse();
    }

    @Test
    void testCountEnrollments() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        // When
        long count = enrollmentService.countEnrollmentsByStudent(student.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testCountActiveEnrollments() {
        // Given
        enrollmentService.enrollStudent(student.getId(), course.getId());

        Course anotherCourse = Course.builder()
                .title("Another Course")
                .description("Another course description")
                .duration("20 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build();
        anotherCourse = courseService.createCourse(anotherCourse, teacher.getId(), category.getId());
        Enrollment enrollment2 = enrollmentService.enrollStudent(student.getId(), anotherCourse.getId());
        enrollmentService.completeEnrollment(enrollment2.getId(), 90.0);

        // When
        long activeCount = enrollmentService.countActiveEnrollmentsByStudent(student.getId());

        // Then
        assertThat(activeCount).isEqualTo(1);
    }
}
