package repository;

import entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CourseReview entity.
 */
@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    /**
     * Find all reviews by course ID
     */
    List<CourseReview> findByCourseId(Long courseId);

    /**
     * Find all reviews by student ID
     */
    List<CourseReview> findByStudentId(Long studentId);

    /**
     * Find review by course and student (should be unique)
     */
    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);

    /**
     * Check if review exists for course and student
     */
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    /**
     * Find reviews with rating equal to or greater than a value
     */
    @Query("SELECT r FROM CourseReview r WHERE r.rating >= :minRating")
    List<CourseReview> findByRatingGreaterThanEqual(@Param("minRating") Integer minRating);

    /**
     * Find reviews by exact rating
     */
    List<CourseReview> findByRating(Integer rating);

    /**
     * Find review with course loaded
     */
    @Query("SELECT r FROM CourseReview r JOIN FETCH r.course WHERE r.id = :id")
    Optional<CourseReview> findByIdWithCourse(@Param("id") Long id);

    /**
     * Find review with student loaded
     */
    @Query("SELECT r FROM CourseReview r JOIN FETCH r.student WHERE r.id = :id")
    Optional<CourseReview> findByIdWithStudent(@Param("id") Long id);

    /**
     * Find reviews with both course and student loaded
     */
    @Query("SELECT r FROM CourseReview r JOIN FETCH r.course JOIN FETCH r.student WHERE r.id = :id")
    Optional<CourseReview> findByIdWithCourseAndStudent(@Param("id") Long id);

    /**
     * Calculate average rating for a course
     */
    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") Long courseId);

    /**
     * Count reviews by course
     */
    long countByCourseId(Long courseId);

    /**
     * Count reviews by rating for a course
     */
    @Query("SELECT COUNT(r) FROM CourseReview r WHERE r.course.id = :courseId AND r.rating = :rating")
    long countByCourseIdAndRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);

    /**
     * Find latest reviews for a course (ordered by creation date desc)
     */
    @Query("SELECT r FROM CourseReview r WHERE r.course.id = :courseId ORDER BY r.createdAt DESC")
    List<CourseReview> findLatestReviewsByCourseId(@Param("courseId") Long courseId);

    /**
     * Find reviews by teacher (all reviews for courses taught by this teacher)
     */
    @Query("SELECT r FROM CourseReview r WHERE r.course.teacher.id = :teacherId")
    List<CourseReview> findReviewsByTeacherId(@Param("teacherId") Long teacherId);
}
