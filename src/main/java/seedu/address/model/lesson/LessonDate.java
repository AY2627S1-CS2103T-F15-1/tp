package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Represents the date of a lesson in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidDate(String)}
 *
 * <p>Accepts the day-month-year and year-month-day orders described in {@link FlexibleDateParser}, such as
 * {@code 2026-12-22}, {@code 22/12/2026}, {@code 22-12-26} and {@code 22 Dec 2026}. Impossible dates such as
 * 2026-02-30 are rejected rather than moved to a nearby valid date.
 */
public class LessonDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Dates must be a real day written as year-month-day or day-month-year, "
                    + "e.g. 2026-09-22, 22/09/2026 or 22 Sep 2026.";

    private static final DateTimeFormatter STORAGE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE dd MMM uuuu", Locale.ENGLISH);

    public final LocalDate value;

    /**
     * Constructs a {@code LessonDate}.
     *
     * @param date A valid date string.
     */
    public LessonDate(String date) {
        requireNonNull(date);
        value = FlexibleDateParser.parse(date)
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    /**
     * Returns true if a given string is a valid date.
     */
    public static boolean isValidDate(String test) {
        requireNonNull(test);
        return FlexibleDateParser.parse(test).isPresent();
    }

    /**
     * Returns the date in the format used to save it, e.g. "2026-09-22".
     */
    public String toStorageString() {
        return value.format(STORAGE_FORMAT);
    }

    /**
     * Returns the date in the format shown to the user, e.g. "Tue 22 Sep 2026".
     */
    @Override
    public String toString() {
        return value.format(DISPLAY_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonDate otherDate)) {
            return false;
        }

        return value.equals(otherDate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
