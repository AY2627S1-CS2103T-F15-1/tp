package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User input split into its first word and the arguments that follow it. Both the top-level command word and the
 * sub-command word of {@code lesson} and {@code task} are read this way.
 */
final class CommandInput {

    private static final Pattern WORD_AND_ARGUMENTS = Pattern.compile("(?<word>\\S+)(?<arguments>.*)");

    private final String word;
    private final String arguments;

    private CommandInput(String word, String arguments) {
        this.word = word;
        this.arguments = arguments;
    }

    /**
     * Splits {@code input} after trimming it, or returns an empty {@code Optional} if it holds no word.
     */
    static Optional<CommandInput> split(String input) {
        requireNonNull(input);
        Matcher matcher = WORD_AND_ARGUMENTS.matcher(input.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new CommandInput(matcher.group("word"), matcher.group("arguments")));
    }

    String getWord() {
        return word;
    }

    /** Returns everything after the first word, including the whitespace that separates them. */
    String getArguments() {
        return arguments;
    }
}
