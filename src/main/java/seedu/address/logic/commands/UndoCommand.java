package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.DestructiveActionType;
import seedu.address.model.Model;

public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Undoes your latest deletion or clear, " +
            "until you make another change and only within this open session.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Undone: %1$s. Your data is back as it was before.";
    public static final String MESSAGE_NOTHING =
            "Nothing to undo. Undo restores your latest deletion or clear, and only until you make another change.";


    @Override
    public CommandResult execute(Model model) throws CommandException {
        DestructiveActionType type = model.undoDestructiveAction();
        return new CommandResult(
                type == DestructiveActionType.NONE
                ? MESSAGE_NOTHING
                : String.format(MESSAGE_SUCCESS, type.description), false, false);
    }
}
