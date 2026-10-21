package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TaskCommandParser;
import seedu.address.model.Model;
import seedu.address.model.task.Task;

/**
 * Deletes a task that is shown, once the user has confirmed it.
 */
public class TaskDeleteCommand extends Command {

    public static final String COMMAND_WORD = TaskCommandParser.COMMAND_WORD + " delete";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes the task identified by the index number "
            + "used in the last task list. Without '" + CONFIRM_KEYWORD + "' it only asks you to confirm.\n"
            + "Parameters: INDEX (must be a positive integer) [" + CONFIRM_KEYWORD + "]\n"
            + "Example: " + COMMAND_WORD + " 1 " + CONFIRM_KEYWORD;

    public static final String MESSAGE_CONFIRMATION = "Delete task: %1$s? Type '" + COMMAND_WORD + " %2$d "
            + CONFIRM_KEYWORD + "' to proceed.";
    public static final String MESSAGE_SUCCESS = "Deleted task %1$d: %2$s. Remaining shown indices are "
            + "unchanged; run 'task list' to refresh.";

    private final Index index;
    private final boolean isConfirmed;

    /**
     * Creates a {@code TaskDeleteCommand} for the task at {@code index} of the shown tasks. The task is only
     * deleted if {@code isConfirmed} is true.
     */
    public TaskDeleteCommand(Index index, boolean isConfirmed) {
        requireNonNull(index);
        this.index = index;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Task task = TaskCommandUtil.getShownTask(model, index);
        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRMATION, task.getDescription(),
                    index.getOneBased()));
        }
        model.deleteTask(task);
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(), task.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskDeleteCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index) && isConfirmed == otherCommand.isConfirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
