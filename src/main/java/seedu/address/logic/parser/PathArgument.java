package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The value of a {@code file/} parameter. The path runs to the end of the command, and a final word
 * {@code confirm} is the confirmation. A path that has to end in the word {@code confirm}, or that starts or ends
 * with a space, is put in double quotes.
 */
public final class PathArgument {

    public static final String CONFIRM = "confirm";
    public static final String MESSAGE_UNCLOSED_QUOTE = "The quote that opens the path is not closed.";
    public static final String MESSAGE_EMPTY_PATH = "Empty value for file/. Enter a value after this prefix.";

    private static final char QUOTE = '"';

    private final String path;
    private final boolean isConfirmed;

    private PathArgument(String path, boolean isConfirmed) {
        this.path = path;
        this.isConfirmed = isConfirmed;
    }

    /**
     * Splits {@code value}, the text after {@code file/}, into the path and whether the command is confirmed.
     *
     * @param usage the usage string of the command, shown when text follows a quoted path.
     * @throws ParseException if the path is empty, a quote is not closed, or text other than {@code confirm}
     *     follows a quoted path.
     */
    public static PathArgument parse(String value, String usage) throws ParseException {
        requireNonNull(value);
        String text = value.strip();
        if (text.startsWith(String.valueOf(QUOTE))) {
            int closing = text.indexOf(QUOTE, 1);
            if (closing < 0) {
                throw new ParseException(MESSAGE_UNCLOSED_QUOTE);
            }
            String rest = text.substring(closing + 1).strip();
            if (!rest.isEmpty() && !rest.equals(CONFIRM)) {
                throw ParserUtil.invalidFormat(usage);
            }
            return of(text.substring(1, closing), rest.equals(CONFIRM));
        }
        if (text.equals(CONFIRM)) {
            return of("", true);
        }
        if (text.endsWith(" " + CONFIRM) || text.endsWith("\t" + CONFIRM)) {
            return of(text.substring(0, text.length() - CONFIRM.length()).strip(), true);
        }
        return of(text, false);
    }

    private static PathArgument of(String path, boolean isConfirmed) throws ParseException {
        if (path.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_PATH);
        }
        return new PathArgument(path, isConfirmed);
    }

    /** Returns {@code path} as it is typed after {@code file/}, in quotes if it could not be read back otherwise. */
    public static String format(String path) {
        requireNonNull(path);
        boolean isQuoteNeeded = path.equals(CONFIRM) || path.endsWith(" " + CONFIRM) || !path.equals(path.strip());
        return isQuoteNeeded ? QUOTE + path + QUOTE : path;
    }

    public String getPath() {
        return path;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }
}
