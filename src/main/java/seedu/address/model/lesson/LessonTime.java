package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a lesson time in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidTime(String)}
 *
 * <p>Accepts times in {@code HH:MM} 24-hour format (must-have), and additionally
 * {@code HHMM} and {@code h:mma} 12-hour format (nice-to-have).
 */
public class LessonTime {

    public static final String MESSAGE_CONSTRAINTS =
            "Times must be in 24-hour HH:MM format, e.g. 16:30.";

    private static final DateTimeFormatter COLON_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HHMM_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter TWELVE_HOUR_FORMAT = DateTimeFormatter.ofPattern("h:mma");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public final LocalTime value;

    /**
     * Constructs a {@code LessonTime}.
     *
     * @param time A valid time string.
     */
    public LessonTime(String time) {
        requireNonNull(time);
        checkArgument(isValidTime(time), MESSAGE_CONSTRAINTS);
        value = parseTime(time);
    }

    /**
     * Returns true if a given string is a valid time.
     */
    public static boolean isValidTime(String test) {
        return parseTime(test) != null;
    }

    private static LocalTime parseTime(String text) {
        String trimmed = text.strip();
        // Try HH:MM format
        try {
            return LocalTime.parse(trimmed, COLON_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Fall through
        }
        // Try HHMM format (NTH)
        try {
            return LocalTime.parse(trimmed, HHMM_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Fall through
        }
        // Try 12-hour format like 4:30pm (NTH)
        try {
            return LocalTime.parse(trimmed.toUpperCase(), TWELVE_HOUR_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * Returns the time formatted for display in 24-hour HH:MM format.
     */
    public String toDisplayString() {
        return value.format(DISPLAY_FORMAT);
    }

    /**
     * Returns the time in HH:mm format for storage.
     */
    public String toStorageString() {
        return value.format(COLON_FORMAT);
    }

    @Override
    public String toString() {
        return toDisplayString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof LessonTime otherTime)) {
            return false;
        }

        return value.equals(otherTime.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
