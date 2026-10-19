package seedu.address.logic.commands;

import seedu.address.model.Model;

/** Retries saving session changes; persistence is performed by the logic manager before success is returned. */
public class SaveCommand extends Command {
    public static final String COMMAND_WORD = "save";
    public static final String MESSAGE_USAGE = "save: Retries saving unsaved changes. Parameters: none.";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult("All changes have been saved.");
    }
}
