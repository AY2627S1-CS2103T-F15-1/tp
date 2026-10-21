package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.view.DetailView;
import seedu.address.model.Model;
import seedu.address.model.homework.HomeworkQueries;
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
            throw new CommandException(Messages.invalidPersonIndex(targetIndex, lastShownList.size()));
        }

        Person student = lastShownList.get(targetIndex.getZeroBased());
        List<Lesson> lessons = model.getAddressBook().getLessonList().stream()
                .filter(lesson -> lesson.isWith(student))
                .sorted(Lesson.CHRONOLOGICAL)
                .collect(Collectors.toList());

        List<String> recentNotes = recentNotes(lessons);
        String history = String.format(MESSAGE_HISTORY, count(lessons, LessonStatus.COMPLETED),
                count(lessons, LessonStatus.CANCELLED), count(lessons, LessonStatus.MISSED));
        HomeworkQueries.Summary homework = HomeworkQueries.summaryOf(model.getAddressBook(), student.getId(), false,
                today);

        String text = formatStudent(student)
                + "\n\n" + block(HEADING_NEXT_LESSON, nextLesson(lessons))
                + "\n\n" + block(HEADING_RECENT_NOTES, recentNotes)
                + "\n\n" + HEADING_LESSON_HISTORY + "\n" + history
                + "\n\n" + homework.toText();

        String title = String.format(MESSAGE_VIEWING, student.getName());
        List<DetailView.Section> sections = List.of(
                new DetailView.Section("", plain(studentLines(student))),
                new DetailView.Section(HEADING_NEXT_LESSON, plain(nextLesson(lessons))),
                new DetailView.Section(HEADING_RECENT_NOTES, plain(recentNotes)),
                new DetailView.Section(HEADING_LESSON_HISTORY, plain(List.of(history.strip()))),
                new DetailView.Section(homework.heading(), plain(homework.lines())));
        return new CommandResult(text).withDetailView(new DetailView(title, title, false, sections));
    }

    private static List<DetailView.Line> plain(List<String> lines) {
        return lines.stream().map(DetailView.Line::plain).toList();
    }

    /** Returns {@code heading} with each of {@code lines} indented below it. */
    private static String block(String heading, List<String> lines) {
        StringBuilder text = new StringBuilder(heading);
        lines.forEach(line -> text.append("\n  ").append(line));
        return text.toString();
    }

    private static List<String> studentLines(Person student) {
        String subjects = student.getSubjects().stream()
                .map(Subject::toString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
        StringBuilder contact = new StringBuilder("Phone: " + student.getPhone());
        student.getEmail().ifPresent(email -> contact.append(SEPARATOR).append("Email: ").append(email));
        student.getVenue().ifPresent(venue -> contact.append(SEPARATOR).append("Venue: ").append(venue));

        List<String> lines = new ArrayList<>();
        lines.add("Level " + student.getLevel() + SEPARATOR + subjects + SEPARATOR
                + student.getRate().toDisplayString() + "/hour");
        lines.add(contact.toString());
        if (!student.getRemark().value.isEmpty()) {
            lines.add("Remark: " + student.getRemark());
        }
        return lines;
    }

    private static String formatStudent(Person student) {
        return String.format(MESSAGE_VIEWING, student.getName()) + "\n" + String.join("\n", studentLines(student));
    }

    /** Returns the lines about the next lesson: when and where it is, and how to prepare, or that none is booked. */
    private List<String> nextLesson(List<Lesson> lessons) {
        // The lessons are in chronological order, so the first upcoming one is the next lesson
        Optional<Lesson> nextLesson = lessons.stream().filter(lesson -> lesson.isUpcoming(today)).findFirst();
        if (nextLesson.isEmpty()) {
            return List.of(MESSAGE_NO_UPCOMING_LESSON.strip());
        }
        Lesson lesson = nextLesson.get();
        List<String> lines = new ArrayList<>();
        lines.add(String.format("%s, %s%s%s%s%s", lesson.getDate(), lesson.getTimeRange(), SEPARATOR,
                lesson.getSubject(), SEPARATOR, Messages.formatVenue(lesson)));
        lesson.getPreparation().ifPresent(notes -> lines.add("Preparation: " + notes));
        return lines;
    }

    /** Returns the notes of the most recent completed lessons, newest first, or that there are none. */
    private static List<String> recentNotes(List<Lesson> lessons) {
        // The lessons are in chronological order, so the most recent ones are at the end
        List<Lesson> withNotes = lessons.stream()
                .filter(lesson -> lesson.getStatus() == LessonStatus.COMPLETED && lesson.getNotes().isPresent())
                .collect(Collectors.toList());
        List<Lesson> recent = withNotes.subList(Math.max(0, withNotes.size() - MAX_RECENT_NOTES), withNotes.size());
        if (recent.isEmpty()) {
            return List.of(MESSAGE_NO_NOTES.strip());
        }
        return recent.reversed().stream()
                .map(lesson -> lesson.getDate() + "  " + lesson.getNotes().get())
                .toList();
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
    public int hashCode() {
        return Objects.hash(targetIndex, today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("today", today)
                .toString();
    }
}
