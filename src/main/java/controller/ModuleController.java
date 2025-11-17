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
import dto.ModuleRequest;
import entity.Module;
import service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for module management
 */
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Module Management", description = "APIs for managing course modules")
public class ModuleController {

    private final CourseService courseService;

    /**
     * Add module to course
     * POST /api/modules
     */
    @Operation(
            summary = "Add module to course",
            description = "Creates and adds a new module to a specific course with title, description, and order"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Module added successfully",
                    content = @Content(schema = @Schema(implementation = Module.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping
    public ResponseEntity<Module> addModuleToCourse(
            @Parameter(description = "ID of the course") @RequestParam Long courseId,
            @Valid @RequestBody ModuleRequest request) {
        log.info("REST request to add module to course {}", courseId);

        Module module = Module.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderNumber())
                .build();

        Module addedModule = courseService.addModuleToCourse(courseId, module);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedModule);
    }
}
