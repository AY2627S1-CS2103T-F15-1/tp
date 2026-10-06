package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Shows everything needed to prepare for a student on one screen: the student's details, their next lesson, the
 * notes of their most recent completed lessons and a count of their lessons by status.
 * It only reads the student and the lessons, so the displayed student list is left unchanged.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the overview of the student identified by the "
            + "index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_VIEWING = "Viewing: %1$s";
    public static final String HEADING_NEXT_LESSON = "NEXT LESSON";
    public static final String HEADING_RECENT_NOTES = "RECENT LESSON NOTES";
    public static final String HEADING_LESSON_HISTORY = "LESSON HISTORY";
    public static final String MESSAGE_NO_UPCOMING_LESSON = "  No upcoming lessons.";
    public static final String MESSAGE_NO_NOTES = "  No lesson notes yet.";
    public static final String MESSAGE_HISTORY = "  %1$d completed \u00b7 %2$d cancelled \u00b7 %3$d missed";

    /** The number of completed lessons whose notes are shown. */
    public static final int MAX_RECENT_NOTES = 3;

    private static final String SEPARATOR = " \u00b7 ";

    private final Index targetIndex;
    private final LocalDate today;

    /**
     * Creates a {@code ViewCommand} to show the student at {@code targetIndex}, treating {@code today} as the
     * current day when it looks for the next lesson.
     */
    public ViewCommand(Index targetIndex, LocalDate today) {
        requireNonNull(targetIndex);
        requireNonNull(today);
        this.targetIndex = targetIndex;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person student = lastShownList.get(targetIndex.getZeroBased());
        List<Lesson> lessons = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.isWith(student))
                .sorted(Lesson.CHRONOLOGICAL)
                .collect(Collectors.toList());

        return new CommandResult(formatStudent(student)
                + "\n\n" + formatNextLesson(lessons)
                + "\n\n" + formatRecentNotes(lessons)
                + "\n\n" + formatHistory(lessons));
    }

    private static String formatStudent(Person student) {
        String subjects = student.getSubjects().stream()
                .map(Subject::toString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
        StringBuilder contact = new StringBuilder("Phone: " + student.getPhone());
        student.getEmail().ifPresent(email -> contact.append(SEPARATOR).append("Email: ").append(email));
        student.getVenue().ifPresent(venue -> contact.append(SEPARATOR).append("Venue: ").append(venue));

        StringBuilder text = new StringBuilder(String.format(MESSAGE_VIEWING, student.getName()));
        text.append("\nLevel ").append(student.getLevel()).append(SEPARATOR).append(subjects)
                .append(SEPARATOR).append(student.getRate().toDisplayString()).append("/lesson");
        text.append("\n").append(contact);
        if (!student.getRemark().value.isEmpty()) {
            text.append("\nRemark: ").append(student.getRemark());
        }
        return text.toString();
    }

    private String formatNextLesson(List<Lesson> lessons) {
        // The lessons are in chronological order, so the first upcoming one is the next lesson
        Optional<Lesson> nextLesson = lessons.stream().filter(lesson -> lesson.isUpcoming(today)).findFirst();
        String body = nextLesson
                .map(lesson -> String.format("  %s, %s%s%s%s%s", lesson.getDate(), lesson.getTimeRange(), SEPARATOR,
                        lesson.getSubject(), SEPARATOR, Messages.formatVenue(lesson)))
                .orElse(MESSAGE_NO_UPCOMING_LESSON);
        return HEADING_NEXT_LESSON + "\n" + body;
    }

    private static String formatRecentNotes(List<Lesson> lessons) {
        // The lessons are in chronological order, so the most recent ones are at the end
        List<Lesson> withNotes = lessons.stream()
                .filter(lesson -> lesson.getStatus() == LessonStatus.COMPLETED && lesson.getNotes().isPresent())
                .collect(Collectors.toList());
        List<Lesson> recent = withNotes.subList(Math.max(0, withNotes.size() - MAX_RECENT_NOTES), withNotes.size());
        if (recent.isEmpty()) {
            return HEADING_RECENT_NOTES + "\n" + MESSAGE_NO_NOTES;
        }
        return HEADING_RECENT_NOTES + recent.reversed().stream()
                .map(lesson -> "\n  " + lesson.getDate() + "  " + lesson.getNotes().get())
                .collect(Collectors.joining());
    }

    private static String formatHistory(List<Lesson> lessons) {
        return HEADING_LESSON_HISTORY + "\n" + String.format(MESSAGE_HISTORY, count(lessons, LessonStatus.COMPLETED),
                count(lessons, LessonStatus.CANCELLED), count(lessons, LessonStatus.MISSED));
    }

    private static long count(List<Lesson> lessons, LessonStatus status) {
        return lessons.stream().filter(lesson -> lesson.getStatus() == status).count();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return targetIndex.equals(otherViewCommand.targetIndex) && today.equals(otherViewCommand.today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("today", today)
                .toString();
    }
}
