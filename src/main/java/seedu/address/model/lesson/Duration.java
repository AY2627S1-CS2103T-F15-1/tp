package seedu.address.model.lesson;

import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a lesson duration in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidDuration(int)}
 *
 * <p>Duration is measured in minutes, must be a multiple of 15, and must be
 * between 15 and 480 minutes inclusive (i.e. 15 minutes to 8 hours).
 */
public class Duration {

    public static final String MESSAGE_CONSTRAINTS =
            "Duration must be a multiple of 15 minutes, between 15 and 480.";

    public static final int MIN_DURATION = 15;
    public static final int MAX_DURATION = 480;

    public final int value;

    /**
     * Constructs a {@code Duration}.
     *
     * @param minutes A valid duration in minutes.
     */
    public Duration(int minutes) {
        checkArgument(isValidDuration(minutes), MESSAGE_CONSTRAINTS);
        value = minutes;
    }

    /**
     * Constructs a {@code Duration} from a string.
     *
     * @param minutes A string representing a valid duration in minutes.
     */
    public Duration(String minutes) {
        try {
            int parsed = Integer.parseInt(minutes.strip());
            checkArgument(isValidDuration(parsed), MESSAGE_CONSTRAINTS);
            value = parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Returns true if a given integer is a valid lesson duration.
     */
    public static boolean isValidDuration(int test) {
        return test >= MIN_DURATION && test <= MAX_DURATION && test % MIN_DURATION == 0;
    }

    /**
     * Returns true if a given string represents a valid lesson duration.
     */
    public static boolean isValidDuration(String test) {
        try {
            return isValidDuration(Integer.parseInt(test.strip()));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the duration as a human-readable string, e.g. "1h 30m".
     */
    public String toDisplayString() {
        int hours = value / 60;
        int mins = value % 60;
        if (hours == 0) {
            return mins + "m";
        }
        if (mins == 0) {
            return hours + "h";
        }
        return hours + "h " + mins + "m";
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Duration otherDuration)) {
            return false;
        }

        return value == otherDuration.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
