package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * An index that may be followed by a confirmation keyword, as in {@code delete 3} and {@code delete 3 confirm}.
 */
public final class ConfirmableIndex {

    private final Index index;
    private final boolean isConfirmed;

    private ConfirmableIndex(Index index, boolean isConfirmed) {
        this.index = index;
        this.isConfirmed = isConfirmed;
    }

    /**
     * Parses {@code args}, which must be one index optionally followed by {@code confirmKeyword}.
     *
     * @param usage the usage message shown if {@code args} is anything else.
     * @throws ParseException if {@code args} is not an index with an optional {@code confirmKeyword}.
     */
    public static ConfirmableIndex parse(String args, String confirmKeyword, String usage) throws ParseException {
        requireNonNull(args);
        requireNonNull(confirmKeyword);
        String[] words = args.strip().split("(?U)\\s+");
        boolean isConfirmed = words.length == 2 && words[1].equals(confirmKeyword);
        if (words.length > 2 || words.length == 2 && !isConfirmed) {
            throw ParserUtil.invalidFormat(usage);
        }
        return new ConfirmableIndex(ParserUtil.parseIndex(words[0], usage), isConfirmed);
    }

    public Index getIndex() {
        return index;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}
