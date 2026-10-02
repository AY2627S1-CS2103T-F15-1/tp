package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DURATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

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
import seedu.address.model.person.Venue;

/**
 * Reschedules a lesson in the displayed lesson list. Whatever is not given is retained.
 */
public class LessonMoveCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " move";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Reschedules the lesson identified by the index "
            + "number used in the displayed lesson list. Whatever is not given stays as it was.\n"
            + "Parameters: INDEX (must be a positive integer) [" + PREFIX_DATE + "DATE] [" + PREFIX_TIME
            + "TIME] [" + PREFIX_DURATION + "MINUTES] [" + PREFIX_VENUE + "VENUE]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DATE + "2026-12-24 " + PREFIX_TIME + "17:00";

    public static final String MESSAGE_NOT_MOVED = "Provide at least one of " + PREFIX_DATE + ", " + PREFIX_TIME
            + ", " + PREFIX_DURATION + " or " + PREFIX_VENUE + ".";
    public static final String MESSAGE_SUCCESS = "Rescheduled: %1$s — %2$s\n  from  %3$s\n  to    %4$s";
    public static final String MESSAGE_DATE_IN_PAST = "That date is in the past.";
    public static final String MESSAGE_COMPLETED_LESSON = "Cannot reschedule a completed lesson. "
            + "Use 'lesson done' to correct its notes instead.";
    public static final String MESSAGE_CANCELLED_LESSON = "Cannot reschedule a cancelled lesson. "
            + "Schedule a new one instead.";

    private static final Logger logger = LogsCenter.getLogger(LessonMoveCommand.class);

    private final Index lessonIndex;
    private final LessonDate date; // null if the date is retained
    private final LessonTime time; // null if the time is retained
    private final LessonDuration duration; // null if the duration is retained
    private final Venue venue; // null if the venue is retained

    /**
     * Creates a {@code LessonMoveCommand} to reschedule the lesson at {@code lessonIndex}.
     * Every field except {@code lessonIndex} may be null, which means that the lesson retains its own value.
     */
    public LessonMoveCommand(Index lessonIndex, LessonDate date, LessonTime time, LessonDuration duration,
            Venue venue) {
        requireNonNull(lessonIndex);
        this.lessonIndex = lessonIndex;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.venue = venue;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson target = LessonCommandUtil.getLesson(model, lessonIndex);
        requireMovable(target);
        Lesson moved = createMovedLesson(target);

        if (!moved.isSameLesson(target) && model.hasLesson(moved)) {
            throw new CommandException(LessonCommandUtil.MESSAGE_DUPLICATE_LESSON);
        }
        model.setLesson(target, moved);
        logger.info("Rescheduled a lesson of " + target.getStudentName());

        return new CommandResult(String.format(MESSAGE_SUCCESS, target.getStudentName(), target.getSubject(),
                Messages.formatSlot(target), Messages.formatSlot(moved))
                + LessonCommandUtil.getOverlapWarning(model, moved));
    }

    private static void requireMovable(Lesson lesson) throws CommandException {
        if (lesson.getStatus() == LessonStatus.COMPLETED) {
            throw new CommandException(MESSAGE_COMPLETED_LESSON);
        }
        if (lesson.getStatus() == LessonStatus.CANCELLED) {
            throw new CommandException(MESSAGE_CANCELLED_LESSON);
        }
    }

    private Lesson createMovedLesson(Lesson target) throws CommandException {
        if (date != null) {
            LessonCommandUtil.requireNotInPast(date, MESSAGE_DATE_IN_PAST);
            LessonCommandUtil.requireNearFuture(date);
        }
        LessonTime newTime = Objects.requireNonNullElse(time, target.getTime());
        LessonDuration newDuration = Objects.requireNonNullElse(duration, target.getDuration());
        LessonCommandUtil.requireEndsBeforeMidnight(newTime, newDuration);

        return target.reschedule(Objects.requireNonNullElse(date, target.getDate()), newTime, newDuration,
                venue != null ? venue : target.getVenue().orElse(null));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonMoveCommand otherCommand)) {
            return false;
        }

        return lessonIndex.equals(otherCommand.lessonIndex)
                && Objects.equals(date, otherCommand.date)
                && Objects.equals(time, otherCommand.time)
                && Objects.equals(duration, otherCommand.duration)
                && Objects.equals(venue, otherCommand.venue);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("lessonIndex", lessonIndex)
                .add("date", date)
                .add("time", time)
                .add("duration", duration)
                .add("venue", venue)
                .toString();
    }
}
