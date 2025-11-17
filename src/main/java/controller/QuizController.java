package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.dto.QuestionRequest;
import org.example.learningplatform.dto.QuizRequest;
import org.example.learningplatform.entity.AnswerOption;
import org.example.learningplatform.entity.Question;
import org.example.learningplatform.entity.Quiz;
import org.example.learningplatform.entity.QuizSubmission;
import org.example.learningplatform.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for quiz management
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Management", description = "APIs for managing quizzes, questions, and quiz submissions")
public class QuizController {

    private final QuizService quizService;

    /**
     * Create quiz for a module
     * POST /api/quizzes
     */
    @Operation(
            summary = "Create quiz",
            description = "Creates a new quiz for a specific module with title, description, time limit, and passing score"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully",
                    content = @Content(schema = @Schema(implementation = Quiz.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Module not found")
    })
    @PostMapping
    public ResponseEntity<Quiz> createQuiz(
            @Parameter(description = "ID of the module") @RequestParam Long moduleId,
            @Valid @RequestBody QuizRequest request) {
        log.info("REST request to create quiz for module {}", moduleId);

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .timeLimit(request.getTimeLimit())
                .passingScore(request.getPassingScore())
                .build();

        Quiz createdQuiz = quizService.createQuiz(moduleId, quiz);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    /**
     * Get quiz by ID
     * GET /api/quizzes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        log.info("REST request to get quiz by id: {}", id);

        Quiz quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get quiz with full structure (questions and options)
     * GET /api/quizzes/{id}/full-structure
     */
    @GetMapping("/{id}/full-structure")
    public ResponseEntity<Quiz> getQuizWithFullStructure(@PathVariable Long id) {
        log.info("REST request to get quiz with full structure by id: {}", id);

        Quiz quiz = quizService.getQuizWithFullStructure(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get quiz by module
     * GET /api/quizzes/module/{moduleId}
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<Quiz> getQuizByModule(@PathVariable Long moduleId) {
        log.info("REST request to get quiz for module {}", moduleId);

        Quiz quiz = quizService.getQuizByModule(moduleId);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Add question to quiz
     * POST /api/quizzes/{quizId}/questions
     */
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<Question> addQuestionToQuiz(@PathVariable Long quizId,
                                                       @Valid @RequestBody QuestionRequest request) {
        log.info("REST request to add question to quiz {}", quizId);

        Question question = Question.builder()
                .text(request.getQuestionText())
                .type(request.getType())
                .points(request.getPoints())
                .build();

        Question addedQuestion = quizService.addQuestionToQuiz(quizId, question);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedQuestion);
    }

    /**
     * Add answer option to question
     * POST /api/quizzes/questions/{questionId}/options
     */
    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<AnswerOption> addAnswerOption(@PathVariable Long questionId,
                                                         @RequestBody Map<String, Object> request) {
        log.info("REST request to add answer option to question {}", questionId);

        AnswerOption answerOption = AnswerOption.builder()
                .text((String) request.get("optionText"))
                .isCorrect((Boolean) request.get("isCorrect"))
                .build();

        AnswerOption addedOption = quizService.addAnswerOption(questionId, answerOption);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedOption);
    }

    /**
     * Take quiz - submit student's answers and calculate score
     * POST /api/quizzes/{quizId}/take
     * Body: { "studentId": 1, "answers": { "1": [1, 2], "2": [3] } }
     */
    @Operation(
            summary = "Take quiz",
            description = "Submits a student's answers for a quiz and automatically calculates the score based on correct answers"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz submitted and graded successfully",
                    content = @Content(schema = @Schema(implementation = QuizSubmission.class))),
            @ApiResponse(responseCode = "404", description = "Quiz or Student not found"),
            @ApiResponse(responseCode = "400", description = "Invalid answer format")
    })
    @PostMapping("/{quizId}/take")
    public ResponseEntity<QuizSubmission> takeQuiz(
            @Parameter(description = "ID of the quiz") @PathVariable Long quizId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Student ID and answers map (questionId -> list of answerOptionIds)",
                    content = @Content(schema = @Schema(example = "{\"studentId\": 1, \"answers\": {\"1\": [1, 2], \"2\": [3]}}"))
            )
            @RequestBody Map<String, Object> request) {
        log.info("REST request to take quiz {}", quizId);

        Long studentId = ((Number) request.get("studentId")).longValue();
        @SuppressWarnings("unchecked")
        Map<Long, List<Long>> answers = (Map<Long, List<Long>>) request.get("answers");

        QuizSubmission submission = quizService.takeQuiz(quizId, studentId, answers);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }

    /**
     * Get quiz submissions by quiz
     * GET /api/quizzes/{quizId}/submissions
     */
    @GetMapping("/{quizId}/submissions")
    public ResponseEntity<List<QuizSubmission>> getSubmissionsByQuiz(@PathVariable Long quizId) {
        log.info("REST request to get submissions for quiz {}", quizId);

        List<QuizSubmission> submissions = quizService.getSubmissionsByQuiz(quizId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Get quiz submissions by student
     * GET /api/quizzes/submissions/student/{studentId}
     */
    @GetMapping("/submissions/student/{studentId}")
    public ResponseEntity<List<QuizSubmission>> getSubmissionsByStudent(@PathVariable Long studentId) {
        log.info("REST request to get quiz submissions for student {}", studentId);

        List<QuizSubmission> submissions = quizService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Get quiz average score
     * GET /api/quizzes/{quizId}/average-score
     */
    @GetMapping("/{quizId}/average-score")
    public ResponseEntity<Double> getQuizAverageScore(@PathVariable Long quizId) {
        log.info("REST request to get average score for quiz {}", quizId);

        Double averageScore = quizService.getQuizAverageScore(quizId);
        return ResponseEntity.ok(averageScore != null ? averageScore : 0.0);
    }

    /**
     * Get student's average score across all quizzes
     * GET /api/quizzes/submissions/student/{studentId}/average-score
     */
    @GetMapping("/submissions/student/{studentId}/average-score")
    public ResponseEntity<Double> getStudentAverageScore(@PathVariable Long studentId) {
        log.info("REST request to get average score for student {}", studentId);

        Double averageScore = quizService.getStudentAverageScore(studentId);
        return ResponseEntity.ok(averageScore != null ? averageScore : 0.0);
    }

    /**
     * Check if student passed quiz
     * GET /api/quizzes/{quizId}/student/{studentId}/passed
     */
    @GetMapping("/{quizId}/student/{studentId}/passed")
    public ResponseEntity<Boolean> didStudentPass(@PathVariable Long quizId,
                                                   @PathVariable Long studentId) {
        log.info("REST request to check if student {} passed quiz {}", studentId, quizId);

        boolean passed = quizService.didStudentPass(quizId, studentId);
        return ResponseEntity.ok(passed);
    }
}
