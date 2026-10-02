package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            + PREFIX_VENUE + "VENUE]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT + "1 " + PREFIX_SUBJECT + "Math "
            + PREFIX_DATE + "2026-12-22 " + PREFIX_TIME + "16:30 " + PREFIX_DURATION + "90";

    public static final String MESSAGE_SUCCESS = "Scheduled: %1$s, %2$s";
    public static final String MESSAGE_DATE_IN_PAST = "That date is in the past. Use 'lesson add' only for "
            + "upcoming lessons.";
    public static final String MESSAGE_SUBJECT_NOT_TAKEN = "%1$s is not recorded as taking %2$s. "
            + "Add the subject with 'edit' first.";
    public static final String MESSAGE_SUBJECT_REQUIRED = "%1$s takes more than one subject (%2$s). "
            + "Specify one with " + PREFIX_SUBJECT + "SUBJECT.";

    private static final Logger logger = LogsCenter.getLogger(LessonAddCommand.class);

    private final Index studentIndex;
    private final Subject subject; // null if the student's only subject is to be used
    private final LessonDate date;
    private final LessonTime time;
    private final LessonDuration duration;
    private final Venue venue; // null if the student's venue is to be used

    /**
     * Creates a {@code LessonAddCommand} to schedule a lesson for the student at {@code studentIndex}.
     * The {@code subject} and {@code venue} may be null, and the other fields must not be null.
     */
    public LessonAddCommand(Index studentIndex, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration, Venue venue) {
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
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = getStudent(model);
        Lesson lesson = createLesson(student);

        if (model.hasLesson(lesson)) {
            throw new CommandException(LessonCommandUtil.MESSAGE_DUPLICATE_LESSON);
        }
        model.addLesson(lesson);
        logger.info("Scheduled a lesson for " + student.getName());

        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(lesson),
                Messages.formatVenue(lesson)) + LessonCommandUtil.getOverlapWarning(model, lesson));
    }

    private Person getStudent(Model model) throws CommandException {
        List<Person> shownStudents = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= shownStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return shownStudents.get(studentIndex.getZeroBased());
    }

    private Lesson createLesson(Person student) throws CommandException {
        if (date.value.isBefore(LocalDate.now())) {
            throw new CommandException(MESSAGE_DATE_IN_PAST);
        }
        LessonCommandUtil.requireNearFuture(date);
        LessonCommandUtil.requireEndsBeforeMidnight(time, duration);

        Venue lessonVenue = venue != null ? venue : student.getVenue().orElse(null);
        return new Lesson(student.getName(), student.getPhone(), chooseSubject(student), date, time, duration,
                lessonVenue, LessonStatus.SCHEDULED, null, null);
    }

    /**
     * Returns the subject the user asked for, or the only subject of {@code student} if none was asked for.
     */
    private Subject chooseSubject(Person student) throws CommandException {
        if (subject == null && student.getSubjects().size() == 1) {
            return student.getSubjects().iterator().next();
        }
        if (subject == null) {
            throw new CommandException(String.format(MESSAGE_SUBJECT_REQUIRED, student.getName(),
                    joinSubjects(student)));
        }
        if (!student.getSubjects().contains(subject)) {
            throw new CommandException(String.format(MESSAGE_SUBJECT_NOT_TAKEN, student.getName(), subject));
        }
        return subject;
    }

    private static String joinSubjects(Person student) {
        return student.getSubjects().stream().map(Subject::toString).sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
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
                && Objects.equals(venue, otherCommand.venue);
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
                .toString();
    }
}
