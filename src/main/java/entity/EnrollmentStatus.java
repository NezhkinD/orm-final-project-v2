package entity;

/**
 * Enum representing enrollment status.
 */
public enum EnrollmentStatus {
    /**
     * Student is currently enrolled and taking the course
     */
    ACTIVE,

    /**
     * Student has completed the course
     */
    COMPLETED,

    /**
     * Student has dropped/withdrawn from the course
     */
    DROPPED,

    /**
     * Student is suspended from the course
     */
    SUSPENDED
}
