package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REASON;

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
import seedu.address.model.lesson.LessonStatus;

/**
 * Cancels a lesson in the displayed lesson list. The lesson is kept, so that the record of the booking remains.
 */
public class LessonCancelCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " cancel";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Cancels the lesson identified by the index "
            + "number used in the displayed lesson list. The lesson stays in the agenda as cancelled and no "
            + "longer overlaps other lessons.\n"
            + "Parameters: INDEX (must be a positive integer) [" + PREFIX_REASON + "REASON]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_REASON + "Student unwell";

    public static final String MESSAGE_SUCCESS = "Cancelled: %1$s.";
    public static final String MESSAGE_REASON = "\nReason: %1$s";
    public static final String MESSAGE_ALREADY_CANCELLED = "That lesson is already cancelled.";
    public static final String MESSAGE_COMPLETED_LESSON = "Cannot cancel a completed lesson.";

    private static final Logger logger = LogsCenter.getLogger(LessonCancelCommand.class);

    private final Index lessonIndex;
    private final String reason; // null if no reason was given

    /**
     * Creates a {@code LessonCancelCommand} to cancel the lesson at {@code lessonIndex}.
     * The {@code reason} may be null.
     */
    public LessonCancelCommand(Index lessonIndex, String reason) {
        requireNonNull(lessonIndex);
        this.lessonIndex = lessonIndex;
        this.reason = reason;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson target = LessonCommandUtil.getLesson(model, lessonIndex);
        if (target.getStatus() == LessonStatus.CANCELLED) {
            throw new CommandException(MESSAGE_ALREADY_CANCELLED);
        }
        if (target.getStatus() == LessonStatus.COMPLETED) {
            throw new CommandException(MESSAGE_COMPLETED_LESSON);
        }

        model.setLesson(target, target.cancel(reason));
        logger.info("Cancelled a lesson of " + target.getStudentName());

        String message = String.format(MESSAGE_SUCCESS, Messages.format(target));
        return new CommandResult(reason == null ? message : message + String.format(MESSAGE_REASON, reason));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonCancelCommand otherCommand)) {
            return false;
        }

        return lessonIndex.equals(otherCommand.lessonIndex) && Objects.equals(reason, otherCommand.reason);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("lessonIndex", lessonIndex)
                .add("reason", reason)
                .toString();
    }
}
