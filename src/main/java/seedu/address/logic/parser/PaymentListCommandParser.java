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
        Index studentIndex = ParserUtil.parseOptional(arguments, PREFIX_STUDENT, ParserUtil::parseIndex);
        return new PaymentListCommand(studentIndex);
    }
}
