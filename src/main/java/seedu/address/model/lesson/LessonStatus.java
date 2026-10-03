package seedu.address.model.lesson;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents the status of a lesson in TutorFlow.
 *
 * <p>A lesson starts as {@code SCHEDULED} and ends as one of {@code COMPLETED}, {@code CANCELLED}
 * or {@code MISSED}. This class does not restrict which status can follow which, so that the
 * commands that change the status decide which changes are allowed.
 */
public enum LessonStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    MISSED;

    public static final String MESSAGE_CONSTRAINTS =
            "Status must be one of: scheduled, completed, cancelled, missed.";

    /**
     * Returns true if a given string is a valid lesson status, ignoring case.
     */
    public static boolean isValidStatus(String test) {
        return Arrays.stream(values()).anyMatch(status -> status.name().equalsIgnoreCase(test));
    }

    /**
     * Returns the status that matches {@code status}, ignoring case.
     *
     * @throws IllegalArgumentException if {@code status} is not a valid status.
     */
    public static LessonStatus fromString(String status) {
        return Arrays.stream(values())
                .filter(value -> value.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
