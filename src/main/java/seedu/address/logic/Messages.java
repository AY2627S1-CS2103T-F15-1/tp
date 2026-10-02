package seedu.address.logic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX =
            "The lesson index provided is invalid. Use 'agenda' or 'lesson list' to see the lesson numbers.";
    public static final String MESSAGE_NO_VENUE = "\u2014";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d student(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone());
        person.getEmail().ifPresent(email -> builder.append("; Email: ").append(email));
        builder.append("; Level: ")
                .append(person.getLevel())
                .append("; Subjects: ")
                .append(person.getSubjects().stream()
                        .map(Subject::toString)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")))
                .append("; Rate: ")
                .append(person.getRate().toDisplayString());
        person.getVenue().ifPresent(venue -> builder.append("; Venue: ").append(venue));
        builder.append("; Remark: ")
                .append(person.getRemark());
        return builder.toString();
    }

    /**
     * Formats who, what and when of {@code lesson} for display, e.g.
     * "Tan Wei Ming \u2014 Math, Mon 22 Sep 2026, 16:30-18:00".
     */
    public static String format(Lesson lesson) {
        return String.format("%s \u2014 %s, %s, %s", lesson.getStudentName(), lesson.getSubject(), lesson.getDate(),
                lesson.getTimeRange());
    }

    /**
     * Formats when and where {@code lesson} takes place, e.g. "Mon 22 Sep 2026, 16:30-18:00, Blk 512".
     */
    public static String formatSlot(Lesson lesson) {
        return String.format("%s, %s, %s", lesson.getDate(), lesson.getTimeRange(), formatVenue(lesson));
    }

    /**
     * Returns the venue of {@code lesson}, or a dash if the lesson has no venue.
     */
    public static String formatVenue(Lesson lesson) {
        return lesson.getVenue().map(venue -> venue.toString()).orElse(MESSAGE_NO_VENUE);
    }

    /**
     * Formats the lessons that overlap with another lesson, separated by semicolons.
     */
    public static String formatOverlaps(List<Lesson> overlappingLessons) {
        return overlappingLessons.stream().map(Messages::format).collect(Collectors.joining("; ")) + ".";
    }

}
