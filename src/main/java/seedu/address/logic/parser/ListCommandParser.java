package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses student filters, combining academic level, subject, and archive scope with AND. */
public class ListCommandParser implements Parser<ListCommand> {
    private static final Prefix PREFIX_ARCHIVED = new Prefix("archived/");

    @Override
    public ListCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args,
                PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_ARCHIVED, PREFIX_ALL);
        values.verifyNoUnexpectedPrefixes(List.of(), List.of());
        values.verifyNoDuplicatePrefixesFor(PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_ARCHIVED, PREFIX_ALL);
        values.verifyNoEmptyValuesFor(PREFIX_LEVEL, PREFIX_SUBJECT);
        if (!values.getPreamble().isBlank()) {
            throw ParserUtil.invalidFormat(ListCommand.MESSAGE_USAGE);
        }
        boolean isArchivedOnly = ParserUtil.parseFlag(values, PREFIX_ARCHIVED);
        boolean isArchivedIncluded = ParserUtil.parseFlag(values, PREFIX_ALL);
        if (isArchivedOnly && isArchivedIncluded) {
            throw new ParseException(ListCommand.MESSAGE_USAGE);
        }
        return new ListCommand(ParserUtil.parseOptional(values, PREFIX_LEVEL, ParserUtil::parseLevel),
                ParserUtil.parseOptional(values, PREFIX_SUBJECT, ParserUtil::parseSubject),
                isArchivedOnly, isArchivedIncluded);
    }
}
