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
import dto.EnrollmentRequest;
import entity.Enrollment;
import service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for enrollment management
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment Management", description = "APIs for managing student enrollments in courses")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll a student in a course
     * POST /api/enrollments
     */
    @Operation(
            summary = "Enroll student in course",
            description = "Creates a new enrollment record for a student in a specific course. Prevents duplicate enrollments."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Student enrolled successfully",
                    content = @Content(schema = @Schema(implementation = Enrollment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Student or Course not found"),
            @ApiResponse(responseCode = "409", description = "Student already enrolled in this course")
    })
    @PostMapping
    public ResponseEntity<Enrollment> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        log.info("REST request to enroll student {} in course {}",
                request.getStudentId(), request.getCourseId());

        Enrollment enrollment = enrollmentService.enrollStudent(
                request.getStudentId(),
                request.getCourseId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /**
     * Unenroll a student from a course
     * DELETE /api/enrollments/student/{studentId}/course/{courseId}
     */
    @Operation(
            summary = "Unenroll student from course",
            description = "Removes a student's enrollment from a course"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Student unenrolled successfully"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    @DeleteMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Void> unenrollStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId,
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        log.info("REST request to unenroll student {} from course {}", studentId, courseId);

        enrollmentService.unenrollStudent(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get enrollment by ID
     * GET /api/enrollments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Long id) {
        log.info("REST request to get enrollment by id: {}", id);

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Get enrollment by student and course
     * GET /api/enrollments/student/{studentId}/course/{courseId}
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Enrollment> getEnrollmentByStudentAndCourse(@PathVariable Long studentId,
                                                                       @PathVariable Long courseId) {
        log.info("REST request to get enrollment for student {} and course {}", studentId, courseId);

        Enrollment enrollment = enrollmentService.getEnrollmentByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Get all enrollments for a student
     * GET /api/enrollments/student/{studentId}
     */
    @Operation(
            summary = "Get enrollments by student",
            description = "Retrieves all enrollment records for a specific student"
    )
    @ApiResponse(responseCode = "200", description = "List of enrollments for the student")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(
            @Parameter(description = "ID of the student") @PathVariable Long studentId) {
        log.info("REST request to get all enrollments for student {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get active enrollments for a student
     * GET /api/enrollments/student/{studentId}/active
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<Enrollment>> getActiveEnrollmentsByStudent(@PathVariable Long studentId) {
        log.info("REST request to get active enrollments for student {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get completed enrollments for a student
     * GET /api/enrollments/student/{studentId}/completed
     */
    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<List<Enrollment>> getCompletedEnrollmentsByStudent(@PathVariable Long studentId) {
        log.info("REST request to get completed enrollments for student {}", studentId);

        List<Enrollment> enrollments = enrollmentService.getCompletedEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get all enrollments for a course
     * GET /api/enrollments/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        log.info("REST request to get all enrollments for course {}", courseId);

        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get active enrollments for a course
     * GET /api/enrollments/course/{courseId}/active
     */
    @GetMapping("/course/{courseId}/active")
    public ResponseEntity<List<Enrollment>> getActiveEnrollmentsByCourse(@PathVariable Long courseId) {
        log.info("REST request to get active enrollments for course {}", courseId);

        List<Enrollment> enrollments = enrollmentService.getActiveEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Update enrollment progress
     * PATCH /api/enrollments/{enrollmentId}/progress
     */
    @Operation(
            summary = "Update enrollment progress",
            description = "Updates the progress percentage for a student's enrollment in a course (0-100%)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid progress percentage")
    })
    @PatchMapping("/{enrollmentId}/progress")
    public ResponseEntity<Enrollment> updateProgress(
            @Parameter(description = "ID of the enrollment") @PathVariable Long enrollmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Progress percentage (0-100)",
                    content = @Content(schema = @Schema(example = "{\"progressPercentage\": 75}"))
            )
            @RequestBody Map<String, Integer> request) {
        log.info("REST request to update progress for enrollment {}", enrollmentId);

        Integer progressPercentage = request.get("progressPercentage");
        Enrollment enrollment = enrollmentService.updateProgress(enrollmentId, progressPercentage);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Complete enrollment
     * POST /api/enrollments/{enrollmentId}/complete
     */
    @PostMapping("/{enrollmentId}/complete")
    public ResponseEntity<Enrollment> completeEnrollment(@PathVariable Long enrollmentId,
                                                          @RequestBody(required = false) Map<String, Double> request) {
        log.info("REST request to complete enrollment {}", enrollmentId);

        Double finalGrade = request != null ? request.get("finalGrade") : null;
        Enrollment enrollment = enrollmentService.completeEnrollment(enrollmentId, finalGrade);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Check if student is enrolled in course
     * GET /api/enrollments/check?studentId=1&courseId=2
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> isStudentEnrolled(@RequestParam Long studentId,
                                                      @RequestParam Long courseId) {
        log.info("REST request to check if student {} is enrolled in course {}", studentId, courseId);

        boolean isEnrolled = enrollmentService.isStudentEnrolled(studentId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Count total enrollments for student
     * GET /api/enrollments/student/{studentId}/count
     */
    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<Long> countEnrollmentsByStudent(@PathVariable Long studentId) {
        log.info("REST request to count enrollments for student {}", studentId);

        long count = enrollmentService.countEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count active enrollments for student
     * GET /api/enrollments/student/{studentId}/active-count
     */
    @GetMapping("/student/{studentId}/active-count")
    public ResponseEntity<Long> countActiveEnrollmentsByStudent(@PathVariable Long studentId) {
        log.info("REST request to count active enrollments for student {}", studentId);

        long count = enrollmentService.countActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count enrollments for course
     * GET /api/enrollments/course/{courseId}/count
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countEnrollmentsByCourse(@PathVariable Long courseId) {
        log.info("REST request to count enrollments for course {}", courseId);

        long count = enrollmentService.countEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(count);
    }
}
