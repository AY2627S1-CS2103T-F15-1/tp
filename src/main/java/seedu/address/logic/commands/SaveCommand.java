package seedu.address.logic.commands;

import seedu.address.model.Model;

/** Retries saving session changes; persistence is performed by the logic manager before success is returned. */
public class SaveCommand extends Command {
    public static final String COMMAND_WORD = "save";
    public static final String MESSAGE_USAGE = "save: Retries saving unsaved changes. Parameters: none.";

    public static final String MESSAGE_SAVED = "All changes have been saved.";
    public static final String MESSAGE_DEMO = "Nothing is saved in the demo. "
            + "Type demo exit to go back to your own data.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(model.isDemoMode() ? MESSAGE_DEMO : MESSAGE_SAVED);
    }
}
