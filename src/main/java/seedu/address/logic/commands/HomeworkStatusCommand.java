package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** Moves homework through its lifecycle, allowing explicit backward corrections. */
public class HomeworkStatusCommand extends Command {
    public static final String MESSAGE_USAGE = "hw status INDEX s/assigned|submitted|reviewed";

    private final Index index;
    private final HomeworkStatus status;

    /** Creates a lifecycle update against a previously displayed index. */
    public HomeworkStatusCommand(Index index, HomeworkStatus status) {
        this.index = index;
        this.status = status;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Homework target = HomeworkCommandSupport.target(model, index);
        if (target.getStatus() == status) {
            throw new CommandException("That homework is already marked " + status + ".");
        }
        String note = status.ordinal() < target.getStatus().ordinal()
                ? "\nNote: moved back from " + target.getStatus() + " to " + status + "." : "";
        model.setHomework(target, target.withStatus(status));
        return new CommandResult("Homework " + index.getOneBased() + ": " + target.getDescription()
                + " is now: " + status + note);
    }
}
