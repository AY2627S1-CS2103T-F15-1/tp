package seedu.address.model.lesson;

/**
 * Represents the status of a lesson in TutorFlow.
 *
 * <p>A lesson starts as {@code SCHEDULED} and transitions to one of the
 * terminal states: {@code COMPLETED}, {@code CANCELLED}, or {@code MISSED}.
 */
public enum LessonStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    MISSED;

    public static final String MESSAGE_CONSTRAINTS =
            "Status must be one of: scheduled, completed, cancelled, missed.";

    /**
     * Returns true if the given string matches a valid lesson status (case-insensitive).
     */
    public static boolean isValidStatus(String test) {
        for (LessonStatus status : values()) {
            if (status.name().equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
