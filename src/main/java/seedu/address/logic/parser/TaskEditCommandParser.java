package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TaskEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new TaskEditCommand object
 */
public class TaskEditCommandParser implements Parser<TaskEditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TaskEditCommand
     * and returns a TaskEditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TaskEditCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DESCRIPTION, PREFIX_DUE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TaskEditCommand.MESSAGE_USAGE),
                    pe);
        }
        if (argMultimap.getValue(PREFIX_DESCRIPTION).isEmpty()) {
            throw new ParseException(TaskEditCommand.MESSAGE_USAGE);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DESCRIPTION, PREFIX_DUE);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_DESCRIPTION, PREFIX_DUE);

        String description = TaskAddCommandParser.parseDescription(argMultimap.getValue(PREFIX_DESCRIPTION).get());
        String dueValue = argMultimap.getValue(PREFIX_DUE).map(String::strip).orElse(null);
        boolean isClearingDue = TaskEditCommand.CLEAR_DUE_VALUE.equals(dueValue);
        LocalDate newDue = dueValue == null || isClearingDue ? null : ParserUtil.parseDate(dueValue).value;
        return new TaskEditCommand(index, description, newDue, isClearingDue);
    }

}
