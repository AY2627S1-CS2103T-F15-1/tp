package seedu.address.logic.parser;

import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the student number of an {@code archive} or a {@code restore} command. */
public class ArchiveCommandParser implements Parser<ArchiveCommand> {
    private final boolean shouldArchive;

    /**
     * Creates a parser for {@code archive} if {@code shouldArchive} is true, and for {@code restore} otherwise.
     */
    public ArchiveCommandParser(boolean shouldArchive) {
        this.shouldArchive = shouldArchive;
    }

    @Override
    public ArchiveCommand parse(String args) throws ParseException {
        String usage = shouldArchive ? ArchiveCommand.MESSAGE_USAGE_ARCHIVE : ArchiveCommand.MESSAGE_USAGE_RESTORE;
        return new ArchiveCommand(ParserUtil.parseIndex(args, usage), shouldArchive);
    }
}
