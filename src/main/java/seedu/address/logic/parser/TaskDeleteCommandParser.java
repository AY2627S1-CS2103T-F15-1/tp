package seedu.address.logic.parser;

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
        ConfirmableIndex target = ConfirmableIndex.parse(args, TaskDeleteCommand.CONFIRM_KEYWORD,
                TaskDeleteCommand.MESSAGE_USAGE);
        return new TaskDeleteCommand(target.getIndex(), target.isConfirmed());
    }

}
