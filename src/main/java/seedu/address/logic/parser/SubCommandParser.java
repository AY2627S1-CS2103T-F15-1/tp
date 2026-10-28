package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a command that is a family of sub-commands, such as {@code lesson add} or {@code task done}. It reads the
 * sub-command word and hands the remaining arguments to the {@code Parser} registered for that word.
 */
public class SubCommandParser implements Parser<Command> {

    private final Map<String, Parser<? extends Command>> parsers;
    private final String usage;

    /**
     * Creates a parser that dispatches on the keys of {@code parsers}.
     *
     * @param usage the message shown when no sub-command is given.
     */
    public SubCommandParser(Map<String, Parser<? extends Command>> parsers, String usage) {
        requireNonNull(parsers);
        requireNonNull(usage);
        this.parsers = new LinkedHashMap<>(parsers);
        this.usage = usage;
    }

    @Override
    public Command parse(String args) throws ParseException {
        Optional<CommandInput> input = CommandInput.split(args);
        if (input.isEmpty()) {
            throw ParserUtil.invalidFormat(usage);
        }
        Parser<? extends Command> parser = parsers.get(input.get().getWord());
        if (parser == null) {
            throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        }
        return parser.parse(input.get().getArguments());
    }
}
