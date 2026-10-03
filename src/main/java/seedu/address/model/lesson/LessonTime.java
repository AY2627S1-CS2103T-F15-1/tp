package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the start time of a lesson in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidTime(String)}
 *
 * <p>Accepts times in 24-hour {@code HH:MM} or {@code HHMM} format, and in 12-hour format such as
 * {@code 4:30pm}. Times are always shown and saved in 24-hour {@code HH:MM} format.
 */
public class LessonTime {

    public static final String MESSAGE_CONSTRAINTS =
            "Times must be in 24-hour HH:MM format, e.g. 16:30.";

    private static final DateTimeFormatter STORAGE_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<DateTimeFormatter> INPUT_FORMATS = List.of(
            DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("HHmm").withResolverStyle(ResolverStyle.STRICT),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mma")
                    .toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT));

    public final LocalTime value;

    /**
     * Constructs a {@code LessonTime}.
     *
     * @param time A valid time string.
     */
    public LessonTime(String time) {
        requireNonNull(time);
        value = parseTime(time).orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    /**
     * Returns true if a given string is a valid time.
     */
    public static boolean isValidTime(String test) {
        requireNonNull(test);
        return parseTime(test).isPresent();
    }

    private static Optional<LocalTime> parseTime(String text) {
        String trimmed = text.strip();
        for (DateTimeFormatter format : INPUT_FORMATS) {
            try {
                return Optional.of(LocalTime.parse(trimmed, format));
            } catch (DateTimeParseException e) {
                // Not in this format, so try the next one
                continue;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the time in 24-hour HH:MM format, e.g. "16:30".
     */
    @Override
    public String toString() {
        return value.format(STORAGE_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
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
