package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents a lesson date in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidDate(String)}
 *
 * <p>Accepts dates in {@code YYYY-MM-DD} format (must-have) and {@code DD/MM/YYYY}
 * format (nice-to-have). Impossible dates like 2026-02-30 are rejected.
 */
public class LessonDate {

    public static final String MESSAGE_CONSTRAINTS =
            "Dates must be in YYYY-MM-DD format, e.g. 2026-09-22.";

    private static final DateTimeFormatter ISO_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE dd MMM uuuu");
    private static final DateTimeFormatter SLASH_FORMAT =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public final LocalDate value;

    /**
     * Constructs a {@code LessonDate}.
     *
     * @param date A valid date string.
     */
    public LessonDate(String date) {
        requireNonNull(date);
        checkArgument(isValidDate(date), MESSAGE_CONSTRAINTS);
        value = parseDate(date);
    }

    /**
     * Returns true if a given string is a valid date.
     */
    public static boolean isValidDate(String test) {
        return parseDate(test) != null;
    }

    private static LocalDate parseDate(String text) {
        String trimmed = text.strip();
        // Try ISO format first (YYYY-MM-DD)
        try {
            return LocalDate.parse(trimmed, ISO_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Fall through to try other formats
        }
        // Try DD/MM/YYYY format (NTH)
        try {
            return LocalDate.parse(trimmed, SLASH_FORMAT);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /**
     * Returns the date formatted for display, e.g. "Mon 22 Sep 2026".
     */
    public String toDisplayString() {
        return value.format(DISPLAY_FORMAT);
    }

    /**
     * Returns the date in ISO format for storage.
     */
    public String toStorageString() {
        return value.format(ISO_FORMAT);
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
