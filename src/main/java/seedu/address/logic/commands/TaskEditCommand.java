package seedu.address.logic.commands;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.task.FollowUpTask;

/** Corrects task content while preserving its identity, parent, and completion history. */
public class TaskEditCommand extends Command {
    public static final String MESSAGE_USAGE = "task edit INDEX desc/TEXT [due/DATE|due/-]"
            + "\nOmit due/ to retain the date, or use due/- to remove it.";
    private final Index index;
    private final String description;
    private final LocalDate dueDate;
    private final boolean changeDueDate;

    /** Stores an edit; a changed null date explicitly removes the due date. */
    public TaskEditCommand(Index index, String description, LocalDate dueDate, boolean changeDueDate) {
        this.index = index;
        this.description = description;
        this.dueDate = dueDate;
        this.changeDueDate = changeDueDate;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        FollowUpTask task = TaskCommandSupport.task(model, index);
        model.setTask(task, task.edit(description, changeDueDate ? dueDate : task.dueDate()));
        return new CommandResult("Updated task " + index.getOneBased() + ": " + description
                + ". Shown task indices stay unchanged; run 'task list' to refresh.");
    }
}
