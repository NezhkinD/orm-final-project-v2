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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for QuizService
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
class QuizServiceIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User student;
    private User teacher;
    private Course course;
    private org.example.learningplatform.entity.Module module;

    @BeforeEach
    void setUp() {
        // Clean up
        quizSubmissionRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
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
    }

    @Test
    void testCreateQuiz_Success() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Midterm Exam")
                .description("Test your knowledge")
                .passingScore(70)
                .build();

        // When
        Quiz savedQuiz = quizService.createQuiz(module.getId(), quiz);

        // Then
        assertThat(savedQuiz.getId()).isNotNull();
        assertThat(savedQuiz.getTitle()).isEqualTo("Midterm Exam");
        assertThat(savedQuiz.getModule()).isEqualTo(module);
        assertThat(savedQuiz.getPassingScore()).isEqualTo(70);
    }

    @Test
    void testCreateQuiz_ModuleNotFound_ThrowsException() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Test Quiz")
                .description("Test")
                .passingScore(70)
                .build();

        // When/Then
        assertThatThrownBy(() -> quizService.createQuiz(999L, quiz))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testAddQuestionToQuiz_Success() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Quiz")
                .description("Test quiz")
                .passingScore(70)
                .build();
        Quiz savedQuiz = quizService.createQuiz(module.getId(), quiz);

        Question question = Question.builder()
                .text("What is Java?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();

        // When
        Question savedQuestion = quizService.addQuestionToQuiz(savedQuiz.getId(), question);

        // Then
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getText()).isEqualTo("What is Java?");
        assertThat(savedQuestion.getQuiz()).isEqualTo(savedQuiz);
    }

    @Test
    void testAddAnswerOption_Success() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Quiz")
                .description("Test quiz")
                .passingScore(70)
                .build();
        Quiz savedQuiz = quizService.createQuiz(module.getId(), quiz);

        Question question = Question.builder()
                .text("What is Java?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();
        Question savedQuestion = quizService.addQuestionToQuiz(savedQuiz.getId(), question);

        AnswerOption option = AnswerOption.builder()
                .text("A programming language")
                .isCorrect(true)
                .build();

        // When
        AnswerOption savedOption = quizService.addAnswerOption(savedQuestion.getId(), option);

        // Then
        assertThat(savedOption.getId()).isNotNull();
        assertThat(savedOption.getText()).isEqualTo("A programming language");
        assertThat(savedOption.getIsCorrect()).isTrue();
        assertThat(savedOption.getQuestion()).isEqualTo(savedQuestion);
    }

    @Test
    void testTakeQuiz_Success() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        // Get questions
        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);
        Question q2 = fullQuiz.getQuestions().get(1);

        // Get correct answer IDs
        Long q1CorrectAnswerId = q1.getOptions().stream()
                .filter(AnswerOption::getIsCorrect)
                .findFirst()
                .get()
                .getId();

        Long q2CorrectAnswerId = q2.getOptions().stream()
                .filter(AnswerOption::getIsCorrect)
                .findFirst()
                .get()
                .getId();

        // Student answers (1 correct, 1 wrong)
        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1CorrectAnswerId),
                q2.getId(), List.of(q2.getOptions().get(0).getId()) // wrong answer
        );

        // When
        QuizSubmission submission = quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        // Then
        assertThat(submission.getId()).isNotNull();
        assertThat(submission.getQuiz()).isEqualTo(quiz);
        assertThat(submission.getStudent()).isEqualTo(student);
        assertThat(submission.getTotalQuestions()).isEqualTo(2);
        assertThat(submission.getCorrectAnswers()).isEqualTo(1);
        assertThat(submission.getScore()).isEqualTo(50); // 1/2 = 50%
    }

    @Test
    void testTakeQuiz_AllCorrect() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        // Get questions
        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);
        Question q2 = fullQuiz.getQuestions().get(1);

        // Get correct answer IDs
        Long q1CorrectAnswerId = q1.getCorrectOptions().get(0).getId();
        Long q2CorrectAnswerId = q2.getCorrectOptions().get(0).getId();

        // Student answers (all correct)
        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1CorrectAnswerId),
                q2.getId(), List.of(q2CorrectAnswerId)
        );

        // When
        QuizSubmission submission = quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        // Then
        assertThat(submission.getCorrectAnswers()).isEqualTo(2);
        assertThat(submission.getScore()).isEqualTo(100);
    }

    @Test
    void testTakeQuiz_DuplicateSubmission_ThrowsException() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);

        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1.getOptions().get(0).getId())
        );

        quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        // When/Then
        assertThatThrownBy(() -> quizService.takeQuiz(quiz.getId(), student.getId(), answers))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Quiz submission already exists");
    }

    @Test
    void testGetQuizById_Success() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Find Me Quiz")
                .description("Test")
                .passingScore(70)
                .build();
        Quiz savedQuiz = quizService.createQuiz(module.getId(), quiz);

        // When
        Quiz foundQuiz = quizService.getQuizById(savedQuiz.getId());

        // Then
        assertThat(foundQuiz).isNotNull();
        assertThat(foundQuiz.getTitle()).isEqualTo("Find Me Quiz");
    }

    @Test
    void testGetQuizWithFullStructure_Success() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        // When
        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());

        // Then
        assertThat(fullQuiz.getQuestions()).hasSize(2);
        assertThat(fullQuiz.getQuestions().get(0).getOptions()).hasSize(2);
        assertThat(fullQuiz.getQuestions().get(1).getOptions()).hasSize(2);
    }

    @Test
    void testGetQuizByModule_Success() {
        // Given
        Quiz quiz = Quiz.builder()
                .title("Module Quiz")
                .description("Test")
                .passingScore(70)
                .build();
        quizService.createQuiz(module.getId(), quiz);

        // When
        Quiz foundQuiz = quizService.getQuizByModule(module.getId());

        // Then
        assertThat(foundQuiz).isNotNull();
        assertThat(foundQuiz.getModule()).isEqualTo(module);
    }

    @Test
    void testGetSubmissionsByQuiz_Success() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);

        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1.getOptions().get(0).getId())
        );

        quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        User anotherStudent = User.builder()
                .email("student2@example.com")
                .name("Another Student")
                .role(UserRole.STUDENT)
                .build();
        anotherStudent = userService.createUser(anotherStudent);

        quizService.takeQuiz(quiz.getId(), anotherStudent.getId(), answers);

        // When
        List<QuizSubmission> submissions = quizService.getSubmissionsByQuiz(quiz.getId());

        // Then
        assertThat(submissions).hasSize(2);
    }

    @Test
    void testGetSubmissionsByStudent_Success() {
        // Given
        Quiz quiz1 = createQuizWithQuestions();
        Quiz quiz2 = createQuizWithQuestions();

        Quiz fullQuiz1 = quizService.getQuizWithFullStructure(quiz1.getId());
        Question q1 = fullQuiz1.getQuestions().get(0);

        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1.getOptions().get(0).getId())
        );

        quizService.takeQuiz(quiz1.getId(), student.getId(), answers);

        Quiz fullQuiz2 = quizService.getQuizWithFullStructure(quiz2.getId());
        Question q2 = fullQuiz2.getQuestions().get(0);

        Map<Long, List<Long>> answers2 = Map.of(
                q2.getId(), List.of(q2.getOptions().get(0).getId())
        );

        quizService.takeQuiz(quiz2.getId(), student.getId(), answers2);

        // When
        List<QuizSubmission> submissions = quizService.getSubmissionsByStudent(student.getId());

        // Then
        assertThat(submissions).hasSize(2);
        assertThat(submissions).allMatch(s -> s.getStudent().equals(student));
    }

    @Test
    void testGetQuizAverageScore_Success() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);
        Long correctAnswerId = q1.getCorrectOptions().get(0).getId();

        // Student 1: 50%
        Map<Long, List<Long>> answers1 = Map.of(
                fullQuiz.getQuestions().get(0).getId(), List.of(correctAnswerId)
        );
        quizService.takeQuiz(quiz.getId(), student.getId(), answers1);

        // Student 2: 100%
        User student2 = User.builder()
                .email("student2@example.com")
                .name("Student2 Test")
                .role(UserRole.STUDENT)
                .build();
        student2 = userService.createUser(student2);

        Map<Long, List<Long>> answers2 = Map.of(
                fullQuiz.getQuestions().get(0).getId(), List.of(correctAnswerId),
                fullQuiz.getQuestions().get(1).getId(), List.of(fullQuiz.getQuestions().get(1).getCorrectOptions().get(0).getId())
        );
        quizService.takeQuiz(quiz.getId(), student2.getId(), answers2);

        // When
        Double averageScore = quizService.getQuizAverageScore(quiz.getId());

        // Then
        assertThat(averageScore).isEqualTo(75.0); // (50 + 100) / 2
    }

    @Test
    void testDidStudentPass_Success() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());
        Question q1 = fullQuiz.getQuestions().get(0);
        Question q2 = fullQuiz.getQuestions().get(1);

        // Student gets 100%
        Map<Long, List<Long>> answers = Map.of(
                q1.getId(), List.of(q1.getCorrectOptions().get(0).getId()),
                q2.getId(), List.of(q2.getCorrectOptions().get(0).getId())
        );

        quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        // When
        boolean passed = quizService.didStudentPass(quiz.getId(), student.getId());

        // Then
        assertThat(passed).isTrue(); // passing score is 70, student got 100
    }

    @Test
    void testDidStudentPass_Failed() {
        // Given
        Quiz quiz = createQuizWithQuestions();

        Quiz fullQuiz = quizService.getQuizWithFullStructure(quiz.getId());

        // Student gets 0%
        Map<Long, List<Long>> answers = Map.of(
                fullQuiz.getQuestions().get(0).getId(), List.of(fullQuiz.getQuestions().get(0).getOptions().get(1).getId())
        );

        quizService.takeQuiz(quiz.getId(), student.getId(), answers);

        // When
        boolean passed = quizService.didStudentPass(quiz.getId(), student.getId());

        // Then
        assertThat(passed).isFalse(); // passing score is 70, student got 0
    }

    // ==============================================
    // LAZY LOADING DEMONSTRATION TESTS
    // ==============================================

    /**
     * Test 1: Demonstrates LazyInitializationException for Quiz → Questions → AnswerOptions
     * Shows what happens when accessing lazy questions outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessQuestionsOutsideTransaction_ThrowsException() {
        // Given - create quiz with questions outside transaction
        Long quizId;
        try {
            Quiz quiz = createQuizWithQuestions();
            quizId = quiz.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - try to access lazy questions outside transaction
        Quiz quizOutsideTransaction = quizRepository.findById(quizId).orElseThrow();

        // Then - accessing lazy collection should throw LazyInitializationException
        assertThatThrownBy(() -> quizOutsideTransaction.getQuestions().size())
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    /**
     * Test 2: Demonstrates solution using JOIN FETCH for Quiz → Questions → Options
     * Shows how JOIN FETCH allows accessing nested lazy collections outside transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testLazyLoading_AccessQuestionsWithJoinFetch_Success() {
        // Given - create quiz with questions
        Long quizId;
        try {
            Quiz quiz = createQuizWithQuestions();
            quizId = quiz.getId();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }

        // When - use repository method with JOIN FETCH for full structure
        Quiz quizWithFullStructure = quizRepository.findByIdWithFullStructure(quizId).orElseThrow();

        // Then - can access all nested collections outside transaction
        assertThat(quizWithFullStructure.getQuestions()).isNotNull();
        assertThat(quizWithFullStructure.getQuestions()).hasSize(2);

        // Can even access nested AnswerOptions
        assertThat(quizWithFullStructure.getQuestions().get(0).getOptions()).hasSize(2);
        assertThat(quizWithFullStructure.getQuestions().get(1).getOptions()).hasSize(2);

        // Verify question content
        assertThat(quizWithFullStructure.getQuestions())
                .extracting("text")
                .containsExactlyInAnyOrder("What is 2+2?", "What is 3+3?");
    }

    /**
     * Test 3: Demonstrates solution using @Transactional for Quiz → Questions
     * Shows that lazy loading works within transaction context.
     */
    @Test
    @Transactional
    void testLazyLoading_AccessQuestionsWithinTransaction_Success() {
        // Given - create quiz with questions within transaction
        Quiz quiz = createQuizWithQuestions();

        // When - access quiz within transaction
        Quiz foundQuiz = quizRepository.findById(quiz.getId()).orElseThrow();

        // Then - can access lazy questions and options because we're within transaction
        assertThat(foundQuiz.getQuestions()).isNotNull();
        assertThat(foundQuiz.getQuestions()).hasSize(2);

        // Access nested collections
        Question firstQuestion = foundQuiz.getQuestions().get(0);
        assertThat(firstQuestion.getOptions()).isNotNull();
        assertThat(firstQuestion.getOptions()).hasSize(2);

        // Verify correct answers
        assertThat(firstQuestion.getCorrectOptions()).hasSize(1);
        assertThat(firstQuestion.getCorrectOptions().get(0).getText()).isEqualTo("4");
    }

    /**
     * Test 4: Demonstrates N+1 problem for Quiz → Questions → AnswerOptions
     * Shows how loading multiple quizzes and accessing questions triggers N+1 queries.
     */
    @Test
    @Transactional
    void testNPlusOneProblem_QuizStructure_Demonstration() {
        // Given - create multiple quizzes with questions
        for (int i = 1; i <= 3; i++) {
            // Create a separate module for each quiz to avoid unique constraint violation
            org.example.learningplatform.entity.Module newModule = org.example.learningplatform.entity.Module.builder()
                    .title("N+1 Module " + i)
                    .orderIndex(i)
                    .build();
            newModule.setCourse(course);
            newModule = moduleRepository.save(newModule);

            Quiz quiz = Quiz.builder()
                    .title("N+1 Quiz " + i)
                    .description("Quiz for N+1 test")
                    .passingScore(70)
                    .build();
            Quiz savedQuiz = quizService.createQuiz(newModule.getId(), quiz);

            // Add 2 questions to each quiz
            for (int j = 1; j <= 2; j++) {
                Question question = Question.builder()
                        .text("Question " + j + " of Quiz " + i)
                        .type(QuestionType.SINGLE_CHOICE)
                        .points(10)
                        .build();
                Question savedQuestion = quizService.addQuestionToQuiz(savedQuiz.getId(), question);

                // Add 3 answer options to each question
                for (int k = 1; k <= 3; k++) {
                    AnswerOption option = AnswerOption.builder()
                            .text("Option " + k)
                            .isCorrect(k == 1) // First option is correct
                            .build();
                    quizService.addAnswerOption(savedQuestion.getId(), option);
                }
            }
        }

        // When - demonstrate N+1 problem: loading quizzes then accessing questions
        // This triggers: 1 query for quizzes + N queries for questions
        List<Quiz> quizzes = quizRepository.findAll();

        // Accessing questions triggers additional queries (N+1 problem)
        long totalQuestions = quizzes.stream()
                .mapToLong(q -> q.getQuestions().size())
                .sum();

        // Then - verify data is correct despite N+1 queries
        assertThat(quizzes).hasSizeGreaterThanOrEqualTo(3);
        assertThat(totalQuestions).isGreaterThanOrEqualTo(6); // At least 3 quizzes * 2 questions

        // Now demonstrate the SOLUTION using service method with JOIN FETCH
        // Load each quiz with full structure using JOIN FETCH
        List<Quiz> quizzesWithFullStructure = quizzes.stream()
                .map(q -> quizRepository.findByIdWithFullStructure(q.getId()).orElseThrow())
                .toList();

        // Accessing questions and options now doesn't trigger additional queries
        long totalOptions = quizzesWithFullStructure.stream()
                .flatMap(q -> q.getQuestions().stream())
                .mapToLong(question -> question.getOptions().size())
                .sum();

        // Then - verify same data with better performance
        assertThat(quizzesWithFullStructure).hasSizeGreaterThanOrEqualTo(3);
        assertThat(totalOptions).isGreaterThanOrEqualTo(18); // At least 3 quizzes * 2 questions * 3 options

        // Verify correct answers are accessible
        long totalCorrectOptions = quizzesWithFullStructure.stream()
                .flatMap(q -> q.getQuestions().stream())
                .flatMap(question -> question.getCorrectOptions().stream())
                .count();

        assertThat(totalCorrectOptions).isGreaterThanOrEqualTo(6); // 1 correct per question
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    // Helper method to create a quiz with 2 questions and answers
    private Quiz createQuizWithQuestions() {
        // Create a new module for each quiz to avoid unique constraint violation
        org.example.learningplatform.entity.Module newModule = org.example.learningplatform.entity.Module.builder()
                .title("Module for Quiz " + System.nanoTime())
                .orderIndex(1)
                .build();
        newModule.setCourse(course);
        newModule = moduleRepository.save(newModule);

        Quiz quiz = Quiz.builder()
                .title("Test Quiz")
                .description("Quiz with questions")
                .passingScore(70)
                .build();
        Quiz savedQuiz = quizService.createQuiz(newModule.getId(), quiz);

        // Question 1
        Question q1 = Question.builder()
                .text("What is 2+2?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();
        Question savedQ1 = quizService.addQuestionToQuiz(savedQuiz.getId(), q1);

        AnswerOption q1o1 = AnswerOption.builder()
                .text("4")
                .isCorrect(true)
                .build();
        quizService.addAnswerOption(savedQ1.getId(), q1o1);

        AnswerOption q1o2 = AnswerOption.builder()
                .text("5")
                .isCorrect(false)
                .build();
        quizService.addAnswerOption(savedQ1.getId(), q1o2);

        // Question 2
        Question q2 = Question.builder()
                .text("What is 3+3?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();
        Question savedQ2 = quizService.addQuestionToQuiz(savedQuiz.getId(), q2);

        AnswerOption q2o1 = AnswerOption.builder()
                .text("6")
                .isCorrect(true)
                .build();
        quizService.addAnswerOption(savedQ2.getId(), q2o1);

        AnswerOption q2o2 = AnswerOption.builder()
                .text("7")
                .isCorrect(false)
                .build();
        quizService.addAnswerOption(savedQ2.getId(), q2o2);

        return savedQuiz;
    }
}
