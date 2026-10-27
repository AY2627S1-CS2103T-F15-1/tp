package seedu.address.logic.parser;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of the task commands, which are written as "task" and the name of the task command.
 */
public class TaskCommandParser implements Parser<Command> {

    public static final String COMMAND_WORD = "task";

    private final SubCommandParser subCommandParser;

    /** Creates a parser that reads today's date from the system clock. */
    public TaskCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a parser that reads today's date, for due dates, from {@code clock}. */
    public TaskCommandParser(Clock clock) {
        Objects.requireNonNull(clock);
        Map<String, Parser<? extends Command>> parsers = new LinkedHashMap<>();
        parsers.put("add", new TaskAddCommandParser(clock));
        parsers.put("list", new TaskListCommandParser());
        parsers.put("edit", new TaskEditCommandParser(clock));
        parsers.put("done", new TaskDoneCommandParser());
        parsers.put("delete", new TaskDeleteCommandParser());
        subCommandParser = new SubCommandParser(parsers, getUsage());
    }

    /**
     * Parses the given {@code String} of arguments, which start with the name of a task command, and returns the
     * command for execution.
     * @throws ParseException if the task command is missing, is unknown or its arguments are invalid
     */
    @Override
    public Command parse(String args) throws ParseException {
        return subCommandParser.parse(args);
    }

    private static String getUsage() {
        return "Task commands: " + COMMAND_WORD + " add, " + COMMAND_WORD + " list, " + COMMAND_WORD + " edit, "
                + COMMAND_WORD + " done, " + COMMAND_WORD + " delete";
    }
}
