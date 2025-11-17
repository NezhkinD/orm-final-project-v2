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
import dto.LessonRequest;
import entity.Lesson;
import service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for lesson management
 */
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lesson Management", description = "APIs for managing lessons within modules")
public class LessonController {

    private final CourseService courseService;

    /**
     * Add lesson to module
     * POST /api/lessons
     */
    @Operation(
            summary = "Add lesson to module",
            description = "Creates and adds a new lesson to a specific module with title, content, duration, and optional video URL"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lesson added successfully",
                    content = @Content(schema = @Schema(implementation = Lesson.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Module not found")
    })
    @PostMapping
    public ResponseEntity<Lesson> addLessonToModule(
            @Parameter(description = "ID of the module") @RequestParam Long moduleId,
            @Valid @RequestBody LessonRequest request) {
        log.info("REST request to add lesson to module {}", moduleId);

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .orderIndex(request.getOrderNumber())
                .durationMinutes(request.getDuration())
                .videoUrl(request.getVideoUrl())
                .build();

        Lesson addedLesson = courseService.addLessonToModule(moduleId, lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedLesson);
    }
}
