package seedu.address.logic.parser;

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
        return new TaskDoneCommand(ParserUtil.parseIndex(args, TaskDoneCommand.MESSAGE_USAGE));
    }

}
