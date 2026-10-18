package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.task.FollowUpTask;

/** Completes a follow-up without deleting it from history or shifting shown indices. */
public class TaskDoneCommand extends Command {
    public static final String MESSAGE_USAGE = "task done INDEX (from the last task list)";
    private final Index index;

    /** Creates a completion against a shown row. */
    public TaskDoneCommand(Index index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        FollowUpTask task = TaskCommandSupport.task(model, index);
        if (task.done()) {
            throw new CommandException("That task is already done.");
        }
        model.setTask(task, task.complete());
        return new CommandResult("Completed task " + index.getOneBased() + ": " + task.description()
                + ". Shown task indices stay unchanged; run 'task list' to refresh.");
    }
}
