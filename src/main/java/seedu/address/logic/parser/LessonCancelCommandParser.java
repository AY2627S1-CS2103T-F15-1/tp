package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REASON;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonCancelCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new LessonCancelCommand object
 */
public class LessonCancelCommandParser implements Parser<LessonCancelCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonCancelCommand
     * and returns a LessonCancelCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonCancelCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_REASON);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonCancelCommand.MESSAGE_USAGE), pe);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REASON);

        return new LessonCancelCommand(index,
                ParserUtil.parseOptional(argMultimap, PREFIX_REASON, ParserUtil::parseReason));
    }

}
