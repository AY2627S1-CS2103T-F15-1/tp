package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.LessonMissedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed lesson index for marking a lesson as missed.
 */
public class LessonMissedCommandParser implements Parser<LessonMissedCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonMissedCommand
     * and returns a LessonMissedCommand object for execution.
     * @throws ParseException if the arguments are not a single positive lesson index
     */
    @Override
    public LessonMissedCommand parse(String args) throws ParseException {
        try {
            return new LessonMissedCommand(ParserUtil.parseIndex(args));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonMissedCommand.MESSAGE_USAGE), pe);
        }
    }
}
