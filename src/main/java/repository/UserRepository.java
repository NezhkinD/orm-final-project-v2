package repository;

import entity.User;
import entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (unique field)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all students
     */
    default List<User> findAllStudents() {
        return findByRole(UserRole.STUDENT);
    }

    /**
     * Find all teachers
     */
    default List<User> findAllTeachers() {
        return findByRole(UserRole.TEACHER);
    }

    /**
     * Find users by name containing (case-insensitive search)
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Find user with profile loaded (avoiding lazy loading exception)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    /**
     * Find user with all enrollments loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.enrollments WHERE u.id = :id")
    Optional<User> findByIdWithEnrollments(@Param("id") Long id);

    /**
     * Find user with enrollments and their courses loaded
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.enrollments e " +
           "LEFT JOIN FETCH e.course " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithEnrollmentsAndCourses(@Param("id") Long id);

    /**
     * Find user with all submissions loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.submissions WHERE u.id = :id")
    Optional<User> findByIdWithSubmissions(@Param("id") Long id);

    /**
     * Find teachers who teach at least one course
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.coursesTaught WHERE u.role = 'TEACHER'")
    List<User> findTeachersWithCourses();

    /**
     * Count users by role
     */
    long countByRole(UserRole role);
}
