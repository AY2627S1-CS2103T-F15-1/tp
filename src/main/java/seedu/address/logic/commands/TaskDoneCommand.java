package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TaskCommandParser;
import seedu.address.model.Model;
import seedu.address.model.task.Task;

/**
 * Marks a task that is shown as done. The task stays in the history, which {@code task list all/} shows.
 */
public class TaskDoneCommand extends Command {

    public static final String COMMAND_WORD = TaskCommandParser.COMMAND_WORD + " done";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Marks the task identified by the index number "
            + "used in the last task list as done.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Completed task %1$d: %2$s. "
            + TaskCommandUtil.MESSAGE_REFRESH_KEPT;
    public static final String MESSAGE_ALREADY_DONE = "That task is already done.";

    private final Index index;

    /**
     * Creates a {@code TaskDoneCommand} for the task at {@code index} of the shown tasks.
     */
    public TaskDoneCommand(Index index) {
        requireNonNull(index);
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Task task = TaskCommandUtil.getShownTask(model, index);
        if (task.isDone()) {
            throw new CommandException(MESSAGE_ALREADY_DONE);
        }
        model.setTask(task, task.markDone());
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(), task.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof TaskDoneCommand otherCommand && index.equals(otherCommand.index));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).toString();
    }
}
