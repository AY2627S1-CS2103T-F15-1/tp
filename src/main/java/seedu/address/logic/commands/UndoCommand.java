package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.UndoSnapshot;

/**
 * Restores the data as it was before the latest destructive command of this session.
 * Only one step is kept, and it is discarded by any later change.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Restores all data as it was before your latest "
            + "student deletion, clear, homework deletion or task deletion.\n"
            + "Only that one step is remembered, and only until you change any data again or close TutorFlow.\n"
            + "Parameters: none";

    public static final String MESSAGE_NOTHING_TO_UNDO = "Nothing to undo. Undo restores your latest deletion "
            + "or clear, and only until you make another change.";

    public static final String MESSAGE_SUCCESS = "Undone: %1$s. Your data is back as it was before.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Optional<UndoSnapshot> stored = model.getUndoSnapshot();
        if (stored.isEmpty()) {
            throw new CommandException(MESSAGE_NOTHING_TO_UNDO);
        }
        UndoSnapshot snapshot = stored.get();
        model.setAddressBook(snapshot.data());
        model.clearUndoSnapshot();
        return new CommandResult(String.format(MESSAGE_SUCCESS, snapshot.label()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UndoCommand;
    }

    @Override
    public int hashCode() {
        return UndoCommand.class.hashCode();
    }
}
