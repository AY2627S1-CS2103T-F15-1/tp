package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** Moves homework through its lifecycle, allowing explicit backward corrections. */
public class HomeworkStatusCommand extends Command {
    public static final String MESSAGE_USAGE = "hw status INDEX s/STATUS";

    public static final String MESSAGE_SUCCESS = "Homework %1$d: %2$s is now: %3$s";
    public static final String MESSAGE_UNCHANGED = "That homework is already marked %1$s.";
    public static final String MESSAGE_MOVED_BACK =
            "\nNote: moved back from %1$s to %2$s. Feedback already recorded is kept.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkStatusCommand.class);

    private final Index index;
    private final HomeworkStatus status;

    /** Creates a command that sets the homework at {@code index} of the last homework list to {@code status}. */
    public HomeworkStatusCommand(Index index, HomeworkStatus status) {
        requireNonNull(index);
        requireNonNull(status);
        this.index = index;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Homework target = HomeworkCommandSupport.target(model, index);
        if (target.getStatus() == status) {
            throw new CommandException(String.format(MESSAGE_UNCHANGED, status));
        }
        model.setHomework(target, target.withStatus(status));
        logger.fine("Changed the status of a homework item");

        String message = String.format(MESSAGE_SUCCESS, index.getOneBased(), target.getDescription(), status);
        if (status.compareTo(target.getStatus()) < 0) {
            message += String.format(MESSAGE_MOVED_BACK, target.getStatus(), status);
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkStatusCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && status == otherCommand.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("status", status)
                .toString();
    }
}
