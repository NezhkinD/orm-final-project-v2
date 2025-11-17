package org.example.learningplatform.service;

import org.example.learningplatform.entity.*;
import org.example.learningplatform.exception.DuplicateResourceException;
import org.example.learningplatform.exception.ResourceNotFoundException;
import org.example.learningplatform.repository.*;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AssignmentService
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
class AssignmentServiceIntegrationTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private User student;
    private User teacher;
    private Course course;
    private org.example.learningplatform.entity.Module module;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        // Clean up
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        lessonRepository.deleteAll();
        moduleRepository.deleteAll();
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
        Category category = Category.builder()
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

        // Create module
        module = org.example.learningplatform.entity.Module.builder()
                .title("Test Module")
                .description("Test module description")
                .orderIndex(1)
                .build();
        module = courseService.addModuleToCourse(course.getId(), module);

        // Create lesson
        lesson = Lesson.builder()
                .title("Test Lesson")
                .content("Test lesson content")
                .orderIndex(1)
                .durationMinutes(60)
                .build();
        lesson = courseService.addLessonToModule(module.getId(), lesson);
    }

    @Test
    void testCreateAssignment_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework 1")
                .description("Complete exercises 1-5")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();

        // When
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        // Then
        assertThat(savedAssignment.getId()).isNotNull();
        assertThat(savedAssignment.getTitle()).isEqualTo("Homework 1");
        assertThat(savedAssignment.getLesson()).isEqualTo(lesson);
        assertThat(savedAssignment.getMaxScore()).isEqualTo(100);
    }

    @Test
    void testCreateAssignment_LessonNotFound_ThrowsException() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework")
                .description("Test")
                .maxScore(100)
                .build();

        // When/Then
        assertThatThrownBy(() -> assignmentService.createAssignment(999L, assignment))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testSubmitAssignment_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework 1")
                .description("Complete exercises")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        // When
        Submission submission = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student.getId(),
                "My solution to the homework"
        );

        // Then
        assertThat(submission.getId()).isNotNull();
        assertThat(submission.getAssignment()).isEqualTo(savedAssignment);
        assertThat(submission.getStudent()).isEqualTo(student);
        assertThat(submission.getContent()).isEqualTo("My solution to the homework");
        assertThat(submission.getScore()).isNull();
    }

    @Test
    void testSubmitAssignment_DuplicateSubmission_ThrowsException() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework 1")
                .description("Complete exercises")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        assignmentService.submitAssignment(savedAssignment.getId(), student.getId(), "First submission");

        // When/Then
        assertThatThrownBy(() -> assignmentService.submitAssignment(
                savedAssignment.getId(),
                student.getId(),
                "Second submission"
        ))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Submission already exists");
    }

    @Test
    void testGradeSubmission_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework 1")
                .description("Complete exercises")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        Submission submission = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student.getId(),
                "My solution"
        );

        // When
        Submission gradedSubmission = assignmentService.gradeSubmission(
                submission.getId(),
                85,
                "Good work! Some minor improvements needed."
        );

        // Then
        assertThat(gradedSubmission.getScore()).isEqualTo(85);
        assertThat(gradedSubmission.getFeedback()).isEqualTo("Good work! Some minor improvements needed.");
    }

    @Test
    void testGetAssignmentById_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Find Me Assignment")
                .description("Test assignment")
                .maxScore(100)
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        // When
        Assignment foundAssignment = assignmentService.getAssignmentById(savedAssignment.getId());

        // Then
        assertThat(foundAssignment).isNotNull();
        assertThat(foundAssignment.getTitle()).isEqualTo("Find Me Assignment");
    }

    @Test
    void testGetAssignmentById_NotFound_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> assignmentService.getAssignmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGetAssignmentsByLesson_Success() {
        // Given
        Assignment assignment1 = Assignment.builder()
                .title("Homework 1")
                .description("Exercise 1")
                .maxScore(100)
                .build();
        assignmentService.createAssignment(lesson.getId(), assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Homework 2")
                .description("Exercise 2")
                .maxScore(100)
                .build();
        assignmentService.createAssignment(lesson.getId(), assignment2);

        // When
        List<Assignment> assignments = assignmentService.getAssignmentsByLesson(lesson.getId());

        // Then
        assertThat(assignments).hasSize(2);
        assertThat(assignments).extracting("title")
                .containsExactlyInAnyOrder("Homework 1", "Homework 2");
    }

    @Test
    void testGetSubmissionById_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework")
                .description("Test")
                .maxScore(100)
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        Submission submission = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student.getId(),
                "My work"
        );

        // When
        Submission foundSubmission = assignmentService.getSubmissionById(submission.getId());

        // Then
        assertThat(foundSubmission).isNotNull();
        assertThat(foundSubmission.getContent()).isEqualTo("My work");
    }

    @Test
    void testGetSubmissionsByAssignment_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework")
                .description("Test")
                .maxScore(100)
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        assignmentService.submitAssignment(savedAssignment.getId(), student.getId(), "Student 1 work");

        User anotherStudent = User.builder()
                .email("student2@example.com")
                .name("Another Student")
                .role(UserRole.STUDENT)
                .build();
        anotherStudent = userService.createUser(anotherStudent);
        assignmentService.submitAssignment(savedAssignment.getId(), anotherStudent.getId(), "Student 2 work");

        // When
        List<Submission> submissions = assignmentService.getSubmissionsByAssignment(savedAssignment.getId());

        // Then
        assertThat(submissions).hasSize(2);
    }

    @Test
    void testGetSubmissionsByStudent_Success() {
        // Given
        Assignment assignment1 = Assignment.builder()
                .title("Homework 1")
                .description("Test 1")
                .maxScore(100)
                .build();
        Assignment savedAssignment1 = assignmentService.createAssignment(lesson.getId(), assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Homework 2")
                .description("Test 2")
                .maxScore(100)
                .build();
        Assignment savedAssignment2 = assignmentService.createAssignment(lesson.getId(), assignment2);

        assignmentService.submitAssignment(savedAssignment1.getId(), student.getId(), "Work 1");
        assignmentService.submitAssignment(savedAssignment2.getId(), student.getId(), "Work 2");

        // When
        List<Submission> submissions = assignmentService.getSubmissionsByStudent(student.getId());

        // Then
        assertThat(submissions).hasSize(2);
        assertThat(submissions).allMatch(s -> s.getStudent().equals(student));
    }

    @Test
    void testGetUngradedSubmissions_Success() {
        // Given
        Assignment assignment = Assignment.builder()
                .title("Homework")
                .description("Test")
                .maxScore(100)
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        Submission submission1 = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student.getId(),
                "Work 1"
        );

        User anotherStudent = User.builder()
                .email("student2@example.com")
                .name("Another Student")
                .role(UserRole.STUDENT)
                .build();
        anotherStudent = userService.createUser(anotherStudent);

        Submission submission2 = assignmentService.submitAssignment(
                savedAssignment.getId(),
                anotherStudent.getId(),
                "Work 2"
        );

        // Grade one submission
        assignmentService.gradeSubmission(submission1.getId(), 90, "Great!");

        // When
        List<Submission> ungradedSubmissions = assignmentService.getUngradedSubmissions();

        // Then
        assertThat(ungradedSubmissions).hasSize(1);
        assertThat(ungradedSubmissions.get(0).getId()).isEqualTo(submission2.getId());
    }

    @Test
    void testGetStudentAverageScore_Success() {
        // Given
        Assignment assignment1 = Assignment.builder()
                .title("Homework 1")
                .description("Test 1")
                .maxScore(100)
                .build();
        Assignment savedAssignment1 = assignmentService.createAssignment(lesson.getId(), assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Homework 2")
                .description("Test 2")
                .maxScore(100)
                .build();
        Assignment savedAssignment2 = assignmentService.createAssignment(lesson.getId(), assignment2);

        Submission submission1 = assignmentService.submitAssignment(
                savedAssignment1.getId(),
                student.getId(),
                "Work 1"
        );
        assignmentService.gradeSubmission(submission1.getId(), 80, "Good");

        Submission submission2 = assignmentService.submitAssignment(
                savedAssignment2.getId(),
                student.getId(),
                "Work 2"
        );
        assignmentService.gradeSubmission(submission2.getId(), 90, "Excellent");

        // When
        Double averageScore = assignmentService.getStudentAverageScore(student.getId());

        // Then
        assertThat(averageScore).isEqualTo(85.0);
    }

    @Test
    void testGetStudentAverageScore_NoGradedSubmissions_ReturnsNull() {
        // When
        Double averageScore = assignmentService.getStudentAverageScore(student.getId());

        // Then
        assertThat(averageScore).isNull();
    }

    // ==============================================
    // LAZY LOADING DEMONSTRATION TESTS
    // ==============================================

    /**
     * Test 1: Demonstrates LazyInitializationException for Assignment → Submissions
     * Shows what happens when accessing lazy submissions outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessSubmissionsOutsideTransaction_ThrowsException() {
        // Given - create assignment with submissions
        Long assignmentId;
        try {
            Assignment assignment = Assignment.builder()
                    .title("Lazy Assignment")
                    .description("Assignment for lazy loading test")
                    .maxScore(100)
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .build();
            Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

            assignmentService.submitAssignment(savedAssignment.getId(), student.getId(), "Student work");

            assignmentId = savedAssignment.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - try to access lazy submissions outside transaction
        Assignment assignmentOutsideTransaction = assignmentRepository.findById(assignmentId).orElseThrow();

        // Then - accessing lazy collection should throw LazyInitializationException
        assertThatThrownBy(() -> assignmentOutsideTransaction.getSubmissions().size())
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    /**
     * Test 2: Demonstrates solution using JOIN FETCH for Assignment → Submissions
     * Shows how JOIN FETCH allows accessing lazy collections outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessSubmissionsWithJoinFetch_Success() {
        // Given - create assignment with multiple submissions
        Long assignmentId;
        try {
            Assignment assignment = Assignment.builder()
                    .title("Fetch Assignment")
                    .description("Assignment for fetch test")
                    .maxScore(100)
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .build();
            Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

            // Multiple students submit
            assignmentService.submitAssignment(savedAssignment.getId(), student.getId(), "Student 1 work");

            User student2 = User.builder()
                    .email("fetch.student2@example.com")
                    .name("Student 2")
                    .role(UserRole.STUDENT)
                    .build();
            User savedStudent2 = userService.createUser(student2);
            assignmentService.submitAssignment(savedAssignment.getId(), savedStudent2.getId(), "Student 2 work");

            User student3 = User.builder()
                    .email("fetch.student3@example.com")
                    .name("Student 3")
                    .role(UserRole.STUDENT)
                    .build();
            User savedStudent3 = userService.createUser(student3);
            assignmentService.submitAssignment(savedAssignment.getId(), savedStudent3.getId(), "Student 3 work");

            assignmentId = savedAssignment.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - use repository method with JOIN FETCH
        Assignment assignmentWithSubmissions = assignmentRepository.findByIdWithSubmissions(assignmentId).orElseThrow();

        // Then - can access submissions outside transaction
        assertThat(assignmentWithSubmissions.getSubmissions()).isNotNull();
        assertThat(assignmentWithSubmissions.getSubmissions()).hasSize(3);
        assertThat(assignmentWithSubmissions.getSubmissions())
                .extracting(submission -> submission.getContent())
                .containsExactlyInAnyOrder("Student 1 work", "Student 2 work", "Student 3 work");
    }

    /**
     * Test 3: Demonstrates solution using @Transactional for Assignment → Submissions
     * Shows that lazy loading works within transaction context.
     */
    @Test
    @Transactional
    void testLazyLoading_AccessSubmissionsWithinTransaction_Success() {
        // Given - create assignment with submissions
        Assignment assignment = Assignment.builder()
                .title("Transactional Assignment")
                .description("Assignment for transaction test")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

        assignmentService.submitAssignment(savedAssignment.getId(), student.getId(), "My homework");

        User student2 = User.builder()
                .email("trans.student2@example.com")
                .name("Student 2")
                .role(UserRole.STUDENT)
                .build();
        User savedStudent2 = userService.createUser(student2);
        assignmentService.submitAssignment(savedAssignment.getId(), savedStudent2.getId(), "Another homework");

        // When - access assignment within transaction
        Assignment foundAssignment = assignmentRepository.findById(savedAssignment.getId()).orElseThrow();

        // Then - can access lazy submissions because we're within transaction
        assertThat(foundAssignment.getSubmissions()).isNotNull();
        assertThat(foundAssignment.getSubmissions()).hasSize(2);
        assertThat(foundAssignment.getSubmissions())
                .extracting(submission -> submission.getContent())
                .containsExactlyInAnyOrder("My homework", "Another homework");
    }

    /**
     * Test 4: Demonstrates N+1 problem for Assignment → Submissions
     * Shows how loading multiple assignments and accessing submissions triggers N+1 queries.
     */
    @Test
    @Transactional
    void testNPlusOneProblem_Submissions_Demonstration() {
        // Given - create multiple assignments with submissions
        for (int i = 1; i <= 3; i++) {
            Assignment assignment = Assignment.builder()
                    .title("N+1 Assignment " + i)
                    .description("Assignment for N+1 test")
                    .maxScore(100)
                    .dueDate(LocalDateTime.now().plusDays(7))
                    .build();
            Assignment savedAssignment = assignmentService.createAssignment(lesson.getId(), assignment);

            // Each assignment gets 2 submissions
            for (int j = 1; j <= 2; j++) {
                User studentForSubmission = User.builder()
                        .email("n1.student.a" + i + ".s" + j + "@example.com")
                        .name("Student " + j + " for Assignment " + i)
                        .role(UserRole.STUDENT)
                        .build();
                User savedStudent = userService.createUser(studentForSubmission);
                assignmentService.submitAssignment(
                        savedAssignment.getId(),
                        savedStudent.getId(),
                        "Submission " + j + " for Assignment " + i
                );
            }
        }

        // When - demonstrate N+1 problem: loading assignments then accessing submissions
        // This triggers: 1 query for assignments + N queries for submissions
        List<Assignment> assignments = assignmentRepository.findByLessonId(lesson.getId());

        // Accessing submissions triggers additional queries (N+1 problem)
        long totalSubmissions = assignments.stream()
                .mapToLong(a -> a.getSubmissions().size())
                .sum();

        // Then - verify data is correct despite N+1 queries
        assertThat(assignments).hasSize(3);
        assertThat(totalSubmissions).isEqualTo(6); // 3 assignments * 2 submissions each

        // Now demonstrate the SOLUTION using JOIN FETCH
        // Load each assignment with submissions using JOIN FETCH
        List<Assignment> assignmentsWithSubmissions = assignments.stream()
                .map(a -> assignmentRepository.findByIdWithSubmissions(a.getId()).orElseThrow())
                .toList();

        // Accessing submissions now doesn't trigger additional queries
        long totalSubmissionsWithFetch = assignmentsWithSubmissions.stream()
                .mapToLong(a -> a.getSubmissions().size())
                .sum();

        // Can even access nested student data without additional queries
        long uniqueStudents = assignmentsWithSubmissions.stream()
                .flatMap(a -> a.getSubmissions().stream())
                .map(Submission::getStudent)
                .distinct()
                .count();

        // Then - verify same data with better performance
        assertThat(assignmentsWithSubmissions).hasSize(3);
        assertThat(totalSubmissionsWithFetch).isEqualTo(6);
        assertThat(uniqueStudents).isEqualTo(6); // 6 different students submitted
    }
}
