package seedu.address.logic.parser;

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
        return new LessonMissedCommand(ParserUtil.parseIndex(args, LessonMissedCommand.MESSAGE_USAGE));
    }
}
