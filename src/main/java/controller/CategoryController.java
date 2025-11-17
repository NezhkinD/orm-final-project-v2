package controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.dto.CategoryRequest;
import org.example.learningplatform.entity.Category;
import org.example.learningplatform.exception.DuplicateResourceException;
import org.example.learningplatform.exception.ResourceNotFoundException;
import org.example.learningplatform.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API endpoints for category management
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing course categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    /**
     * Create a new category
     * POST /api/categories
     */
    @Operation(
            summary = "Create category",
            description = "Creates a new course category with name and description. Category names must be unique."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = Category.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Category with this name already exists")
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("REST request to create category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        log.info("REST request to get category by id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return ResponseEntity.ok(category);
    }

    /**
     * Get category with courses
     * GET /api/categories/{id}/with-courses
     */
    @GetMapping("/{id}/with-courses")
    public ResponseEntity<Category> getCategoryWithCourses(@PathVariable Long id) {
        log.info("REST request to get category with courses by id: {}", id);

        Category category = categoryRepository.findByIdWithCourses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return ResponseEntity.ok(category);
    }

    /**
     * Get all categories
     * GET /api/categories
     */
    @Operation(
            summary = "Get all categories",
            description = "Retrieves a list of all available course categories"
    )
    @ApiResponse(responseCode = "200", description = "List of all categories")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        log.info("REST request to get all categories");

        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by name
     * GET /api/categories/by-name?name=Programming
     */
    @GetMapping("/by-name")
    public ResponseEntity<Category> getCategoryByName(@RequestParam String name) {
        log.info("REST request to get category by name: {}", name);

        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
        return ResponseEntity.ok(category);
    }

    /**
     * Update category
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                    @Valid @RequestBody CategoryRequest request) {
        log.info("REST request to update category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Delete category
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("REST request to delete category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count courses in category
     * GET /api/categories/{categoryId}/courses-count
     */
    @GetMapping("/{categoryId}/courses-count")
    public ResponseEntity<Long> countCoursesByCategory(@PathVariable Long categoryId) {
        log.info("REST request to count courses in category {}", categoryId);

        long count = categoryRepository.countCoursesByCategory(categoryId);
        return ResponseEntity.ok(count);
    }
}
