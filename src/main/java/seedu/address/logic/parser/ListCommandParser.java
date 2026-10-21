package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses student filters, combining academic level, subject, and archive scope with AND. */
public class ListCommandParser implements Parser<ListCommand> {
    private static final Prefix PREFIX_ARCHIVED = new Prefix("archived/");

    @Override
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args,
                PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_ARCHIVED, PREFIX_ALL);
        values.verifyNoDuplicatePrefixesFor(PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_ARCHIVED, PREFIX_ALL);
        values.verifyNoEmptyValuesFor(PREFIX_LEVEL, PREFIX_SUBJECT);
        boolean archived = values.getValue(PREFIX_ARCHIVED).isPresent();
        boolean all = values.getValue(PREFIX_ALL).isPresent();
        if (!values.getPreamble().isBlank() || archived && all
                || !values.getValue(PREFIX_ARCHIVED).orElse("").isEmpty()
                || !values.getValue(PREFIX_ALL).orElse("").isEmpty()) {
            throw new ParseException(ListCommand.MESSAGE_USAGE);
        }
        return new ListCommand(ParserUtil.parseOptional(values, PREFIX_LEVEL, ParserUtil::parseLevel),
                ParserUtil.parseOptional(values, PREFIX_SUBJECT, ParserUtil::parseSubject), archived, all);
    }
}
