package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVERY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LessonCommandParser;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonDate;
import seedu.address.model.lesson.LessonDuration;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.lesson.LessonTime;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Schedules a series of weekly lessons for a student in the displayed student list (user story 9).
 * The series is all or nothing: if any occurrence is invalid or a duplicate, no lesson is added.
 */
public class LessonRepeatCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " repeat";

    public static final int MIN_COUNT = 2;
    public static final int MAX_COUNT = 52;
    public static final int MIN_EVERY = 1;
    public static final int MAX_EVERY = 12;
    public static final int DEFAULT_EVERY = 1;

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Schedules a series of weekly lessons for the "
            + "student identified by the index number used in the displayed student list. The first lesson is on "
            + "the given date and each later one is the same time of day, " + PREFIX_EVERY + "WEEKS weeks after "
            + "the one before (default 1). The whole series is added or none of it is.\n"
            + "Parameters: " + PREFIX_STUDENT + "STUDENT_INDEX [" + PREFIX_SUBJECT + "SUBJECT] "
            + PREFIX_DATE + "START_DATE " + PREFIX_TIME + "TIME [" + PREFIX_DURATION + "MINUTES] ["
            + PREFIX_VENUE + "VENUE] " + PREFIX_COUNT + "COUNT [" + PREFIX_EVERY + "WEEKS]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT + "4 " + PREFIX_DATE + "2026-10-29 "
            + PREFIX_TIME + "16:00 " + PREFIX_COUNT + "4";

    public static final String MESSAGE_COUNT_CONSTRAINTS = "COUNT is " + MIN_COUNT + "-" + MAX_COUNT
            + "; WEEKS is " + MIN_EVERY + "-" + MAX_EVERY + " (default " + DEFAULT_EVERY + "). "
            + "All occurrences must be within two years.";
    public static final String MESSAGE_SUCCESS_WEEKLY = "Scheduled %1$d weekly occurrences.";
    public static final String MESSAGE_SUCCESS_EVERY = "Scheduled %1$d occurrences, every %2$d weeks.";
    public static final String MESSAGE_PAST_NOT_ACCEPTED = "past/ is not accepted here: a series starts today "
            + "or later.";
    public static final String MESSAGE_NOT_DISPLAYED = "The new lessons are not in the displayed list. "
            + "Run 'agenda' or 'lesson list " + PREFIX_STUDENT + "%1$d' to number them.";
    public static final String MESSAGE_DATE_IN_PAST = "That date is in the past. A series starts today or later.";
    public static final String MESSAGE_DUPLICATE_SERIES = LessonCommandUtil.MESSAGE_DUPLICATE_LESSON
            + " No occurrences were added.";
    public static final String MESSAGE_OVERLAP_LINE = "Warning: the lesson on %1$s overlaps with %2$s";
    public static final String MESSAGE_OVERLAP_SUMMARY = "Warning: %1$d of %2$d occurrences overlap with other "
            + "lessons. The first is on %3$s. Run 'agenda week/' to check them.";
    public static final String MESSAGE_ROLLED_BACK = "The series could not be scheduled, so no occurrences were "
            + "added.";
    public static final String MESSAGE_STUDENT_ARCHIVED =
            "Restore this archived student before scheduling a new lesson.";

    /** The most overlapping occurrences that are listed one by one before the warning is summarised. */
    static final int MAX_LISTED_OVERLAPS = 3;

    private static final Logger logger = LogsCenter.getLogger(LessonRepeatCommand.class);

    private final Index studentIndex;
    private final Subject subject; // null if the student's only subject is to be used
    private final LessonDate startDate;
    private final LessonTime time;
    private final LessonDuration duration;
    private final Venue venue; // null if the student's venue is to be used
    private final int count;
    private final int everyWeeks;
    private final Clock clock;

    /**
     * Creates a {@code LessonRepeatCommand} for the student at {@code studentIndex}.
     * The {@code subject} and {@code venue} may be null. {@code count} must be between {@value #MIN_COUNT} and
     * {@value #MAX_COUNT} and {@code everyWeeks} between {@value #MIN_EVERY} and {@value #MAX_EVERY}.
     */
    public LessonRepeatCommand(Index studentIndex, Subject subject, LessonDate startDate, LessonTime time,
            LessonDuration duration, Venue venue, int count, int everyWeeks) {
        this(studentIndex, subject, startDate, time, duration, venue, count, everyWeeks,
                Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code LessonRepeatCommand} as above that takes today's date from {@code clock}, so that the date
     * rules can be tested at their boundaries.
     */
    public LessonRepeatCommand(Index studentIndex, Subject subject, LessonDate startDate, LessonTime time,
            LessonDuration duration, Venue venue, int count, int everyWeeks, Clock clock) {
        requireNonNull(studentIndex);
        requireNonNull(clock);
        requireNonNull(startDate);
        requireNonNull(time);
        requireNonNull(duration);
        assert isValidCount(count) : "The number of occurrences must be validated by the caller";
        assert isValidEvery(everyWeeks) : "The gap in weeks must be validated by the caller";
        this.studentIndex = studentIndex;
        this.subject = subject;
        this.startDate = startDate;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
        this.count = count;
        this.everyWeeks = everyWeeks;
        this.clock = clock;
    }

    /** Returns true if {@code count} is an allowed number of occurrences. */
    public static boolean isValidCount(int count) {
        return count >= MIN_COUNT && count <= MAX_COUNT;
    }

    /** Returns true if {@code weeks} is an allowed gap between occurrences. */
    public static boolean isValidEvery(int weeks) {
        return weeks >= MIN_EVERY && weeks <= MAX_EVERY;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = LessonCommandUtil.getStudent(model, studentIndex);
        if (student.isArchived()) {
            throw new CommandException(MESSAGE_STUDENT_ARCHIVED);
        }
        List<Lesson> series = buildSeries(student);

        // Check the whole series before anything is created, so that it is all or nothing
        for (Lesson lesson : series) {
            if (model.hasLesson(lesson)) {
                throw new CommandException(MESSAGE_DUPLICATE_SERIES);
            }
        }
        String warnings = formatOverlapWarnings(model, series);

        List<Lesson> shownBefore = new ArrayList<>(model.getFilteredLessonList());
        addAllOrNone(model, series);
        logger.fine("Scheduled a series of " + series.size() + " lessons");

        String summary = everyWeeks == DEFAULT_EVERY
                ? String.format(MESSAGE_SUCCESS_WEEKLY, count)
                : String.format(MESSAGE_SUCCESS_EVERY, count, everyWeeks);
        String details = warnings + LessonCommandUtil.getTravelWarningsForLessons(model, series);
        if (isRenumbered(shownBefore, model.getFilteredLessonList())) {
            // Like lesson add, end with the new numbering so that the tutor does not use stale numbers.
            return new CommandResult(summary + details + Messages.formatLessonList(model.getFilteredLessonList()))
                    .withDisplayedLessons(model.getFilteredLessonList());
        }
        return new CommandResult(summary + "\n" + String.format(MESSAGE_NOT_DISPLAYED, studentIndex.getOneBased())
                + details);
    }

    /**
     * Returns true if a lesson that was displayed before the series was added is now displayed under a different
     * number, so that the tutor must refresh the numbers before using them.
     */
    private static boolean isRenumbered(List<Lesson> shownBefore, List<Lesson> shownAfter) {
        return shownAfter.size() < shownBefore.size()
                || !shownAfter.subList(0, shownBefore.size()).equals(shownBefore);
    }

    private List<Lesson> buildSeries(Person student) throws CommandException {
        LessonCommandUtil.requireNotInPast(startDate, MESSAGE_DATE_IN_PAST, clock);
        LessonCommandUtil.requireEndsBeforeMidnight(time, duration);
        Subject chosenSubject = LessonCommandUtil.chooseSubject(student, subject);
        Venue lessonVenue = venue != null ? venue : student.getVenue().orElse(null);
        UUID seriesId = UUID.randomUUID();

        List<Lesson> series = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LessonDate date = new LessonDate(startDate.value.plusWeeks((long) i * everyWeeks).toString());
            LessonCommandUtil.requireNearFuture(date, clock);
            series.add(new Lesson(student.getName(), student.getPhone(), chosenSubject, date, time, duration,
                    lessonVenue, LessonStatus.SCHEDULED, null, null)
                    .snapshotStudent(student).withSeries(seriesId));
        }
        return series;
    }

    /**
     * Adds every lesson of {@code series}. If adding any of them fails, the ones added so far are removed again.
     */
    private static void addAllOrNone(Model model, List<Lesson> series) throws CommandException {
        List<Lesson> added = new ArrayList<>();
        try {
            for (Lesson lesson : series) {
                model.addLesson(lesson);
                added.add(lesson);
            }
        } catch (RuntimeException e) {
            logger.warning("Rolling back a failed series: " + e.getMessage());
            added.forEach(model::deleteLesson);
            throw new CommandException(MESSAGE_ROLLED_BACK);
        }
    }

    /**
     * Returns the overlap warnings for the occurrences, one line each, or one summary line if there are many.
     * Occurrences are on different dates, so they can only overlap lessons that already exist.
     */
    private static String formatOverlapWarnings(Model model, List<Lesson> series) {
        List<String> lines = new ArrayList<>();
        Lesson first = null;
        for (Lesson lesson : series) {
            List<Lesson> overlaps = model.getConflictingLessons(lesson);
            if (overlaps.isEmpty()) {
                continue;
            }
            first = first == null ? lesson : first;
            lines.add(String.format(MESSAGE_OVERLAP_LINE, lesson.getDate(), Messages.formatOverlaps(overlaps)));
        }
        if (lines.isEmpty()) {
            return "";
        }
        if (lines.size() > MAX_LISTED_OVERLAPS) {
            return "\n" + String.format(MESSAGE_OVERLAP_SUMMARY, lines.size(), series.size(), first.getDate());
        }
        return "\n" + String.join("\n", lines);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonRepeatCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && Objects.equals(subject, otherCommand.subject)
                && startDate.equals(otherCommand.startDate)
                && time.equals(otherCommand.time)
                && duration.equals(otherCommand.duration)
                && Objects.equals(venue, otherCommand.venue)
                && count == otherCommand.count
                && everyWeeks == otherCommand.everyWeeks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("subject", subject)
                .add("startDate", startDate)
                .add("time", time)
                .add("duration", duration)
                .add("venue", venue)
                .add("count", count)
                .add("everyWeeks", everyWeeks)
                .toString();
    }
}
