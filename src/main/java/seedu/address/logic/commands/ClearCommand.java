package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears all students, lessons, homework, follow-up tasks and charge/payment records from TutorFlow, but only once
 * the user has confirmed.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRM_KEYWORD = "confirm";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes ALL students, lessons, homework, follow-up tasks and charge/payment records from TutorFlow. "
            + "Nothing is deleted unless '" + CONFIRM_KEYWORD + "' is added.\n"
            + "Parameters: [" + CONFIRM_KEYWORD + "]\n"
            + "Example: " + COMMAND_WORD + " " + CONFIRM_KEYWORD;

    public static final String MESSAGE_SUCCESS = "All data cleared.";
    public static final String MESSAGE_CONFIRMATION =
            "This deletes ALL students, lessons, homework, follow-up tasks and charge/payment records. "
            + "Type '" + COMMAND_WORD + " " + CONFIRM_KEYWORD + "' to proceed.";

    private final boolean isConfirmed;

    /**
     * Creates a {@code ClearCommand} that only asks for confirmation.
     */
    public ClearCommand() {
        this(false);
    }

    /**
     * Creates a {@code ClearCommand} that clears all data only if {@code isConfirmed} is true.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (!isConfirmed) {
            return new CommandResult(MESSAGE_CONFIRMATION);
        }
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ClearCommand otherCommand)) {
            return false;
        }

        return isConfirmed == otherCommand.isConfirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
