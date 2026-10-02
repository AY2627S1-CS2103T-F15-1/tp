package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of the lesson commands, which are written as "lesson" and the name of the lesson command.
 */
public class LessonCommandParser implements Parser<Command> {

    public static final String COMMAND_WORD = "lesson";

    private static final Pattern SUB_COMMAND_FORMAT = Pattern.compile("(?<subCommandWord>\\S+)(?<arguments>.*)");

    /**
     * Parses the given {@code String} of arguments, which start with the name of a lesson command, and returns the
     * command for execution.
     * @throws ParseException if the lesson command is missing, is unknown or its arguments are invalid
     */
    @Override
    public Command parse(String args) throws ParseException {
        Matcher matcher = SUB_COMMAND_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, getUsage()));
        }

        String arguments = matcher.group("arguments");
        return switch (matcher.group("subCommandWord")) {
            case "add" -> new LessonAddCommandParser().parse(arguments);
            case "list" -> new LessonListCommandParser().parse(arguments);
            default -> throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        };
    }

    private static String getUsage() {
        return "Lesson commands: " + COMMAND_WORD + " add, " + COMMAND_WORD + " list";
    }
}
