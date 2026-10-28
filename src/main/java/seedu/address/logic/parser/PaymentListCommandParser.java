package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PaymentListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code pay list [st/STUDENT_INDEX]}. */
public class PaymentListCommandParser implements Parser<PaymentListCommand> {

    @Override
    public PaymentListCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT);
        if (!arguments.getPreamble().isEmpty()) {
            throw ParserUtil.invalidFormat(PaymentListCommand.MESSAGE_USAGE);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT);
        arguments.verifyNoEmptyValuesFor(PREFIX_STUDENT);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseOptional(arguments, PREFIX_STUDENT, ParserUtil::parseIndex);
        } catch (ParseException e) {
            // Section 3.2: an index that is not a positive whole number is a format error, with the usage string
            throw ParserUtil.invalidFormat(PaymentListCommand.MESSAGE_USAGE);
        }
        return new PaymentListCommand(studentIndex);
    }
}
