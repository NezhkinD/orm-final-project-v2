package service;

import entity.Profile;
import entity.User;
import entity.UserRole;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.ProfileRepository;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .phoneNumber("+1234567890")
                .build();

        testProfile = Profile.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .city("Test City")
                .country("Test Country")
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when creating user with duplicate email")
    void shouldThrowExceptionWhenCreatingUserWithDuplicateEmail() {
        // Given
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository).existsByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmailSuccessfully() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should get users by role")
    void shouldGetUsersByRole() {
        // Given
        List<User> students = Arrays.asList(testUser);
        when(userRepository.findByRole(UserRole.STUDENT)).thenReturn(students);

        // When
        List<User> result = userService.getUsersByRole(UserRole.STUDENT);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(UserRole.STUDENT);
        verify(userRepository).findByRole(UserRole.STUDENT);
    }

    @Test
    @DisplayName("Should search users by name")
    void shouldSearchUsersByName() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByNameContainingIgnoreCase("Test")).thenReturn(users);

        // When
        List<User> result = userService.searchUsersByName("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Test");
        verify(userRepository).findByNameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        User updatedDetails = User.builder()
                .name("Updated Name")
                .email("test@example.com")
                .role(UserRole.TEACHER)
                .phoneNumber("+9876543210")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with duplicate email")
    void shouldThrowExceptionWhenUpdatingUserWithDuplicateEmail() {
        // Given
        User updatedDetails = User.builder()
                .email("another@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("another@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updatedDetails))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should create new profile for user")
    void shouldCreateNewProfileForUser() {
        // Given
        testUser.setProfile(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = userService.createOrUpdateProfile(1L, testProfile);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should update existing profile")
    void shouldUpdateExistingProfile() {
        // Given
        testUser.setProfile(testProfile);
        Profile updatedDetails = Profile.builder()
                .bio("Updated bio")
                .city("New City")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = userService.createOrUpdateProfile(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    @DisplayName("Should count users by role")
    void shouldCountUsersByRole() {
        // Given
        when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(5L);

        // When
        long result = userService.countUsersByRole(UserRole.STUDENT);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(userRepository).countByRole(UserRole.STUDENT);
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }
}
