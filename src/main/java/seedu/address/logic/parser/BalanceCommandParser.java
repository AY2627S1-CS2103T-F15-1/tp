package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a displayed student index for the balance command. */
public class BalanceCommandParser implements Parser<BalanceCommand> {
    @Override
    public BalanceCommand parse(String args) throws ParseException {
        try {
            ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT);
            ParserUtil.requirePrefixesPresent(arguments, BalanceCommand.MESSAGE_USAGE, PREFIX_STUDENT);
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT);
            arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT);
            return new BalanceCommand(ParserUtil.parseIndex(arguments.getValue(PREFIX_STUDENT).orElseThrow()));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, BalanceCommand.MESSAGE_USAGE), e);
        }
    }
}
