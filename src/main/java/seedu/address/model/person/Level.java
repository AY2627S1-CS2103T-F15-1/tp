package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a student's academic level in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidLevel(String)}
 *
 * <p>Levels correspond to the Singapore education system: Primary 1-6 (P1-P6),
 * Secondary 1-5 (S1-S5), and Junior College 1-2 (J1-J2). Input is case-insensitive
 * but stored in uppercase.
 */
public class Level {

    public static final String MESSAGE_CONSTRAINTS =
            "Level must be one of P1-P6, S1-S5, J1-J2 (e.g. S3).";

    /**
     * Matches P1-P6, S1-S5, J1-J2 in any case.
     */
    public static final String VALIDATION_REGEX = "(?i)[pP][1-6]|[sS][1-5]|[jJ][1-2]";

    public final String value;

    /**
     * Constructs a {@code Level}.
     *
     * @param level A valid academic level string.
     */
    public Level(String level) {
        requireNonNull(level);
        checkArgument(isValidLevel(level), MESSAGE_CONSTRAINTS);
        value = level.toUpperCase();
    }

    /**
     * Returns true if a given string is a valid academic level.
     */
    public static boolean isValidLevel(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Level otherLevel)) {
            return false;
        }

        return value.equals(otherLevel.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
