package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_AMOUNT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.math.BigDecimal;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the displayed student index and a positive decimal payment amount. */
public class PaymentCommandParser implements Parser<PaymentCommand> {
    @Override
    public PaymentCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_AMOUNT);
        ParserUtil.requirePrefixesPresent(arguments, PaymentCommand.MESSAGE_USAGE, PREFIX_STUDENT, PREFIX_AMOUNT);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_AMOUNT);
        arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_AMOUNT);
        Index index = ParserUtil.parseIndex(arguments.getValue(PREFIX_STUDENT).orElseThrow());
        String amountText = arguments.getValue(PREFIX_AMOUNT).orElseThrow();
        if (!amountText.matches("\\$?[0-9]+(?:\\.[0-9]{1,2})?")) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        BigDecimal amount = new BigDecimal(amountText.startsWith("$") ? amountText.substring(1) : amountText);
        if (amount.signum() <= 0) {
            throw new ParseException(PaymentCommand.MESSAGE_AMOUNT_CONSTRAINTS);
        }
        return new PaymentCommand(index, amount);
    }
}
