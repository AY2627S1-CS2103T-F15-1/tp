package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Starts the guided tour of the window. The tour only points at parts of the window and offers examples to try, so
 * the command itself changes nothing.
 */
public class TourCommand extends Command {

    public static final String COMMAND_WORD = "tour";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Starts the guided tour, which points out the parts "
            + "of the window and offers examples to try. It changes none of your data, and Esc leaves it at any "
            + "time.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Starting the guided tour. Press Esc at any time to leave it.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS).withTourRequested();
    }
}
