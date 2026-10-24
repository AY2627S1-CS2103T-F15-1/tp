package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_BUFFER;

import seedu.address.logic.commands.TravelCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.TravelGap;

/** Parses exactly one non-negative whole-number travel buffer. */
public class TravelCommandParser implements Parser<TravelCommand> {
    @Override
    public TravelCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + args.strip(), PREFIX_BUFFER);
        ParserUtil.requirePrefixesPresent(arguments, TravelCommand.MESSAGE_USAGE, PREFIX_BUFFER);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_BUFFER);
        arguments.verifyNoEmptyValuesFor(PREFIX_BUFFER);
        String value = arguments.getValue(PREFIX_BUFFER).orElseThrow();
        if (!value.matches("[0-9]+")) {
            throw new ParseException(TravelGap.MESSAGE_CONSTRAINTS);
        }
        try {
            int minutes = Integer.parseInt(value);
            if (TravelGap.isValidBuffer(minutes)) {
                return new TravelCommand(minutes);
            }
        } catch (NumberFormatException e) {
            throw new ParseException(TravelGap.MESSAGE_CONSTRAINTS);
        }
        throw new ParseException(TravelGap.MESSAGE_CONSTRAINTS);
    }
}
