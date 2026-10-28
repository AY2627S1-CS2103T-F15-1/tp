package seedu.address.logic.parser;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Routes student namespace aliases through the same validated top-level parsers. */
public class StudentCommandParser implements Parser<Command> {
    public static final String COMMAND_WORD = "student";

    @Override
    public Command parse(String args) throws ParseException {
        String input = args.strip();
        String command = input.split(" ", 2)[0];
        return switch (command) {
            case "add", "edit", "delete", "find", "view", "list", "archive", "restore" ->
                new AddressBookParser().parseCommand(input);
            default -> throw new ParseException("Student commands: add, edit, delete, find, view, list, "
                    + "archive, restore.");
        };
    }
}
