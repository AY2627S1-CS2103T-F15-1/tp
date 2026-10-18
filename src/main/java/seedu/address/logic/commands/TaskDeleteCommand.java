package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.task.FollowUpTask;

/** Previews and explicitly confirms destructive deletion of a follow-up. */
public class TaskDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "task delete INDEX [confirm]";
    private final Index index;
    private final boolean confirmed;

    /** Creates a preview or confirmed deletion of a shown task. */
    public TaskDeleteCommand(Index index, boolean confirmed) {
        this.index = index;
        this.confirmed = confirmed;
    }

    @Override
    public Optional<String> undoLabel() {
        return Optional.of("task deletion");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        FollowUpTask task = TaskCommandSupport.task(model, index);
        if (!confirmed) {
            return new CommandResult("Delete task: " + task.description() + "? Type 'task delete "
                    + index.getOneBased() + " confirm' to proceed.");
        }
        model.deleteTask(task);
        return new CommandResult("Deleted task " + index.getOneBased() + ": " + task.description()
                + ". Remaining shown indices are unchanged; run 'task list' to refresh.");
    }
}
