package entity;

/**
 * Enum representing user roles in the learning platform.
 */
public enum UserRole {
    /**
     * Student role - can enroll in courses, submit assignments, take quizzes
     */
    STUDENT,

    /**
     * Teacher role - can create courses, grade assignments, manage content
     */
    TEACHER,

    /**
     * Administrator role - has full access to manage the platform
     */
    ADMIN
}
