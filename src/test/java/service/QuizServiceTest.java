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

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuizService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Unit Tests")
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerOptionRepository answerOptionRepository;

    @Mock
    private QuizSubmissionRepository quizSubmissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private QuizService quizService;

    private Quiz testQuiz;
    private entity.Module testModule;
    private Question testQuestion;
    private AnswerOption correctOption;
    private AnswerOption wrongOption;
    private User testStudent;
    private QuizSubmission testSubmission;

    @BeforeEach
    void setUp() {
        testModule = entity.Module.builder()
                .id(1L)
                .title("Test Module")
                .orderIndex(1)
                .build();

        testQuiz = Quiz.builder()
                .id(1L)
                .title("Test Quiz")
                .description("Quiz Description")
                .passingScore(70)
                .module(testModule)
                .build();

        correctOption = AnswerOption.builder()
                .id(1L)
                .text("Correct Answer")
                .isCorrect(true)
                .build();

        wrongOption = AnswerOption.builder()
                .id(2L)
                .text("Wrong Answer")
                .isCorrect(false)
                .build();

        testQuestion = Question.builder()
                .id(1L)
                .text("What is 2+2?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .quiz(testQuiz)
                .options(new ArrayList<>(Arrays.asList(correctOption, wrongOption)))
                .build();

        testStudent = User.builder()
                .id(1L)
                .name("Student")
                .email("student@example.com")
                .role(UserRole.STUDENT)
                .build();

        testSubmission = QuizSubmission.builder()
                .id(1L)
                .quiz(testQuiz)
                .student(testStudent)
                .score(80)
                .build();
    }

    @Test
    @DisplayName("Should create quiz successfully")
    void shouldCreateQuizSuccessfully() {
        // Given
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // When
        Quiz result = quizService.createQuiz(1L, testQuiz);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getModule()).isEqualTo(testModule);
        verify(moduleRepository).findById(1L);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw exception when module not found")
    void shouldThrowExceptionWhenModuleNotFound() {
        // Given
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quizService.createQuiz(999L, testQuiz))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Module");

        verify(moduleRepository).findById(999L);
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should get quiz by id")
    void shouldGetQuizById() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));

        // When
        Quiz result = quizService.getQuizById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(quizRepository).findById(1L);
    }

    @Test
    @DisplayName("Should add question to quiz")
    void shouldAddQuestionToQuiz() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        // When
        Question result = quizService.addQuestionToQuiz(1L, testQuestion);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuiz()).isEqualTo(testQuiz);
        verify(quizRepository).findById(1L);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Should add answer option to question")
    void shouldAddAnswerOptionToQuestion() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(answerOptionRepository.save(any(AnswerOption.class))).thenReturn(correctOption);

        // When
        AnswerOption result = quizService.addAnswerOption(1L, correctOption);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestion()).isEqualTo(testQuestion);
        verify(questionRepository).findById(1L);
        verify(answerOptionRepository).save(any(AnswerOption.class));
    }

    @Test
    @DisplayName("Should take quiz and calculate score correctly")
    void shouldTakeQuizAndCalculateScore() {
        // Given
        testQuestion.setOptions(Arrays.asList(correctOption, wrongOption));
        testQuiz.setQuestions(Arrays.asList(testQuestion));

        Map<Long, List<Long>> answers = Map.of(1L, List.of(1L)); // Correct answer

        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 1L)).thenReturn(false);
        when(quizRepository.findByIdWithFullStructure(1L)).thenReturn(Optional.of(testQuiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenReturn(testSubmission);

        // When
        QuizSubmission result = quizService.takeQuiz(1L, 1L, answers);

        // Then
        assertThat(result).isNotNull();
        verify(quizRepository).findByIdWithFullStructure(1L);
        verify(userRepository).findById(1L);
        verify(quizSubmissionRepository).save(any(QuizSubmission.class));
    }

    @Test
    @DisplayName("Should throw exception when student already took quiz")
    void shouldThrowExceptionWhenAlreadyTookQuiz() {
        // Given
        Map<Long, List<Long>> answers = Map.of(1L, List.of(1L));

        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> quizService.takeQuiz(1L, 1L, answers))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Quiz submission already exists");

        verify(quizSubmissionRepository, never()).save(any(QuizSubmission.class));
    }

    @Test
    @DisplayName("Should allow any user to take quiz")
    void shouldAllowAnyUserToTakeQuiz() {
        // Given
        User teacher = User.builder()
                .id(2L)
                .role(UserRole.TEACHER)
                .build();
        testQuestion.setOptions(Arrays.asList(correctOption, wrongOption));
        testQuiz.setQuestions(Arrays.asList(testQuestion));
        Map<Long, List<Long>> answers = Map.of(1L, List.of(1L));

        QuizSubmission teacherSubmission = QuizSubmission.builder()
                .id(2L)
                .quiz(testQuiz)
                .student(teacher)
                .score(100)
                .build();

        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 2L)).thenReturn(false);
        when(quizRepository.findByIdWithFullStructure(1L)).thenReturn(Optional.of(testQuiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenReturn(teacherSubmission);

        // When
        QuizSubmission result = quizService.takeQuiz(1L, 2L, answers);

        // Then
        assertThat(result).isNotNull();
        verify(quizSubmissionRepository).save(any(QuizSubmission.class));
    }

    @Test
    @DisplayName("Should get quiz submissions by student")
    void shouldGetQuizSubmissionsByStudent() {
        // Given
        List<QuizSubmission> submissions = Arrays.asList(testSubmission);
        when(quizSubmissionRepository.findByStudentId(1L)).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizService.getSubmissionsByStudent(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(quizSubmissionRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should get quiz submissions by quiz")
    void shouldGetQuizSubmissionsByQuiz() {
        // Given
        List<QuizSubmission> submissions = Arrays.asList(testSubmission);
        when(quizSubmissionRepository.findByQuizId(1L)).thenReturn(submissions);

        // When
        List<QuizSubmission> result = quizService.getSubmissionsByQuiz(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(quizSubmissionRepository).findByQuizId(1L);
    }

    @Test
    @DisplayName("Should get quiz by id successfully")
    void shouldGetQuizByIdAgain() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));

        // When
        Quiz result = quizService.getQuizById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Quiz");
        verify(quizRepository).findById(1L);
    }
}
