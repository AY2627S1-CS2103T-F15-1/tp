package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TaskDoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new TaskDoneCommand object
 */
public class TaskDoneCommandParser implements Parser<TaskDoneCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TaskDoneCommand
     * and returns a TaskDoneCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TaskDoneCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new TaskDoneCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskDoneCommand.MESSAGE_USAGE),
                    pe);
        }
    }

}
