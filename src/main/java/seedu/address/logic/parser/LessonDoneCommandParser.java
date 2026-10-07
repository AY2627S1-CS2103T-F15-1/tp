package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonDoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for completing a lesson and recording optional teaching notes.
 */
public class LessonDoneCommandParser implements Parser<LessonDoneCommand> {

    @Override
    public LessonDoneCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NOTES);
        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonDoneCommand.MESSAGE_USAGE), pe);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NOTES);
        return new LessonDoneCommand(index,
                ParserUtil.parseOptional(argMultimap, PREFIX_NOTES, ParserUtil::parseNotes));
    }
}
