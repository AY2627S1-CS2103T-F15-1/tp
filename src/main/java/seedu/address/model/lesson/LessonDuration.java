package seedu.address.model.lesson;

import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents how long a lesson lasts in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidDuration(int)}
 *
 * <p>A duration is a whole number of minutes that is a multiple of 15, from 15 minutes to 8 hours,
 * because lessons are booked in quarter hours.
 */
public class LessonDuration {

    public static final String MESSAGE_CONSTRAINTS =
            "Duration must be a multiple of 15 minutes, between 15 and 480.";

    public static final int MIN_DURATION = 15;
    public static final int MAX_DURATION = 480;

    public final int value;

    /**
     * Constructs a {@code LessonDuration}.
     *
     * @param minutes A valid duration in minutes.
     */
    public LessonDuration(int minutes) {
        checkArgument(isValidDuration(minutes), MESSAGE_CONSTRAINTS);
        value = minutes;
    }

    /**
     * Returns true if a given number of minutes is a valid lesson duration.
     */
    public static boolean isValidDuration(int test) {
        return test >= MIN_DURATION && test <= MAX_DURATION && test % MIN_DURATION == 0;
    }

    /**
     * Returns the duration in hours and minutes, e.g. "1h 30m".
     */
    @Override
    public String toString() {
        int hours = value / 60;
        int minutes = value % 60;
        if (hours == 0) {
            return minutes + "m";
        }
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonDuration otherDuration)) {
            return false;
        }

        return value == otherDuration.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
