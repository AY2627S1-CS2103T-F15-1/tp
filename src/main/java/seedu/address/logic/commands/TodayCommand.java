package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.finance.Ledger;
import seedu.address.model.homework.HomeworkQueries;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentSummaryLabels;

/**
 * Shows what a day requires: the lessons that take place on it, the past lessons that are still waiting to be
 * marked completed, the overdue homework and the students who owe money. It only reads the data, so the displayed
 * lesson list is left unchanged.
 */
public class TodayCommand extends Command {

    public static final String COMMAND_WORD = "today";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows what today requires: today's lessons, "
            + "earlier lessons that are not yet marked completed, overdue homework and the students who owe money.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_TODAY_HEADER = "TODAY'S LESSONS (%1$d, %2$s)";
    public static final String MESSAGE_AWAITING_NOTES_HEADER = "LESSONS AWAITING NOTES (%1$d)";
    public static final String MESSAGE_DUES_HEADER = "DUES";
    public static final String MESSAGE_NO_LESSONS_TODAY = "  No lessons today.";
    public static final String MESSAGE_ALL_RECORDED = "  All past lessons are recorded.";
    public static final String MESSAGE_NOT_COMPLETED = "not yet marked completed";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter PAST_DAY_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
    private static final Logger logger = LogsCenter.getLogger(TodayCommand.class);
    private static final int MAX_DISPLAYED_LESSONS = 200;

    private final LocalDate today;

    /**
     * Creates a {@code TodayCommand} that treats {@code today} as the current day.
     */
    public TodayCommand(LocalDate today) {
        requireNonNull(today);
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Lesson> lessons = model.getAddressBook().getLessonList();

        // Cancelled lessons are left out because they will not take place
        List<Lesson> lessonsToday = lessons.stream()
                .filter(lesson -> lesson.getDate().value.equals(today))
                .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELLED)
                .sorted(Lesson.CHRONOLOGICAL)
                .collect(Collectors.toList());
        // The most recent lesson comes first, as it is the one most likely to still be remembered
        List<Lesson> lessonsAwaitingNotes = lessons.stream()
                .filter(lesson -> lesson.isScheduled() && lesson.getDate().value.isBefore(today))
                .sorted(Lesson.CHRONOLOGICAL.reversed())
                .collect(Collectors.toList());
        logger.fine("Showing " + lessonsToday.size() + " lessons on " + today + " and "
                + lessonsAwaitingNotes.size() + " lessons awaiting notes");

        // Same-name and archived students are told apart as in the homework and dues sections below
        StudentSummaryLabels labels = new StudentSummaryLabels(model.getAddressBook());
        Set<UUID> students = model.getAddressBook().getPersonList().stream().map(Person::getId)
                .collect(Collectors.toSet());
        Function<Lesson, String> studentLabel = lesson -> lesson.getStudentId().filter(students::contains)
                .map(labels::format).orElseGet(() -> lesson.getStudentName().toString());

        return new CommandResult(DATE_FORMAT.format(today)
                + "\n\n" + formatLessonsToday(model, lessonsToday, studentLabel)
                + LessonCommandUtil.getTravelWarnings(model, lesson -> lesson.getDate().value.equals(today), "  ")
                + "\n\n" + formatLessonsAwaitingNotes(lessonsAwaitingNotes, studentLabel)
                + "\n\n" + HomeworkQueries.summary(model.getAddressBook(), null, true, today)
                + "\n\n" + formatDues(model));
    }

    private static String formatLessonsToday(Model model, List<Lesson> lessons,
            Function<Lesson, String> studentLabel) {
        int minutes = lessons.stream().mapToInt(lesson -> lesson.getDuration().value).sum();
        String header = String.format(MESSAGE_TODAY_HEADER, lessons.size(), LessonDuration.format(minutes));
        if (lessons.isEmpty()) {
            return header + "\n" + MESSAGE_NO_LESSONS_TODAY;
        }
        List<Lesson> visible = lessons.stream().limit(MAX_DISPLAYED_LESSONS).toList();
        String rows = visible.stream()
                .map(lesson -> "\n" + formatLessonToday(lesson, studentLabel.apply(lesson),
                        !model.getConflictingLessons(lesson).isEmpty()))
                .collect(Collectors.joining());
        return header + rows + omitted(lessons, visible);
    }

    private static String formatLessonToday(Lesson lesson, String student, boolean isOverlapping) {
        String line = String.format("  %s  %-18s %-10s %s", lesson.getTimeRange(), student,
                lesson.getSubject(), Messages.formatVenue(lesson));
        if (!lesson.isScheduled()) {
            line += "  (" + lesson.getStatus() + ")";
        }
        return isOverlapping ? line + "  " + AgendaCommand.MESSAGE_OVERLAPS : line;
    }

    private static String formatLessonsAwaitingNotes(List<Lesson> lessons, Function<Lesson, String> studentLabel) {
        String header = String.format(MESSAGE_AWAITING_NOTES_HEADER, lessons.size());
        if (lessons.isEmpty()) {
            return header + "\n" + MESSAGE_ALL_RECORDED;
        }
        List<Lesson> visible = lessons.stream().limit(MAX_DISPLAYED_LESSONS).toList();
        String rows = visible.stream()
                .map(lesson -> String.format("\n  %-10s  %-18s %-10s %s",
                        PAST_DAY_FORMAT.format(lesson.getDate().value), studentLabel.apply(lesson),
                        lesson.getSubject(), MESSAGE_NOT_COMPLETED))
                .collect(Collectors.joining());
        return header + rows + omitted(lessons, visible);
    }

    private static String omitted(List<Lesson> all, List<Lesson> visible) {
        return all.size() == visible.size() ? "" : "\n  Showing " + visible.size() + " of " + all.size()
                + " lessons.";
    }

    private String formatDues(Model model) {
        // The ledger starts the list with the heading only when somebody owes money, but an empty section keeps it
        String dues = Ledger.formatDues(model.getAddressBook(), today);
        return dues.startsWith(MESSAGE_DUES_HEADER + "\n") ? dues : MESSAGE_DUES_HEADER + "\n" + dues;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TodayCommand otherCommand)) {
            return false;
        }

        return today.equals(otherCommand.today);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("today", today)
                .toString();
    }
}
