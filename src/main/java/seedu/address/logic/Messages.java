package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Student;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX = "The student index provided is invalid.";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX = "The lesson index provided is invalid.";
    public static final String MESSAGE_STUDENTS_LISTED_OVERVIEW = "%1$d student(s) listed!";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = MESSAGE_STUDENTS_LISTED_OVERVIEW;
    public static final String MESSAGE_DUPLICATE_STUDENT = "This student already exists in TutorFlow.";
    public static final String MESSAGE_DUPLICATE_PERSON = MESSAGE_DUPLICATE_STUDENT;
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

        if (person.getEmail() != null) {
            builder.append("; Email: ").append(person.getEmail());
        }

        if (person instanceof Student) {
            Student student = (Student) person;
            builder.append("; Level: ")
                    .append(student.getLevel())
                    .append("; Subjects: ");
            student.getSubjects().forEach(builder::append);
            builder.append("; Rate: ")
                    .append(student.getRate());

            if (student.getVenue() != null) {
                builder.append("; Venue: ").append(student.getVenue());
            }
        }
        return builder.toString();
    }

    /**
     * Formats the {@code lesson} for display to the user.
     */
    public static String formatLesson(Lesson lesson) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Student: ")
                .append(lesson.getStudentName())
                .append("; Subject: ")
                .append(lesson.getSubject())
                .append("; Date: ")
                .append(lesson.getDate())
                .append("; Time: ")
                .append(lesson.getTime())
                .append("; Duration: ")
                .append(lesson.getDuration());

        if (lesson.getVenue() != null) {
            builder.append("; Venue: ").append(lesson.getVenue());
        }
        return builder.toString();
    }
}
