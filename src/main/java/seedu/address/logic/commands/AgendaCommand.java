package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.view.DetailView;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;

/**
 * Shows the lessons of a day or of the week containing the day, in the order that they take place. The numbers it
 * shows are the lesson indices that the other lesson commands use.
 */
public class AgendaCommand extends Command {

    public static final String COMMAND_WORD = "agenda";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the lessons of a day, which is today "
            + "if no date is given, or of the Monday to Sunday week containing it if " + PREFIX_WEEK
            + " is given.\n"
            + "Parameters: [" + PREFIX_DATE + "DATE] [" + PREFIX_WEEK + "]\n"
            + "Examples: " + COMMAND_WORD + " " + PREFIX_DATE + "2026-12-22 " + PREFIX_WEEK;

    public static final String MESSAGE_DAY_HEADER = "Agenda — %1$s (%2$s)";
    public static final String MESSAGE_WEEK_HEADER = "Week of %1$s to %2$s (%3$s)";
    public static final String MESSAGE_NO_LESSONS_ON_DAY = "No lessons on %1$s.";
    public static final String MESSAGE_EMPTY_DAY = "  — no lessons —";
    public static final String MESSAGE_OVERLAPS = "⚠ overlaps";

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE dd MMM uuuu", Locale.ENGLISH);
    private static final Logger logger = LogsCenter.getLogger(AgendaCommand.class);

    private final LocalDate date;
    private final boolean isWeek;

    /**
     * Creates an {@code AgendaCommand} to show the lessons of {@code date}, or of the week containing it if
     * {@code isWeek} is true.
     */
    public AgendaCommand(LocalDate date, boolean isWeek) {
        requireNonNull(date);
        this.date = date;
        this.isWeek = isWeek;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate first = isWeek ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : date;
        LocalDate last = isWeek ? first.plusDays(6) : date;
        model.updateFilteredLessonList(lesson -> !lesson.getDate().value.isBefore(first)
                && !lesson.getDate().value.isAfter(last));
        logger.fine("Showing the agenda from " + first + " to " + last);

        List<Lesson> lessons = model.getFilteredLessonList();
        if (isWeek) {
            return new CommandResult(formatWeek(model, first, last, lessons))
                    .withDetailView(weekView(model, first, last, lessons)).withDisplayedLessons(lessons);
        }
        if (lessons.isEmpty()) {
            String message = String.format(MESSAGE_NO_LESSONS_ON_DAY, DAY_FORMAT.format(date));
            return new CommandResult(message).withDetailView(new DetailView(message, message, false, List.of()))
                    .withDisplayedLessons(lessons);
        }
        String header = String.format(MESSAGE_DAY_HEADER, DAY_FORMAT.format(date), summarise(lessons));
        return new CommandResult(header + formatLessons(model, lessons, date)).withDetailView(new DetailView(header,
                header, false, List.of(new DetailView.Section("", lessonLines(model, lessons, date)))))
                .withDisplayedLessons(lessons);
    }

    /** Returns the week as the detail pane draws it: a section for each day. */
    private static DetailView weekView(Model model, LocalDate first, LocalDate last, List<Lesson> lessons) {
        String header = String.format(MESSAGE_WEEK_HEADER, DAY_FORMAT.format(first), DAY_FORMAT.format(last),
                summarise(lessons));
        List<DetailView.Section> sections = new ArrayList<>();
        for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
            LocalDate currentDay = day;
            List<Lesson> lessonsOfDay = lessons.stream()
                    .filter(lesson -> lesson.getDate().value.equals(currentDay)).collect(Collectors.toList());
            List<DetailView.Line> lines = lessonsOfDay.isEmpty()
                    ? List.of(DetailView.Line.plain(MESSAGE_EMPTY_DAY.strip()))
                    : lessonLines(model, lessons, day);
            sections.add(new DetailView.Section(DAY_FORMAT.format(day) + " (" + summarise(lessonsOfDay) + ")",
                    lines));
        }
        return new DetailView(header, header, false, sections);
    }

    /**
     * Returns one line for each lesson on {@code day}, numbered by its position in {@code shownLessons}. A cancelled
     * lesson is struck through and a lesson that overlaps another is marked as a warning.
     */
    private static List<DetailView.Line> lessonLines(Model model, List<Lesson> shownLessons, LocalDate day) {
        List<DetailView.Line> lines = new ArrayList<>();
        for (int i = 0; i < shownLessons.size(); i++) {
            Lesson lesson = shownLessons.get(i);
            if (lesson.getDate().value.equals(day)) {
                boolean isOverlapping = !model.getConflictingLessons(lesson).isEmpty();
                lines.add(new DetailView.Line(formatLine(i + 1, lesson, isOverlapping),
                        lesson.getStatus() == LessonStatus.CANCELLED, isOverlapping));
            }
        }
        return lines;
    }

    private static String formatWeek(Model model, LocalDate first, LocalDate last, List<Lesson> lessons) {
        StringBuilder week = new StringBuilder(String.format(MESSAGE_WEEK_HEADER, DAY_FORMAT.format(first),
                DAY_FORMAT.format(last), summarise(lessons)));
        for (LocalDate day = first; !day.isAfter(last); day = day.plusDays(1)) {
            LocalDate currentDay = day;
            List<Lesson> lessonsOfDay = lessons.stream()
                    .filter(lesson -> lesson.getDate().value.equals(currentDay)).collect(Collectors.toList());
            week.append("\n").append(DAY_FORMAT.format(day)).append(" (").append(summarise(lessonsOfDay))
                    .append(")");
            week.append(lessonsOfDay.isEmpty() ? "\n" + MESSAGE_EMPTY_DAY
                    : formatLessons(model, lessons, day));
        }
        return week.toString();
    }

    /**
     * Returns the number of lessons that will take place and their total length, e.g. "3 lessons, 4h 30m".
     * Cancelled lessons are left out because they will not take place.
     */
    private static String summarise(List<Lesson> lessons) {
        List<Lesson> takingPlace = lessons.stream()
                .filter(lesson -> lesson.getStatus() != LessonStatus.CANCELLED).collect(Collectors.toList());
        int minutes = takingPlace.stream().mapToInt(lesson -> lesson.getDuration().value).sum();
        return takingPlace.size() + (takingPlace.size() == 1 ? " lesson, " : " lessons, ")
                + LessonDuration.format(minutes);
    }

    /**
     * Returns one numbered line for each lesson on {@code day}. The number of a lesson is its position in
     * {@code shownLessons}, which is the index that other commands use for it.
     */
    private static String formatLessons(Model model, List<Lesson> shownLessons, LocalDate day) {
        return IntStream.range(0, shownLessons.size())
                .filter(i -> shownLessons.get(i).getDate().value.equals(day))
                .mapToObj(i -> "\n" + formatLine(i + 1, shownLessons.get(i),
                        !model.getConflictingLessons(shownLessons.get(i)).isEmpty()))
                .collect(Collectors.joining());
    }

    private static String formatLine(int number, Lesson lesson, boolean isOverlapping) {
        String line = String.format("%2d. %s  %-18s %-10s %s", number, lesson.getTimeRange(),
                lesson.getStudentName(), lesson.getSubject(), Messages.formatVenue(lesson));
        if (lesson.getStatus() != LessonStatus.SCHEDULED) {
            line += "  (" + lesson.getStatus() + ")";
        }
        return isOverlapping ? line + "  " + MESSAGE_OVERLAPS : line;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AgendaCommand otherCommand)) {
            return false;
        }

        return date.equals(otherCommand.date) && isWeek == otherCommand.isWeek;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("isWeek", isWeek)
                .toString();
    }
}
