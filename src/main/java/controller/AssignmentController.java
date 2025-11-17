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
import dto.AssignmentRequest;
import dto.SubmissionRequest;
import entity.Assignment;
import entity.Submission;
import service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assignment Management", description = "APIs for managing assignments and student submissions")
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Create assignment for a lesson
     * POST /api/assignments
     */
    @Operation(
            summary = "Create assignment",
            description = "Creates a new assignment for a course with title, description, due date, and maximum score"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully",
                    content = @Content(schema = @Schema(implementation = Assignment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        log.info("REST request to create assignment: {}", request.getTitle());

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore())
                .build();

        Assignment createdAssignment = assignmentService.createAssignment(request.getCourseId(), assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
    }

    /**
     * Get assignment by ID
     * GET /api/assignments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        log.info("REST request to get assignment by id: {}", id);

        Assignment assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(assignment);
    }

    /**
     * Get assignments by lesson
     * GET /api/assignments/lesson/{lessonId}
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<Assignment>> getAssignmentsByLesson(@PathVariable Long lessonId) {
        log.info("REST request to get assignments for lesson {}", lessonId);

        List<Assignment> assignments = assignmentService.getAssignmentsByLesson(lessonId);
        return ResponseEntity.ok(assignments);
    }

    /**
     * Submit assignment solution by student
     * POST /api/assignments/submit
     */
    @Operation(
            summary = "Submit assignment",
            description = "Allows a student to submit their solution/answer for an assignment"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment submitted successfully",
                    content = @Content(schema = @Schema(implementation = Submission.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Assignment or Student not found"),
            @ApiResponse(responseCode = "409", description = "Student already submitted this assignment")
    })
    @PostMapping("/submit")
    public ResponseEntity<Submission> submitAssignment(@Valid @RequestBody SubmissionRequest request) {
        log.info("REST request to submit assignment {} by student {}",
                request.getAssignmentId(), request.getStudentId());

        Submission submission = assignmentService.submitAssignment(
                request.getAssignmentId(),
                request.getStudentId(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }

    /**
     * Grade submission
     * PATCH /api/assignments/submissions/{submissionId}/grade
     */
    @Operation(
            summary = "Grade submission",
            description = "Allows a teacher to grade a student's submission by providing a score and optional feedback"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Submission graded successfully",
                    content = @Content(schema = @Schema(implementation = Submission.class))),
            @ApiResponse(responseCode = "404", description = "Submission not found"),
            @ApiResponse(responseCode = "400", description = "Invalid score (must be between 0 and maxScore)")
    })
    @PatchMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<Submission> gradeSubmission(
            @Parameter(description = "ID of the submission to grade") @PathVariable Long submissionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Score and optional feedback",
                    content = @Content(schema = @Schema(example = "{\"score\": 95, \"feedback\": \"Excellent work!\"}"))
            )
            @RequestBody Map<String, Object> request) {
        log.info("REST request to grade submission {}", submissionId);

        Integer score = (Integer) request.get("score");
        String feedback = (String) request.get("feedback");

        Submission gradedSubmission = assignmentService.gradeSubmission(submissionId, score, feedback);
        return ResponseEntity.ok(gradedSubmission);
    }

    /**
     * Get submission by ID
     * GET /api/assignments/submissions/{id}
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long id) {
        log.info("REST request to get submission by id: {}", id);

        Submission submission = assignmentService.getSubmissionById(id);
        return ResponseEntity.ok(submission);
    }

    /**
     * Get submissions by assignment
     * GET /api/assignments/{assignmentId}/submissions
     */
    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<List<Submission>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        log.info("REST request to get submissions for assignment {}", assignmentId);

        List<Submission> submissions = assignmentService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Get submissions by student
     * GET /api/assignments/submissions/student/{studentId}
     */
    @GetMapping("/submissions/student/{studentId}")
    public ResponseEntity<List<Submission>> getSubmissionsByStudent(@PathVariable Long studentId) {
        log.info("REST request to get submissions for student {}", studentId);

        List<Submission> submissions = assignmentService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    /**
     * Get ungraded submissions
     * GET /api/assignments/submissions/ungraded
     */
    @GetMapping("/submissions/ungraded")
    public ResponseEntity<List<Submission>> getUngradedSubmissions() {
        log.info("REST request to get ungraded submissions");

        List<Submission> submissions = assignmentService.getUngradedSubmissions();
        return ResponseEntity.ok(submissions);
    }

    /**
     * Get student's average score
     * GET /api/assignments/submissions/student/{studentId}/average-score
     */
    @GetMapping("/submissions/student/{studentId}/average-score")
    public ResponseEntity<Double> getStudentAverageScore(@PathVariable Long studentId) {
        log.info("REST request to get average score for student {}", studentId);

        Double averageScore = assignmentService.getStudentAverageScore(studentId);
        return ResponseEntity.ok(averageScore != null ? averageScore : 0.0);
    }
}
