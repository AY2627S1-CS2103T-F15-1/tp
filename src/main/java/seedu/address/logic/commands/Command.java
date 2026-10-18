package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns a short description of what an undo restores if this command deletes data, such as
     * "student deletion", or an empty value for any other command. A snapshot is only kept when the command
     * also changes the data, so a preview or a rejected attempt never replaces an earlier undo step.
     */
    public Optional<String> undoLabel() {
        return Optional.empty();
    }

}
