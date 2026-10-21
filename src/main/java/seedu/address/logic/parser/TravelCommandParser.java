package seedu.address.logic.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.TravelCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.TravelGap;

/** Parses exactly one non-negative whole-number travel buffer. */
public class TravelCommandParser implements Parser<TravelCommand> {
    private static final Pattern FORMAT = Pattern.compile("buffer/\\s*([0-9]+)");

    @Override
    public TravelCommand parse(String args) throws ParseException {
        Matcher matcher = FORMAT.matcher(args.strip());
        if (!matcher.matches()) {
            throw new ParseException(TravelCommand.MESSAGE_USAGE);
        }
        try {
            int minutes = Integer.parseInt(matcher.group(1));
            if (TravelGap.isValidBuffer(minutes)) {
                return new TravelCommand(minutes);
            }
        } catch (NumberFormatException e) {
            throw new ParseException(TravelCommand.MESSAGE_USAGE);
        }
        throw new ParseException(TravelCommand.MESSAGE_USAGE);
    }
}
