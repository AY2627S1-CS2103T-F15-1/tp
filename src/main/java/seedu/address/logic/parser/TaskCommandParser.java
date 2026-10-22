package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.time.Clock;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of the task commands, which are written as "task" and the name of the task command.
 */
public class TaskCommandParser implements Parser<Command> {

    public static final String COMMAND_WORD = "task";

    private static final Pattern SUB_COMMAND_FORMAT = Pattern.compile("(?<subCommandWord>\\S+)(?<arguments>.*)");

    private final Clock clock;

    /** Creates a parser that reads today's date from the system clock. */
    public TaskCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a parser that reads today's date, for due dates, from {@code clock}. */
    public TaskCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses the given {@code String} of arguments, which start with the name of a task command, and returns the
     * command for execution.
     * @throws ParseException if the task command is missing, is unknown or its arguments are invalid
     */
    @Override
    public Command parse(String args) throws ParseException {
        Matcher matcher = SUB_COMMAND_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, getUsage()));
        }

        String arguments = matcher.group("arguments");
        return switch (matcher.group("subCommandWord")) {
            case "add" -> new TaskAddCommandParser(clock).parse(arguments);
            case "list" -> new TaskListCommandParser().parse(arguments);
            case "edit" -> new TaskEditCommandParser(clock).parse(arguments);
            case "done" -> new TaskDoneCommandParser().parse(arguments);
            case "delete" -> new TaskDeleteCommandParser().parse(arguments);
            default -> throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
        };
    }

    private static String getUsage() {
        return "Task commands: " + COMMAND_WORD + " add, " + COMMAND_WORD + " list, " + COMMAND_WORD + " edit, "
                + COMMAND_WORD + " done, " + COMMAND_WORD + " delete";
    }
}
