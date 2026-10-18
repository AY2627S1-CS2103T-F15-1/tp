package seedu.address.logic.parser;

import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a required displayed student index for an account query. */
public class BalanceCommandParser implements Parser<BalanceCommand> {
    private static final Prefix STUDENT = new Prefix("st/");

    @Override
    public BalanceCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(" " + args.strip(), STUDENT);
        values.verifyNoDuplicatePrefixesFor(STUDENT);
        values.verifyNoEmptyValuesFor(STUDENT);
        if (!values.getPreamble().isEmpty() || values.getValue(STUDENT).isEmpty()) {
            throw new ParseException(BalanceCommand.MESSAGE_USAGE);
        }
        return new BalanceCommand(ParserUtil.parseIndex(values.getValue(STUDENT).orElseThrow()));
    }
}
