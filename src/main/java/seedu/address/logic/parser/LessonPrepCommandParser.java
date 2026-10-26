package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonPrepCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a required preparation note using the shared printable-notes policy. */
public class LessonPrepCommandParser implements Parser<LessonPrepCommand> {
    @Override
    public LessonPrepCommand parse(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_NOTES);
        values.verifyNoDuplicatePrefixesFor(PREFIX_NOTES);
        values.verifyNoEmptyValuesFor(PREFIX_NOTES);
        Index index = ParserUtil.parseIndex(values.getPreamble(), LessonPrepCommand.MESSAGE_USAGE);
        String notes = values.getValue(PREFIX_NOTES)
                .orElseThrow(() -> new ParseException(LessonPrepCommand.MESSAGE_USAGE));
        return new LessonPrepCommand(index, ParserUtil.parseNotes(notes));
    }
}
