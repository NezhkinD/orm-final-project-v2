package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import entity.*;
import exception.BusinessException;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing quizzes and quiz submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    /**
     * Create quiz for a module
     */
    @Transactional
    public Quiz createQuiz(Long moduleId, Quiz quiz) {
        log.info("Creating quiz for module {}", moduleId);

        entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));

        quiz.setModule(module);
        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("Quiz created with id: {}", savedQuiz.getId());
        return savedQuiz;
    }

    /**
     * Add question to quiz
     */
    @Transactional
    public Question addQuestionToQuiz(Long quizId, Question question) {
        log.info("Adding question to quiz {}", quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        question.setQuiz(quiz);
        Question savedQuestion = questionRepository.save(question);

        log.info("Question added with id: {}", savedQuestion.getId());
        return savedQuestion;
    }

    /**
     * Add answer option to question
     */
    @Transactional
    public AnswerOption addAnswerOption(Long questionId, AnswerOption answerOption) {
        log.info("Adding answer option to question {}", questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));

        answerOption.setQuestion(question);
        AnswerOption savedOption = answerOptionRepository.save(answerOption);

        log.info("Answer option added with id: {}", savedOption.getId());
        return savedOption;
    }

    /**
     * Take quiz - submit student's answers and calculate score
     */
    @Transactional
    public QuizSubmission takeQuiz(Long quizId, Long studentId, Map<Long, List<Long>> answers) {
        log.info("Student {} taking quiz {}", studentId, quizId);

        // Check if already taken
        if (quizSubmissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new DuplicateResourceException(
                    "Quiz submission already exists for quiz " + quizId + " by student " + studentId);
        }

        // Load quiz with questions
        Quiz quiz = quizRepository.findByIdWithFullStructure(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        // Load questions with their options in a second query
        quizRepository.findQuestionsWithOptionsByQuizId(quizId);

        // Initialize options collections within transaction to avoid LazyInitializationException
        quiz.getQuestions().forEach(q -> q.getOptions().size());

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        // Calculate score
        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        for (Question question : quiz.getQuestions()) {
            List<Long> studentAnswers = answers.get(question.getId());
            if (studentAnswers == null) continue;

            List<Long> correctOptionIds = question.getOptions().stream()
                    .filter(AnswerOption::getIsCorrect)
                    .map(AnswerOption::getId)
                    .toList();

            // Check if student's answers match correct answers
            if (studentAnswers.size() == correctOptionIds.size() &&
                studentAnswers.containsAll(correctOptionIds)) {
                correctAnswers++;
            }
        }

        // Calculate percentage score
        int score = (int) ((correctAnswers * 100.0) / totalQuestions);

        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .score(score)
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .build();

        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);
        log.info("Quiz submission created with id: {}, score: {}", savedSubmission.getId(), score);
        return savedSubmission;
    }

    /**
     * Get quiz by ID
     */
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    /**
     * Get quiz with full structure (questions and options)
     */
    public Quiz getQuizWithFullStructure(Long id) {
        // First, load quiz with questions
        Quiz quiz = quizRepository.findByIdWithFullStructure(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));

        // Then load all questions with their options in a second query
        // This is necessary due to Hibernate limitation with multiple collection fetches
        quizRepository.findQuestionsWithOptionsByQuizId(id);

        // Now the questions and options should be loaded in the persistence context
        // Access them to ensure they're initialized
        quiz.getQuestions().forEach(q -> q.getOptions().size());

        return quiz;
    }

    /**
     * Get quiz by module
     */
    public Quiz getQuizByModule(Long moduleId) {
        return quizRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found for module", moduleId));
    }

    /**
     * Get quiz submissions by quiz
     */
    public List<QuizSubmission> getSubmissionsByQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuizId(quizId);
    }

    /**
     * Get quiz submissions by student
     */
    public List<QuizSubmission> getSubmissionsByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }

    /**
     * Get quiz average score
     */
    public Double getQuizAverageScore(Long quizId) {
        return quizSubmissionRepository.getAverageScoreByQuiz(quizId);
    }

    /**
     * Get student's average score across all quizzes
     */
    public Double getStudentAverageScore(Long studentId) {
        return quizSubmissionRepository.getAverageScoreByStudent(studentId);
    }

    /**
     * Check if student passed quiz
     */
    public boolean didStudentPass(Long quizId, Long studentId) {
        QuizSubmission submission = quizSubmissionRepository
                .findByQuizIdAndStudentId(quizId, studentId)
                .orElseThrow(() -> new BusinessException("Quiz submission not found"));

        Quiz quiz = submission.getQuiz();
        return submission.isPassed(quiz.getPassingScore());
    }
}
