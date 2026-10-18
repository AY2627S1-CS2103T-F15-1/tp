package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.task.FollowUpTask;

/** Deletes assignments, requiring explicit confirmation when feedback would be lost. */
public class HomeworkDeleteCommand extends Command {
    public static final String MESSAGE_USAGE = "hw delete INDEX [confirm]";

    private final Index index;
    private final boolean confirmed;

    /** Creates a guarded deletion against a previously displayed index. */
    public HomeworkDeleteCommand(Index index, boolean confirmed) {
        this.index = index;
        this.confirmed = confirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Homework target = HomeworkCommandSupport.target(model, index);
        long taskCount = model.getAddressBook().getTaskList().stream()
                .filter(task -> task.linkType() == FollowUpTask.LinkType.HOMEWORK
                        && task.targetId().equals(target.getId())).count();
        if (taskCount > 0 && !confirmed) {
            return new CommandResult("This deletes homework '" + target.getDescription() + "', any feedback, and "
                    + taskCount + " linked task(s). Type 'hw delete " + index.getOneBased() + " confirm' to proceed.");
        }
        if (target.getFeedback().isPresent() && !confirmed) {
            return new CommandResult("This homework has feedback recorded: " + target.getDescription()
                    + ". Type 'hw delete " + index.getOneBased() + " confirm' to delete it anyway.");
        }
        model.deleteHomework(target);
        return new CommandResult("Deleted homework " + index.getOneBased() + ": " + target.getDescription()
                + ". Remaining shown indices are unchanged; run 'hw list' to refresh.");
    }
}
