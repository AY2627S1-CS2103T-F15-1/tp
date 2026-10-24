package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.TaskDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new TaskDeleteCommand object
 */
public class TaskDeleteCommandParser implements Parser<TaskDeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TaskDeleteCommand
     * and returns a TaskDeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TaskDeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        boolean isConfirmed = false;
        String indexArg = trimmedArgs;

        // The optional confirm keyword is the last word, e.g. "1 confirm"
        String[] words = trimmedArgs.split("\\s+");
        if (words.length == 2 && words[1].equals(TaskDeleteCommand.CONFIRM_KEYWORD)) {
            isConfirmed = true;
            indexArg = words[0];
        }

        try {
            return new TaskDeleteCommand(ParserUtil.parseIndex(indexArg), isConfirmed);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    TaskDeleteCommand.MESSAGE_USAGE), pe);
        }
    }

}
