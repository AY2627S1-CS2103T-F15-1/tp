package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE;

import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.TaskCommandParser;
import seedu.address.model.Model;
import seedu.address.model.task.Task;

/**
 * Changes the description, and optionally the due date, of a task that is shown (user story 31).
 */
public class TaskEditCommand extends Command {

    public static final String COMMAND_WORD = TaskCommandParser.COMMAND_WORD + " edit";

    public static final String CLEAR_DUE_VALUE = "-";

    public static final String MESSAGE_DUE_EXPLANATION = "Leave out " + PREFIX_DUE + " to keep the due date, give "
            + "a date to change it, or write " + PREFIX_DUE + CLEAR_DUE_VALUE + " to remove it.";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX " + PREFIX_DESCRIPTION + "TEXT ["
            + PREFIX_DUE + "DATE|" + PREFIX_DUE + CLEAR_DUE_VALUE + "]\n"
            + MESSAGE_DUE_EXPLANATION + "\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DESCRIPTION + "Call parent again " + PREFIX_DUE
            + CLEAR_DUE_VALUE;

    public static final String MESSAGE_SUCCESS = "Updated task %1$d: %2$s. " + TaskCommandUtil.MESSAGE_REFRESH_KEPT;

    private final Index index;
    private final String description;
    private final LocalDate newDue; // null unless the due date is to change
    private final boolean isClearingDue;

    /**
     * Creates a {@code TaskEditCommand} for the task at {@code index} of the shown tasks. The due date is kept
     * unless {@code newDue} is given, or removed if {@code isClearingDue} is true, which excludes {@code newDue}.
     */
    public TaskEditCommand(Index index, String description, LocalDate newDue, boolean isClearingDue) {
        requireNonNull(index);
        requireNonNull(description);
        assert Task.isValidDescription(description) : "The description must be validated by the caller";
        assert !(isClearingDue && newDue != null) : "The due date cannot be both changed and removed";
        this.index = index;
        this.description = description.strip();
        this.newDue = newDue;
        this.isClearingDue = isClearingDue;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Task task = TaskCommandUtil.getShownTask(model, index);

        Task edited = task.withDescription(description);
        if (isClearingDue) {
            edited = edited.withDue(null);
        } else if (newDue != null) {
            edited = edited.withDue(newDue);
        }
        model.setTask(task, edited);
        return new CommandResult(String.format(MESSAGE_SUCCESS, index.getOneBased(), edited.getDescription()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TaskEditCommand otherCommand)) {
            return false;
        }

        return index.equals(otherCommand.index)
                && description.equals(otherCommand.description)
                && Objects.equals(newDue, otherCommand.newDue)
                && isClearingDue == otherCommand.isClearingDue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("description", description)
                .add("newDue", newDue)
                .add("isClearingDue", isClearingDue)
                .toString();
    }
}
