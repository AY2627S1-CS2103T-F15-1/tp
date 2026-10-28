package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.LessonSeriesCancelCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Requires an explicit, standalone confirmation after the displayed occurrence index. */
public class LessonSeriesCancelCommandParser implements Parser<LessonSeriesCancelCommand> {
    @Override
    public LessonSeriesCancelCommand parse(String args) throws ParseException {
        String[] words = args.strip().split("\\s+");
        if (words.length != 3 || !words[0].equals("cancel") || !words[2].equals("confirm")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    LessonSeriesCancelCommand.MESSAGE_USAGE));
        }
        return new LessonSeriesCancelCommand(
                ParserUtil.parseIndex(words[1], LessonSeriesCancelCommand.MESSAGE_USAGE));
    }
}
