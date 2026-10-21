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

/** Deletes assignments, requiring explicit confirmation when feedback would be lost. */
public class HomeworkDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "hw delete INDEX [confirm]";

    public static final String MESSAGE_SUCCESS = "Deleted homework %1$d: %2$s. "
            + "Remaining shown indices are unchanged; run 'hw list' to refresh.";
    public static final String MESSAGE_CONFIRM_FEEDBACK = "This homework has feedback recorded: %1$s. "
            + "Type 'hw delete %2$d confirm' to delete it anyway. Nothing is deleted.";

    private static final Logger logger = LogsCenter.getLogger(HomeworkDeleteCommand.class);

    private final Index index;
    private final boolean isConfirmed;

    /**
     * Creates a command that deletes the homework at {@code index} of the last homework list. If
     * {@code isConfirmed} is false, nothing is deleted when feedback would be lost.
     */
    public HomeworkDeleteCommand(Index index, boolean isConfirmed) {
        requireNonNull(index);
        this.index = index;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Homework target = HomeworkCommandSupport.target(model, index);
        if (target.getFeedback().isPresent() && !isConfirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRM_FEEDBACK, target.getDescription(),
                    index.getOneBased()))
                    .withConfirmationCommand("hw delete " + index.getOneBased() + " confirm");
        }
        model.deleteHomework(target);
        logger.fine("Deleted a homework item");
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(), target.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HomeworkDeleteCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && isConfirmed == otherCommand.isConfirmed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, isConfirmed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
