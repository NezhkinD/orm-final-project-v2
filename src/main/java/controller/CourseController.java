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
import org.example.learningplatform.dto.CourseRequest;
import org.example.learningplatform.dto.CourseResponse;
import org.example.learningplatform.dto.ErrorResponse;
import org.example.learningplatform.entity.Course;
import org.example.learningplatform.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST API endpoints for course management
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Management", description = "APIs for managing courses, modules, lessons, and course structure")
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course
     * POST /api/courses
     */
    @Operation(
            summary = "Create a new course",
            description = "Creates a new course with the specified title, description, duration, and assigns it to a teacher and category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Teacher or Category not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        log.info("REST request to create course: {}", request.getTitle());

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration() != null ? request.getDuration().toString() : null)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Course createdCourse = courseService.createCourse(course, request.getTeacherId(), request.getCategoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toCourseResponse(createdCourse));
    }

    /**
     * Get course by ID
     * GET /api/courses/{id}
     */
    @Operation(
            summary = "Get course by ID",
            description = "Retrieves a course by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course found",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(
            @Parameter(description = "ID of the course to retrieve") @PathVariable Long id) {
        log.info("REST request to get course by id: {}", id);

        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(toCourseResponse(course));
    }

    /**
     * Get course with modules
     * GET /api/courses/{id}/with-modules
     */
    @Operation(
            summary = "Get course with modules",
            description = "Retrieves a course with all its modules (demonstrates JOIN FETCH to avoid lazy loading issues)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course with modules found"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @GetMapping("/{id}/with-modules")
    public ResponseEntity<Course> getCourseWithModules(
            @Parameter(description = "ID of the course") @PathVariable Long id) {
        log.info("REST request to get course with modules by id: {}", id);

        Course course = courseService.getCourseWithModules(id);
        return ResponseEntity.ok(course);
    }

    /**
     * Get course with full structure (modules and lessons)
     * GET /api/courses/{id}/full-structure
     */
    @GetMapping("/{id}/full-structure")
    public ResponseEntity<Course> getCourseWithFullStructure(@PathVariable Long id) {
        log.info("REST request to get course with full structure by id: {}", id);

        Course course = courseService.getCourseWithFullStructure(id);
        return ResponseEntity.ok(course);
    }

    /**
     * Get all courses
     * GET /api/courses
     */
    @Operation(
            summary = "Get all courses",
            description = "Retrieves all courses with their associated teachers"
    )
    @ApiResponse(responseCode = "200", description = "List of all courses")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        log.info("REST request to get all courses");

        List<Course> courses = courseService.getAllCoursesWithTeachers();
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get courses by category
     * GET /api/courses/by-category/{categoryId}
     */
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable Long categoryId) {
        log.info("REST request to get courses by category id: {}", categoryId);

        List<Course> courses = courseService.getCoursesByCategory(categoryId);
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get courses by teacher
     * GET /api/courses/by-teacher/{teacherId}
     */
    @GetMapping("/by-teacher/{teacherId}")
    public ResponseEntity<List<CourseResponse>> getCoursesByTeacher(@PathVariable Long teacherId) {
        log.info("REST request to get courses by teacher id: {}", teacherId);

        List<Course> courses = courseService.getCoursesByTeacher(teacherId);
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Search courses by title
     * GET /api/courses/search?title=Java
     */
    @Operation(
            summary = "Search courses by title",
            description = "Finds courses whose titles contain the specified search string (case-insensitive)"
    )
    @ApiResponse(responseCode = "200", description = "List of matching courses")
    @GetMapping("/search")
    public ResponseEntity<List<CourseResponse>> searchCourses(
            @Parameter(description = "Search string for course title") @RequestParam String title) {
        log.info("REST request to search courses by title: {}", title);

        List<Course> courses = courseService.searchCoursesByTitle(title);
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get courses by tag
     * GET /api/courses/by-tag?tagName=programming
     */
    @GetMapping("/by-tag")
    public ResponseEntity<List<CourseResponse>> getCoursesByTag(@RequestParam String tagName) {
        log.info("REST request to get courses by tag: {}", tagName);

        List<Course> courses = courseService.getCoursesByTag(tagName);
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get popular courses
     * GET /api/courses/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CourseResponse>> getPopularCourses() {
        log.info("REST request to get popular courses");

        List<Course> courses = courseService.getPopularCourses();
        List<CourseResponse> response = courses.stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Update course
     * PUT /api/courses/{id}
     */
    @Operation(
            summary = "Update course",
            description = "Updates an existing course's information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @Parameter(description = "ID of the course to update") @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        log.info("REST request to update course with id: {}", id);

        Course courseDetails = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration() != null ? request.getDuration().toString() : null)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Course updatedCourse = courseService.updateCourse(id, courseDetails);
        return ResponseEntity.ok(toCourseResponse(updatedCourse));
    }

    /**
     * Delete course
     * DELETE /api/courses/{id}
     */
    @Operation(
            summary = "Delete course",
            description = "Deletes a course by its ID (this will cascade delete related modules, lessons, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @Parameter(description = "ID of the course to delete") @PathVariable Long id) {
        log.info("REST request to delete course with id: {}", id);

        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add tags to course
     * POST /api/courses/{courseId}/tags
     */
    @PostMapping("/{courseId}/tags")
    public ResponseEntity<Course> addTagsToCourse(@PathVariable Long courseId,
                                                   @RequestBody Set<String> tagNames) {
        log.info("REST request to add tags to course id: {}", courseId);

        Course course = courseService.addTagsToCourse(courseId, tagNames);
        return ResponseEntity.ok(course);
    }

    /**
     * Remove tag from course
     * DELETE /api/courses/{courseId}/tags/{tagId}
     */
    @DeleteMapping("/{courseId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromCourse(@PathVariable Long courseId, @PathVariable Long tagId) {
        log.info("REST request to remove tag {} from course {}", tagId, courseId);

        courseService.removeTagFromCourse(courseId, tagId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get course average rating
     * GET /api/courses/{courseId}/average-rating
     */
    @GetMapping("/{courseId}/average-rating")
    public ResponseEntity<Double> getCourseAverageRating(@PathVariable Long courseId) {
        log.info("REST request to get average rating for course id: {}", courseId);

        Double averageRating = courseService.getCourseAverageRating(courseId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    /**
     * Get active enrollments count
     * GET /api/courses/{courseId}/active-enrollments-count
     */
    @GetMapping("/{courseId}/active-enrollments-count")
    public ResponseEntity<Long> getActiveEnrollmentsCount(@PathVariable Long courseId) {
        log.info("REST request to get active enrollments count for course id: {}", courseId);

        long count = courseService.countActiveEnrollments(courseId);
        return ResponseEntity.ok(count);
    }

    // Helper method to convert Course entity to CourseResponse DTO
    private CourseResponse toCourseResponse(Course course) {
        Integer durationInt = null;
        if (course.getDuration() != null) {
            try {
                durationInt = Integer.parseInt(course.getDuration());
            } catch (NumberFormatException e) {
                // Duration is not a number, skip
            }
        }

        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .duration(durationInt)
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
