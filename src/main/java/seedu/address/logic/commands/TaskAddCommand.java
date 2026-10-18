package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.task.FollowUpTask;

/** Creates a follow-up tied to exactly one displayed parent. */
public class TaskAddCommand extends Command {
    public static final String MESSAGE_USAGE = "task add st/INDEX|lesson/INDEX|hw/INDEX|pay/INDEX "
            + "desc/TEXT [due/DATE] (choose exactly one link; use hw list or pay list before those indices)";
    private final FollowUpTask.LinkType type;
    private final Index index;
    private final String description;
    private final LocalDate dueDate;

    /** Stores a validated description, optional due date, and parent selection. */
    public TaskAddCommand(FollowUpTask.LinkType type, Index index, String description, LocalDate dueDate) {
        this.type = type;
        this.index = index;
        this.description = description;
        this.dueDate = dueDate;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        TaskCommandSupport.Target target = TaskCommandSupport.target(model, type, index);
        model.addTask(new FollowUpTask(UUID.randomUUID(), target.studentId(), type, target.id(),
                description, dueDate, false));
        return new CommandResult("Added task: " + description + ". Run 'task list' to display its index.");
    }
}
