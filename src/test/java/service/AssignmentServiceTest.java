package service;

import entity.*;
import exception.BusinessException;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.*;
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
 * Unit tests for AssignmentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssignmentService Unit Tests")
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Assignment testAssignment;
    private Lesson testLesson;
    private User testStudent;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        testLesson = Lesson.builder()
                .id(1L)
                .title("Test Lesson")
                .content("Content")
                .orderIndex(1)
                .build();

        testAssignment = Assignment.builder()
                .id(1L)
                .title("Test Assignment")
                .description("Description")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .lesson(testLesson)
                .build();

        testStudent = User.builder()
                .id(1L)
                .name("Student")
                .email("student@example.com")
                .role(UserRole.STUDENT)
                .build();

        testSubmission = Submission.builder()
                .id(1L)
                .assignment(testAssignment)
                .student(testStudent)
                .content("My submission")
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create assignment successfully")
    void shouldCreateAssignmentSuccessfully() {
        // Given
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(testLesson));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        Assignment result = assignmentService.createAssignment(1L, testAssignment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLesson()).isEqualTo(testLesson);
        verify(lessonRepository).findById(1L);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should throw exception when lesson not found")
    void shouldThrowExceptionWhenLessonNotFound() {
        // Given
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> assignmentService.createAssignment(999L, testAssignment))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lesson");

        verify(lessonRepository).findById(999L);
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should get assignment by id")
    void shouldGetAssignmentById() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // When
        Assignment result = assignmentService.getAssignmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(assignmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get assignments by lesson")
    void shouldGetAssignmentsByLesson() {
        // Given
        List<Assignment> assignments = Arrays.asList(testAssignment);
        when(assignmentRepository.findByLessonId(1L)).thenReturn(assignments);

        // When
        List<Assignment> result = assignmentService.getAssignmentsByLesson(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(assignmentRepository).findByLessonId(1L);
    }

    @Test
    @DisplayName("Should submit assignment successfully")
    void shouldSubmitAssignmentSuccessfully() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(submissionRepository.existsByAssignmentIdAndStudentId(1L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        Submission result = assignmentService.submitAssignment(1L, 1L, "My submission");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("My submission");
        verify(assignmentRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("Should allow any user to submit assignment")
    void shouldAllowAnyUserToSubmit() {
        // Given
        User teacher = User.builder()
                .id(2L)
                .role(UserRole.TEACHER)
                .build();
        Submission teacherSubmission = Submission.builder()
                .id(2L)
                .assignment(testAssignment)
                .student(teacher)
                .content("Teacher's content")
                .build();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(submissionRepository.existsByAssignmentIdAndStudentId(1L, 2L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(teacherSubmission);

        // When
        Submission result = assignmentService.submitAssignment(1L, 2L, "Teacher's content");

        // Then
        assertThat(result).isNotNull();
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("Should throw exception when student already submitted")
    void shouldThrowExceptionWhenAlreadySubmitted() {
        // Given
        when(submissionRepository.existsByAssignmentIdAndStudentId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> assignmentService.submitAssignment(1L, 1L, "content"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Submission already exists");

        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    @DisplayName("Should grade submission successfully")
    void shouldGradeSubmissionSuccessfully() {
        // Given
        Submission gradedSubmission = Submission.builder()
                .id(1L)
                .assignment(testAssignment)
                .student(testStudent)
                .content("My submission")
                .score(85)
                .feedback("Good work")
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(Submission.class))).thenReturn(gradedSubmission);

        // When
        Submission result = assignmentService.gradeSubmission(1L, 85, "Good work");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(85);
        assertThat(result.getFeedback()).isEqualTo("Good work");
        verify(submissionRepository).findById(1L);
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("Should allow grading with any score value")
    void shouldAllowAnyScoreValue() {
        // Given
        Submission gradedSubmission = Submission.builder()
                .id(1L)
                .assignment(testAssignment)
                .student(testStudent)
                .content("My submission")
                .score(150)
                .feedback("feedback")
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(Submission.class))).thenReturn(gradedSubmission);

        // When
        Submission result = assignmentService.gradeSubmission(1L, 150, "feedback");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(150);
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("Should get submissions by assignment")
    void shouldGetSubmissionsByAssignment() {
        // Given
        List<Submission> submissions = Arrays.asList(testSubmission);
        when(submissionRepository.findByAssignmentId(1L)).thenReturn(submissions);

        // When
        List<Submission> result = assignmentService.getSubmissionsByAssignment(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(submissionRepository).findByAssignmentId(1L);
    }

    @Test
    @DisplayName("Should get submissions by student")
    void shouldGetSubmissionsByStudent() {
        // Given
        List<Submission> submissions = Arrays.asList(testSubmission);
        when(submissionRepository.findByStudentId(1L)).thenReturn(submissions);

        // When
        List<Submission> result = assignmentService.getSubmissionsByStudent(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(submissionRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should get assignment by id")
    void shouldGetAssignmentByIdAgain() {
        // Given
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));

        // When
        Assignment result = assignmentService.getAssignmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Assignment");
        verify(assignmentRepository).findById(1L);
    }
}
