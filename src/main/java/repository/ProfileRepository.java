package repository;

import entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Profile entity.
 * Provides CRUD operations and custom queries for user profiles.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Find profile by user ID
     */
    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId")
    Optional<Profile> findByUserId(@Param("userId") Long userId);

    /**
     * Check if profile exists for a user
     */
    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    /**
     * Find profile with user loaded
     */
    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Profile> findByIdWithUser(@Param("id") Long id);
}
