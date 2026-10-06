package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a student to TutorFlow.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student to TutorFlow. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_LEVEL + "LEVEL "
            + PREFIX_SUBJECT + "SUBJECT... "
            + PREFIX_RATE + "RATE "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_VENUE + "VENUE]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_LEVEL + "S4 "
            + PREFIX_SUBJECT + "Math "
            + PREFIX_SUBJECT + "Science "
            + PREFIX_RATE + "50 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_VENUE + "311, Clementi Ave 2, #02-25";

    public static final String MESSAGE_SUCCESS = "New student added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This student already exists in TutorFlow.";
    public static final String MESSAGE_SAME_NAME_WARNING =
            "\nNote: you already have a student named %1$s. Both are now in your list.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        // Different students can share a name, so a student with the same name is added with a warning
        boolean hasSameName = model.getAddressBook().getPersonList().stream().anyMatch(toAdd::hasSameName);

        model.addPerson(toAdd);
        String message = String.format(MESSAGE_SUCCESS, Messages.format(toAdd));
        if (hasSameName) {
            message += String.format(MESSAGE_SAME_NAME_WARNING, toAdd.getName());
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
