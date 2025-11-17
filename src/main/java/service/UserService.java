package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import entity.Profile;
import entity.User;
import entity.UserRole;
import exception.DuplicateResourceException;
import exception.ResourceNotFoundException;
import repository.ProfileRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing users and profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /**
     * Create a new user
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating user with email: {}", user.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("User", "email", user.getEmail());
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get user with profile loaded (avoiding lazy loading exception)
     */
    public User getUserWithProfile(Long id) {
        return userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Get all users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Get all students
     */
    public List<User> getAllStudents() {
        return userRepository.findAllStudents();
    }

    /**
     * Get all teachers
     */
    public List<User> getAllTeachers() {
        return userRepository.findAllTeachers();
    }

    /**
     * Search users by name
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Update user
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with id: {}", id);

        User user = getUserById(id);

        // Check if email changed and is unique
        if (!user.getEmail().equals(userDetails.getEmail()) &&
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDetails.getEmail());
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setPhoneNumber(userDetails.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());
        return updatedUser;
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = getUserById(id);
        userRepository.delete(user);

        log.info("User deleted successfully with id: {}", id);
    }

    /**
     * Create or update profile for user
     */
    @Transactional
    public Profile createOrUpdateProfile(Long userId, Profile profileDetails) {
        log.info("Creating/updating profile for user id: {}", userId);

        User user = getUserById(userId);

        Profile profile = user.getProfile();
        if (profile == null) {
            // Create new profile
            profile = Profile.builder()
                    .user(user)
                    .bio(profileDetails.getBio())
                    .avatarUrl(profileDetails.getAvatarUrl())
                    .city(profileDetails.getCity())
                    .country(profileDetails.getCountry())
                    .websiteUrl(profileDetails.getWebsiteUrl())
                    .linkedinUrl(profileDetails.getLinkedinUrl())
                    .githubUrl(profileDetails.getGithubUrl())
                    .build();
            user.setProfile(profile);
        } else {
            // Update existing profile
            profile.setBio(profileDetails.getBio());
            profile.setAvatarUrl(profileDetails.getAvatarUrl());
            profile.setCity(profileDetails.getCity());
            profile.setCountry(profileDetails.getCountry());
            profile.setWebsiteUrl(profileDetails.getWebsiteUrl());
            profile.setLinkedinUrl(profileDetails.getLinkedinUrl());
            profile.setGithubUrl(profileDetails.getGithubUrl());
        }

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile saved successfully for user id: {}", userId);
        return savedProfile;
    }

    /**
     * Get profile by user ID
     */
    public Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }

    /**
     * Count users by role
     */
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
