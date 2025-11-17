package service;

import entity.*;
import exception.BusinessException;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.CourseRepository;
import repository.EnrollmentRepository;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EnrollmentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User testStudent;
    private Course testCourse;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testStudent = User.builder()
                .id(1L)
                .name("Student")
                .email("student@example.com")
                .role(UserRole.STUDENT)
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .description("Description")
                .build();

        testEnrollment = Enrollment.builder()
                .id(1L)
                .student(testStudent)
                .course(testCourse)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercentage(0)
                .enrolledAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should enroll student successfully")
    void shouldEnrollStudentSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // When
        Enrollment result = enrollmentService.enrollStudent(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(testStudent);
        assertThat(result.getCourse()).isEqualTo(testCourse);
        verify(enrollmentRepository).existsByStudentIdAndCourseId(1L, 1L);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when non-student tries to enroll")
    void shouldThrowExceptionWhenNonStudentEnrolls() {
        // Given
        User teacher = User.builder()
                .id(2L)
                .role(UserRole.TEACHER)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enrollStudent(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("students");

        verify(userRepository).findById(2L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when student already enrolled")
    void shouldThrowExceptionWhenAlreadyEnrolled() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> enrollmentService.enrollStudent(1L, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Enrollment already exists");

        verify(enrollmentRepository).existsByStudentIdAndCourseId(1L, 1L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should get enrollment by id")
    void shouldGetEnrollmentById() {
        // Given
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));

        // When
        Enrollment result = enrollmentService.getEnrollmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(enrollmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get enrollments by student")
    void shouldGetEnrollmentsByStudent() {
        // Given
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(enrollments);

        // When
        List<Enrollment> result = enrollmentService.getEnrollmentsByStudent(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(enrollmentRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should get enrollments by course")
    void shouldGetEnrollmentsByCourse() {
        // Given
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByCourseId(1L)).thenReturn(enrollments);

        // When
        List<Enrollment> result = enrollmentService.getEnrollmentsByCourse(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(enrollmentRepository).findByCourseId(1L);
    }

    @Test
    @DisplayName("Should get enrollment by student and course")
    void shouldGetEnrollmentByStudentAndCourse() {
        // Given
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(testEnrollment));

        // When
        Enrollment result = enrollmentService.getEnrollmentByStudentAndCourse(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(testStudent);
        assertThat(result.getCourse()).isEqualTo(testCourse);
        verify(enrollmentRepository).findByStudentIdAndCourseId(1L, 1L);
    }

    @Test
    @DisplayName("Should count enrollments by course")
    void shouldCountEnrollmentsByCourse() {
        // Given
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(10L);

        // When
        long count = enrollmentRepository.countByCourseId(1L);

        // Then
        assertThat(count).isEqualTo(10L);
        verify(enrollmentRepository).countByCourseId(1L);
    }
}
