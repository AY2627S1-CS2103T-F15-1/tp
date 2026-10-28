package seedu.address.logic.parser;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the arguments of the lesson commands, which are written as "lesson" and the name of the lesson command.
 */
public class LessonCommandParser implements Parser<Command> {

    public static final String COMMAND_WORD = "lesson";

    private final SubCommandParser subCommandParser;

    /** Uses the local system date. */
    public LessonCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a shared date source for scheduling and completing lessons. */
    public LessonCommandParser(Clock clock) {
        Objects.requireNonNull(clock);
        Map<String, Parser<? extends Command>> parsers = new LinkedHashMap<>();
        parsers.put("prep", new LessonPrepCommandParser());
        parsers.put("add", new LessonAddCommandParser(clock));
        parsers.put("list", new LessonListCommandParser(clock));
        parsers.put("repeat", new LessonRepeatCommandParser());
        parsers.put("move", new LessonMoveCommandParser());
        parsers.put("cancel", new LessonCancelCommandParser());
        parsers.put("series", new LessonSeriesCancelCommandParser());
        parsers.put("done", new LessonDoneCommandParser(clock));
        parsers.put("missed", new LessonMissedCommandParser());
        subCommandParser = new SubCommandParser(parsers, getUsage());
    }

    /**
     * Parses the given {@code String} of arguments, which start with the name of a lesson command, and returns the
     * command for execution.
     * @throws ParseException if the lesson command is missing, is unknown or its arguments are invalid
     */
    @Override
    public Command parse(String args) throws ParseException {
        return subCommandParser.parse(args);
    }

    private static String getUsage() {
        return "Lesson commands: " + COMMAND_WORD + " add, " + COMMAND_WORD + " repeat, " + COMMAND_WORD + " list, "
                + COMMAND_WORD + " move, " + COMMAND_WORD + " cancel, " + COMMAND_WORD + " series cancel, "
                + COMMAND_WORD + " done, " + COMMAND_WORD + " missed, " + COMMAND_WORD + " prep";
    }
}
