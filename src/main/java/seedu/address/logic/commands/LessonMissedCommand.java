package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LessonCommandParser;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;

/**
 * Records that a scheduled lesson in the displayed lesson list was missed.
 */
public class LessonMissedCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " missed";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Marks the lesson identified by the index "
            + "in the displayed lesson list as missed, retaining its booking details and notes.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Marked missed: %1$s.";
    public static final String MESSAGE_ALREADY_MISSED = "That lesson is already marked as missed.";
    public static final String MESSAGE_CANCELLED_LESSON = "Cannot mark a cancelled lesson as missed.";
    public static final String MESSAGE_COMPLETED_LESSON = "Cannot mark a completed lesson as missed.";
    public static final String MESSAGE_FUTURE_LESSON =
            "That lesson is scheduled for %1$s. Mark it missed on or after that date.";

    private final Index lessonIndex;
    private final Clock clock;

    /**
     * Creates a command to mark the lesson at {@code lessonIndex} as missed, using the system date.
     */
    public LessonMissedCommand(Index lessonIndex) {
        this(lessonIndex, Clock.systemDefaultZone());
    }

    /**
     * Creates a command with an explicit clock for testing local date boundaries.
     */
    public LessonMissedCommand(Index lessonIndex, Clock clock) {
        requireNonNull(lessonIndex);
        requireNonNull(clock);
        this.lessonIndex = lessonIndex;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson target = LessonCommandUtil.getLesson(model, lessonIndex);
        if (target.getStatus() == LessonStatus.CANCELLED) {
            throw new CommandException(MESSAGE_CANCELLED_LESSON);
        }
        if (target.getStatus() == LessonStatus.COMPLETED) {
            throw new CommandException(MESSAGE_COMPLETED_LESSON);
        }
        if (target.getDate().value.isAfter(LocalDate.now(clock))) {
            throw new CommandException(String.format(MESSAGE_FUTURE_LESSON, target.getDate()));
        }
        if (target.getStatus() == LessonStatus.MISSED) {
            return new CommandResult(MESSAGE_ALREADY_MISSED);
        }

        model.setLesson(target, target.miss());
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(target)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LessonMissedCommand otherCommand)) {
            return false;
        }
        return lessonIndex.equals(otherCommand.lessonIndex) && clock.equals(otherCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("lessonIndex", lessonIndex).toString();
    }
}
