package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.entity.Assignment;
import org.example.learningplatform.entity.Lesson;
import org.example.learningplatform.entity.Submission;
import org.example.learningplatform.entity.User;
import org.example.learningplatform.exception.DuplicateResourceException;
import org.example.learningplatform.exception.ResourceNotFoundException;
import org.example.learningplatform.repository.AssignmentRepository;
import org.example.learningplatform.repository.LessonRepository;
import org.example.learningplatform.repository.SubmissionRepository;
import org.example.learningplatform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing assignments and student submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    /**
     * Create assignment for a lesson
     */
    @Transactional
    public Assignment createAssignment(Long lessonId, Assignment assignment) {
        log.info("Creating assignment for lesson {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        assignment.setLesson(lesson);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        log.info("Assignment created with id: {}", savedAssignment.getId());
        return savedAssignment;
    }

    /**
     * Submit assignment solution by student
     */
    @Transactional
    public Submission submitAssignment(Long assignmentId, Long studentId, String content) {
        log.info("Student {} submitting assignment {}", studentId, assignmentId);

        // Check if already submitted
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new DuplicateResourceException(
                    "Submission already exists for assignment " + assignmentId + " by student " + studentId);
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content)
                .build();

        Submission savedSubmission = submissionRepository.save(submission);
        log.info("Submission created with id: {}", savedSubmission.getId());
        return savedSubmission;
    }

    /**
     * Grade submission
     */
    @Transactional
    public Submission gradeSubmission(Long submissionId, Integer score, String feedback) {
        log.info("Grading submission {}", submissionId);

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));

        submission.setScore(score);
        submission.setFeedback(feedback);

        Submission gradedSubmission = submissionRepository.save(submission);
        log.info("Submission {} graded with score: {}", submissionId, score);
        return gradedSubmission;
    }

    /**
     * Get assignment by ID
     */
    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
    }

    /**
     * Get assignments by lesson
     */
    public List<Assignment> getAssignmentsByLesson(Long lessonId) {
        return assignmentRepository.findByLessonId(lessonId);
    }

    /**
     * Get submission by ID
     */
    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", id));
    }

    /**
     * Get submissions by assignment
     */
    public List<Submission> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    /**
     * Get submissions by student
     */
    public List<Submission> getSubmissionsByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    /**
     * Get ungraded submissions
     */
    public List<Submission> getUngradedSubmissions() {
        return submissionRepository.findUngradedSubmissions();
    }

    /**
     * Get student's average score
     */
    public Double getStudentAverageScore(Long studentId) {
        return submissionRepository.getAverageScoreByStudent(studentId);
    }
}
