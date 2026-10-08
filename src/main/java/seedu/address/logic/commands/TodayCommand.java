package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;

/**
 * Shows what a day requires: the lessons that take place on it, and the past lessons that are still waiting to be
 * marked completed. It only reads the lessons, so the displayed lesson list is left unchanged.
 */
public class TodayCommand extends Command {

    public static final String COMMAND_WORD = "today";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows today's lessons and the past lessons that are "
            + "not yet marked completed.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_TODAY_HEADER = "TODAY'S LESSONS (%1$d, %2$s)";
    public static final String MESSAGE_AWAITING_NOTES_HEADER = "LESSONS AWAITING NOTES (%1$d)";
    public static final String MESSAGE_NO_LESSONS_TODAY = "  No lessons today.";
    public static final String MESSAGE_ALL_RECORDED = "  All past lessons are recorded.";
    public static final String MESSAGE_NOT_COMPLETED = "not yet marked completed";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter PAST_DAY_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);
    private static final Logger logger = LogsCenter.getLogger(TodayCommand.class);

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

        return new CommandResult(DATE_FORMAT.format(today)
                + "\n\n" + formatLessonsToday(model, lessonsToday)
                + "\n\n" + formatLessonsAwaitingNotes(lessonsAwaitingNotes));
    }

    private static String formatLessonsToday(Model model, List<Lesson> lessons) {
        int minutes = lessons.stream().mapToInt(lesson -> lesson.getDuration().value).sum();
        String header = String.format(MESSAGE_TODAY_HEADER, lessons.size(), LessonDuration.format(minutes));
        if (lessons.isEmpty()) {
            return header + "\n" + MESSAGE_NO_LESSONS_TODAY;
        }
        return header + lessons.stream()
                .map(lesson -> "\n" + formatLessonToday(lesson, !model.getConflictingLessons(lesson).isEmpty()))
                .collect(Collectors.joining());
    }

    private static String formatLessonToday(Lesson lesson, boolean isOverlapping) {
        String line = String.format("  %s  %-18s %-10s %s", lesson.getTimeRange(), lesson.getStudentName(),
                lesson.getSubject(), Messages.formatVenue(lesson));
        if (!lesson.isScheduled()) {
            line += "  (" + lesson.getStatus() + ")";
        }
        return isOverlapping ? line + "  " + AgendaCommand.MESSAGE_OVERLAPS : line;
    }

    private static String formatLessonsAwaitingNotes(List<Lesson> lessons) {
        String header = String.format(MESSAGE_AWAITING_NOTES_HEADER, lessons.size());
        if (lessons.isEmpty()) {
            return header + "\n" + MESSAGE_ALL_RECORDED;
        }
        return header + lessons.stream()
                .map(lesson -> String.format("\n  %-10s  %-18s %-10s %s",
                        PAST_DAY_FORMAT.format(lesson.getDate().value), lesson.getStudentName(),
                        lesson.getSubject(), MESSAGE_NOT_COMPLETED))
                .collect(Collectors.joining());
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
