package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the date of a lesson in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidDate(String)}
 *
 * <p>Accepts dates in {@code YYYY-MM-DD} and {@code DD/MM/YYYY} format. Impossible dates such as
 * 2026-02-30 are rejected rather than moved to a nearby valid date.
 */
public class LessonDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Dates must be in YYYY-MM-DD or DD/MM/YYYY format, e.g. 2026-09-22.";

    private static final DateTimeFormatter STORAGE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final List<DateTimeFormatter> INPUT_FORMATS = List.of(
            STORAGE_FORMAT,
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT));
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
        value = parseDate(date).orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    /**
     * Returns true if a given string is a valid date.
     */
    public static boolean isValidDate(String test) {
        requireNonNull(test);
        return parseDate(test).isPresent();
    }

    private static Optional<LocalDate> parseDate(String text) {
        String trimmed = text.strip();
        for (DateTimeFormatter format : INPUT_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(trimmed, format));
            } catch (DateTimeParseException e) {
                // Not in this format, so try the next one
                continue;
            }
        }
        return Optional.empty();
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
