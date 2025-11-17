package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.entity.*;
import org.example.learningplatform.exception.BusinessException;
import org.example.learningplatform.exception.DuplicateResourceException;
import org.example.learningplatform.exception.ResourceNotFoundException;
import org.example.learningplatform.repository.CourseRepository;
import org.example.learningplatform.repository.EnrollmentRepository;
import org.example.learningplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing course enrollments.
 * Handles student registration to courses and enrollment status management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /**
     * Enroll a student in a course
     */
    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        log.info("Enrolling student {} in course {}", studentId, courseId);

        // Validate student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessException("Only students can be enrolled in courses");
        }

        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("Enrollment already exists for student " +
                    studentId + " and course " + courseId);
        }

        // Create enrollment
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .progressPercentage(0)
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Student {} successfully enrolled in course {} with enrollment id: {}",
                studentId, courseId, savedEnrollment.getId());
        return savedEnrollment;
    }

    /**
     * Unenroll a student from a course
     */
    @Transactional
    public void unenrollStudent(Long studentId, Long courseId) {
        log.info("Unenrolling student {} from course {}", studentId, courseId);

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student " + studentId + " and course " + courseId));

        enrollment.drop();
        enrollmentRepository.save(enrollment);

        log.info("Student {} successfully unenrolled from course {}", studentId, courseId);
    }

    /**
     * Get enrollment by ID
     */
    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    /**
     * Get enrollment by student and course
     */
    public Enrollment getEnrollmentByStudentAndCourse(Long studentId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student " + studentId + " and course " + courseId));
    }

    /**
     * Get all enrollments for a student
     */
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    /**
     * Get active enrollments for a student
     */
    public List<Enrollment> getActiveEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
    }

    /**
     * Get completed enrollments for a student
     */
    public List<Enrollment> getCompletedEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findCompletedEnrollmentsByStudentId(studentId);
    }

    /**
     * Get all enrollments for a course
     */
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * Get active enrollments for a course
     */
    public List<Enrollment> getActiveEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findActiveEnrollmentsByCourseId(courseId);
    }

    /**
     * Update enrollment progress
     */
    @Transactional
    public Enrollment updateProgress(Long enrollmentId, Integer progressPercentage) {
        log.info("Updating progress for enrollment {}: {}%", enrollmentId, progressPercentage);

        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new BusinessException("Progress percentage must be between 0 and 100");
        }

        Enrollment enrollment = getEnrollmentById(enrollmentId);
        enrollment.setProgressPercentage(progressPercentage);

        // Auto-complete if progress reaches 100%
        if (progressPercentage == 100 && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            enrollment.complete();
            log.info("Enrollment {} automatically completed", enrollmentId);
        }

        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment progress updated successfully");
        return updatedEnrollment;
    }

    /**
     * Complete enrollment (mark as finished)
     */
    @Transactional
    public Enrollment completeEnrollment(Long enrollmentId, Double finalGrade) {
        log.info("Completing enrollment {}", enrollmentId);

        Enrollment enrollment = getEnrollmentById(enrollmentId);

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new BusinessException("Enrollment is already completed");
        }

        enrollment.complete();
        if (finalGrade != null) {
            enrollment.setFinalGrade(finalGrade);
        }

        Enrollment completedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment {} completed successfully with grade: {}", enrollmentId, finalGrade);
        return completedEnrollment;
    }

    /**
     * Check if student is enrolled in course
     */
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    /**
     * Count total enrollments for student
     */
    public long countEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.countByStudentId(studentId);
    }

    /**
     * Count active enrollments for student
     */
    public long countActiveEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.countActiveEnrollmentsByStudentId(studentId);
    }

    /**
     * Count enrollments for course
     */
    public long countEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }
}
