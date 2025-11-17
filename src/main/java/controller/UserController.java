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
import dto.ErrorResponse;
import dto.ProfileRequest;
import dto.UserRequest;
import dto.UserResponse;
import entity.Profile;
import entity.User;
import entity.UserRole;
import service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API endpoints for user management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users, profiles, and user-related operations")
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     * POST /api/users
     */
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user account with the specified role (STUDENT, TEACHER, or ADMIN)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User with this email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        log.info("REST request to create user: {}", request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(createdUser));
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("REST request to get user by id: {}", id);

        User user = userService.getUserById(id);
        return ResponseEntity.ok(toUserResponse(user));
    }

    /**
     * Get user with profile
     * GET /api/users/{id}/with-profile
     */
    @GetMapping("/{id}/with-profile")
    public ResponseEntity<User> getUserWithProfile(@PathVariable Long id) {
        log.info("REST request to get user with profile by id: {}", id);

        User user = userService.getUserWithProfile(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("REST request to get all users");

        List<User> users = userService.getAllUsers();
        List<UserResponse> response = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get users by role
     * GET /api/users/by-role?role=STUDENT
     */
    @GetMapping("/by-role")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam UserRole role) {
        log.info("REST request to get users by role: {}", role);

        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> response = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all students
     * GET /api/users/students
     */
    @GetMapping("/students")
    public ResponseEntity<List<UserResponse>> getAllStudents() {
        log.info("REST request to get all students");

        List<User> students = userService.getAllStudents();
        List<UserResponse> response = students.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all teachers
     * GET /api/users/teachers
     */
    @GetMapping("/teachers")
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        log.info("REST request to get all teachers");

        List<User> teachers = userService.getAllTeachers();
        List<UserResponse> response = teachers.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Search users by name
     * GET /api/users/search?name=John
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name) {
        log.info("REST request to search users by name: {}", name);

        List<User> users = userService.searchUsersByName(name);
        List<UserResponse> response = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @Valid @RequestBody UserRequest request) {
        log.info("REST request to update user with id: {}", id);

        User userDetails = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(toUserResponse(updatedUser));
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user with id: {}", id);

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create or update profile for user
     * POST /api/users/{userId}/profile
     */
    @PostMapping("/{userId}/profile")
    public ResponseEntity<Profile> createOrUpdateProfile(@PathVariable Long userId,
                                                          @Valid @RequestBody ProfileRequest request) {
        log.info("REST request to create/update profile for user id: {}", userId);

        Profile profileDetails = Profile.builder()
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .city(request.getCity())
                .country(request.getCountry())
                .websiteUrl(request.getWebsiteUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .githubUrl(request.getGithubUrl())
                .build();

        Profile profile = userService.createOrUpdateProfile(userId, profileDetails);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get profile by user ID
     * GET /api/users/{userId}/profile
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<Profile> getProfileByUserId(@PathVariable Long userId) {
        log.info("REST request to get profile for user id: {}", userId);

        Profile profile = userService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Count users by role
     * GET /api/users/count?role=STUDENT
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUsersByRole(@RequestParam UserRole role) {
        log.info("REST request to count users by role: {}", role);

        long count = userService.countUsersByRole(role);
        return ResponseEntity.ok(count);
    }

    // Helper method to convert User entity to UserResponse DTO
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
