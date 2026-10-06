package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        boolean isConfirmed = false;
        String indexArg = trimmedArgs;

        // The optional confirm keyword is the last word, e.g. "3 confirm"
        String[] words = trimmedArgs.split("\\s+");
        if (words.length == 2 && words[1].equals(DeleteCommand.CONFIRM_KEYWORD)) {
            isConfirmed = true;
            indexArg = words[0];
        }

        try {
            Index index = ParserUtil.parseIndex(indexArg);
            return new DeleteCommand(index, isConfirmed);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

}
