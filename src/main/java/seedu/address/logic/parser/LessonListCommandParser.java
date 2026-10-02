package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new LessonListCommand object
 */
public class LessonListCommandParser implements Parser<LessonListCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonListCommand
     * and returns a LessonListCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonListCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_ALL);
        ParserUtil.requirePrefixesPresent(argMultimap, LessonListCommand.MESSAGE_USAGE, PREFIX_STUDENT);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT);

        Index studentIndex = ParserUtil.parseIndex(argMultimap.getValue(PREFIX_STUDENT).get());
        return new LessonListCommand(studentIndex, ParserUtil.parseFlag(argMultimap, PREFIX_ALL));
    }

}
