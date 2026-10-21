package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PAST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.util.List;
import java.util.Objects;
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
 * Schedules a lesson for a student in the displayed student list.
 */
public class LessonAddCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Schedules a lesson for the student identified by "
            + "the index number used in the displayed student list. "
            + "The subject can be left out if the student takes only one, the duration defaults to "
            + LessonDuration.DEFAULT_MINUTES + " minutes, and the venue defaults to the student's venue.\n"
            + "Parameters: " + PREFIX_STUDENT + "STUDENT_INDEX [" + PREFIX_SUBJECT + "SUBJECT] "
            + PREFIX_DATE + "DATE " + PREFIX_TIME + "TIME [" + PREFIX_DURATION + "MINUTES] ["
            + PREFIX_VENUE + "VENUE] [" + PREFIX_PAST + "]\n"
            + "DATE may also be 'today' or 'tmr'. Add " + PREFIX_PAST + " to record a lesson that has already "
            + "taken place (at most 2 years ago).\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT + "1 " + PREFIX_SUBJECT + "Math "
            + PREFIX_DATE + "2026-12-22 " + PREFIX_TIME + "16:30 " + PREFIX_DURATION + "90";

    public static final String MESSAGE_SUCCESS = "Scheduled: %1$s, %2$s";
    public static final String MESSAGE_DATE_IN_PAST = "That date is in the past. Add " + PREFIX_PAST
            + " to record a lesson that has already taken place.";
    public static final String MESSAGE_SUBJECT_NOT_TAKEN = LessonCommandUtil.MESSAGE_SUBJECT_NOT_TAKEN;
    public static final String MESSAGE_SUBJECT_REQUIRED = LessonCommandUtil.MESSAGE_SUBJECT_REQUIRED;

    private static final Logger logger = LogsCenter.getLogger(LessonAddCommand.class);

    private final Index studentIndex;
    private final Subject subject; // null if the student's only subject is to be used
    private final LessonDate date;
    private final LessonTime time;
    private final LessonDuration duration;
    private final Venue venue; // null if the student's venue is to be used
    private final boolean isPast; // true if a lesson that has already taken place may be recorded

    /**
     * Creates a {@code LessonAddCommand} to schedule a lesson for the student at {@code studentIndex}.
     * The {@code subject} and {@code venue} may be null, and the other fields must not be null.
     */
    public LessonAddCommand(Index studentIndex, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration, Venue venue) {
        this(studentIndex, subject, date, time, duration, venue, false);
    }

    /**
     * Creates a {@code LessonAddCommand} as above. If {@code isPast} is true, a date before today is accepted so
     * that a lesson that has already taken place can be recorded (user story 8).
     */
    public LessonAddCommand(Index studentIndex, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration, Venue venue, boolean isPast) {
        requireNonNull(studentIndex);
        requireNonNull(date);
        requireNonNull(time);
        requireNonNull(duration);
        this.studentIndex = studentIndex;
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
        this.isPast = isPast;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = LessonCommandUtil.getStudent(model, studentIndex);
        if (student.isArchived()) {
            throw new CommandException("Restore this archived student before scheduling a new lesson.");
        }
        Lesson lesson = createLesson(student);

        if (model.hasLesson(lesson)) {
            throw new CommandException(LessonCommandUtil.MESSAGE_DUPLICATE_LESSON);
        }
        int previousLessonCount = model.getFilteredLessonList().size();
        model.addLesson(lesson);
        logger.fine("Scheduled lesson");

        String numbering = formatChangedLessonNumbers(model.getFilteredLessonList(), lesson, previousLessonCount);
        CommandResult result = new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(lesson),
                Messages.formatVenue(lesson)) + LessonCommandUtil.getOverlapWarning(model, lesson) + numbering);
        return numbering.isEmpty() ? result : result.withDisplayedLessons(model.getFilteredLessonList());
    }

    /**
     * Shows the new numbers if inserting a matching lesson moved any previously displayed lesson down the list.
     * Additions outside the active filter, or after its last lesson, leave the existing numbers unchanged.
     */
    private static String formatChangedLessonNumbers(List<Lesson> lessons, Lesson added, int previousLessonCount) {
        int addedIndex = lessons.indexOf(added);
        if (addedIndex < 0 || addedIndex >= previousLessonCount) {
            return "";
        }
        return Messages.formatLessonList(lessons);
    }

    private Lesson createLesson(Person student) throws CommandException {
        if (isPast) {
            LessonCommandUtil.requireNotTooFarPast(date);
        } else {
            LessonCommandUtil.requireNotInPast(date, MESSAGE_DATE_IN_PAST);
        }
        LessonCommandUtil.requireNearFuture(date);
        LessonCommandUtil.requireEndsBeforeMidnight(time, duration);

        Venue lessonVenue = venue != null ? venue : student.getVenue().orElse(null);
        return new Lesson(student.getName(), student.getPhone(), LessonCommandUtil.chooseSubject(student, subject),
                date, time, duration, lessonVenue, LessonStatus.SCHEDULED, null, null).snapshotStudent(student);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonAddCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && Objects.equals(subject, otherCommand.subject)
                && date.equals(otherCommand.date)
                && time.equals(otherCommand.time)
                && duration.equals(otherCommand.duration)
                && Objects.equals(venue, otherCommand.venue)
                && isPast == otherCommand.isPast;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("subject", subject)
                .add("date", date)
                .add("time", time)
                .add("duration", duration)
                .add("venue", venue)
                .add("isPast", isPast)
                .toString();
    }
}
