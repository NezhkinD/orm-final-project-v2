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
import dto.ReviewRequest;
import entity.Course;
import entity.CourseReview;
import entity.User;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.CourseRepository;
import repository.CourseReviewRepository;
import repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API endpoints for course review management
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "APIs for managing course reviews and ratings")
public class ReviewController {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    /**
     * Create a new course review
     * POST /api/reviews
     */
    @Operation(
            summary = "Create course review",
            description = "Creates a new review for a course by a student with rating (1-5) and optional comment. Students can only review each course once."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully",
                    content = @Content(schema = @Schema(implementation = CourseReview.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Course or Student not found"),
            @ApiResponse(responseCode = "409", description = "Student already reviewed this course")
    })
    @PostMapping
    public ResponseEntity<CourseReview> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("REST request to create review for course {} by student {}",
                request.getCourseId(), request.getStudentId());

        if (reviewRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new DuplicateResourceException("Review already exists for course " +
                    request.getCourseId() + " by student " + request.getStudentId());
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getStudentId()));

        CourseReview review = CourseReview.builder()
                .course(course)
                .student(student)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        CourseReview savedReview = reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    /**
     * Get review by ID
     * GET /api/reviews/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseReview> getReviewById(@PathVariable Long id) {
        log.info("REST request to get review by id: {}", id);

        CourseReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
        return ResponseEntity.ok(review);
    }

    /**
     * Get all reviews for a course
     * GET /api/reviews/course/{courseId}
     */
    @Operation(
            summary = "Get reviews by course",
            description = "Retrieves all reviews for a specific course"
    )
    @ApiResponse(responseCode = "200", description = "List of reviews for the course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseReview>> getReviewsByCourse(
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        log.info("REST request to get reviews for course {}", courseId);

        List<CourseReview> reviews = reviewRepository.findByCourseId(courseId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get latest reviews for a course
     * GET /api/reviews/course/{courseId}/latest
     */
    @GetMapping("/course/{courseId}/latest")
    public ResponseEntity<List<CourseReview>> getLatestReviewsByCourse(@PathVariable Long courseId) {
        log.info("REST request to get latest reviews for course {}", courseId);

        List<CourseReview> reviews = reviewRepository.findLatestReviewsByCourseId(courseId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get all reviews by a student
     * GET /api/reviews/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseReview>> getReviewsByStudent(@PathVariable Long studentId) {
        log.info("REST request to get reviews by student {}", studentId);

        List<CourseReview> reviews = reviewRepository.findByStudentId(studentId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get review by course and student
     * GET /api/reviews/course/{courseId}/student/{studentId}
     */
    @GetMapping("/course/{courseId}/student/{studentId}")
    public ResponseEntity<CourseReview> getReviewByCourseAndStudent(@PathVariable Long courseId,
                                                                     @PathVariable Long studentId) {
        log.info("REST request to get review for course {} by student {}", courseId, studentId);

        CourseReview review = reviewRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found for course " + courseId + " and student " + studentId));
        return ResponseEntity.ok(review);
    }

    /**
     * Get reviews by rating
     * GET /api/reviews/by-rating?rating=5
     */
    @GetMapping("/by-rating")
    public ResponseEntity<List<CourseReview>> getReviewsByRating(@RequestParam Integer rating) {
        log.info("REST request to get reviews with rating {}", rating);

        List<CourseReview> reviews = reviewRepository.findByRating(rating);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews with minimum rating
     * GET /api/reviews/min-rating?minRating=4
     */
    @GetMapping("/min-rating")
    public ResponseEntity<List<CourseReview>> getReviewsWithMinRating(@RequestParam Integer minRating) {
        log.info("REST request to get reviews with minimum rating {}", minRating);

        List<CourseReview> reviews = reviewRepository.findByRatingGreaterThanEqual(minRating);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get reviews for all courses taught by a teacher
     * GET /api/reviews/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<CourseReview>> getReviewsByTeacher(@PathVariable Long teacherId) {
        log.info("REST request to get reviews for teacher {}", teacherId);

        List<CourseReview> reviews = reviewRepository.findReviewsByTeacherId(teacherId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Update review
     * PUT /api/reviews/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseReview> updateReview(@PathVariable Long id,
                                                      @Valid @RequestBody ReviewRequest request) {
        log.info("REST request to update review with id: {}", id);

        CourseReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        CourseReview updatedReview = reviewRepository.save(review);
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * Delete review
     * DELETE /api/reviews/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("REST request to delete review with id: {}", id);

        CourseReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));

        reviewRepository.delete(review);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get average rating for a course
     * GET /api/reviews/course/{courseId}/average-rating
     */
    @Operation(
            summary = "Get average rating for course",
            description = "Calculates and returns the average rating for a specific course based on all reviews"
    )
    @ApiResponse(responseCode = "200", description = "Average rating (0.0 if no reviews)")
    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<Double> getCourseAverageRating(
            @Parameter(description = "ID of the course") @PathVariable Long courseId) {
        log.info("REST request to get average rating for course {}", courseId);

        Double averageRating = reviewRepository.getAverageRatingByCourseId(courseId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    /**
     * Count reviews for a course
     * GET /api/reviews/course/{courseId}/count
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Long> countReviewsByCourse(@PathVariable Long courseId) {
        log.info("REST request to count reviews for course {}", courseId);

        long count = reviewRepository.countByCourseId(courseId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count reviews by rating for a course
     * GET /api/reviews/course/{courseId}/count-by-rating?rating=5
     */
    @GetMapping("/course/{courseId}/count-by-rating")
    public ResponseEntity<Long> countReviewsByCourseAndRating(@PathVariable Long courseId,
                                                               @RequestParam Integer rating) {
        log.info("REST request to count reviews with rating {} for course {}", rating, courseId);

        long count = reviewRepository.countByCourseIdAndRating(courseId, rating);
        return ResponseEntity.ok(count);
    }
}
