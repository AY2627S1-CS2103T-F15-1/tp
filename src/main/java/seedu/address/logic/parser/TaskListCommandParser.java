package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TaskListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new TaskListCommand object
 */
public class TaskListCommandParser implements Parser<TaskListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TaskListCommand
     * and returns a TaskListCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TaskListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ALL, PREFIX_STUDENT);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskListCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_STUDENT);

        boolean isShowingAll = ParserUtil.parseFlag(argMultimap, PREFIX_ALL);
        Index studentIndex = ParserUtil.parseOptional(argMultimap, PREFIX_STUDENT, ParserUtil::parseIndex);
        return new TaskListCommand(isShowingAll, studentIndex);
    }

}
