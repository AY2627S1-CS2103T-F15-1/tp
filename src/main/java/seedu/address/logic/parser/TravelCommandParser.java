package seedu.address.logic.parser;

import seedu.address.logic.commands.TravelCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the single bounded travel-buffer setting. */
public class TravelCommandParser implements Parser<TravelCommand> {
    private static final Prefix BUFFER = new Prefix("buffer/");

    @Override
    public TravelCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, BUFFER);
        ParserUtil.requirePrefixesPresent(values, TravelCommand.MESSAGE_USAGE, BUFFER);
        values.verifyNoDuplicatePrefixesFor(BUFFER);
        try {
            String raw = values.getValue(BUFFER).orElseThrow();
            int minutes = Integer.parseInt(raw);
            if (!raw.matches("[0-9]+") || minutes < 0 || minutes > 180) {
                throw new NumberFormatException();
            }
            return new TravelCommand(minutes);
        } catch (NumberFormatException e) {
            throw new ParseException(TravelCommand.MESSAGE_USAGE);
        }
    }
}
