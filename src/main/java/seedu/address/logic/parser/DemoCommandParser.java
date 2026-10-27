package seedu.address.logic.parser;

import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DemoCommand object.
 */
public class DemoCommandParser implements Parser<DemoCommand> {

    public static final String COMMAND_WORD = "demo";
    public static final String CONFIRM_KEYWORD = "confirm";
    public static final String MESSAGE_USAGE = "Usage: demo [confirm]";

    /**
     * Parses the given {@code String} of arguments in the context of the DemoCommand
     * and returns a DemoCommand object for execution.
     * @throws ParseException if the arguments are neither empty nor the confirm keyword
     */
    @Override
    public DemoCommand parse(String args) throws ParseException {
        String trimmedArgs = args.strip();
        boolean isConfirmed = trimmedArgs.equals(CONFIRM_KEYWORD);
        if (!trimmedArgs.isEmpty() && !isConfirmed) {
            throw new ParseException(MESSAGE_USAGE);
        }
        return new DemoCommand(isConfirmed);
    }
}
