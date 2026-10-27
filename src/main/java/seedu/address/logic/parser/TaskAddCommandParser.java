package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HOMEWORK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LESSON;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PAYMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.TaskAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.task.Task;

/**
 * Parses input arguments and creates a new TaskAddCommand object
 */
public class TaskAddCommandParser implements Parser<TaskAddCommand> {

    public static final String MESSAGE_PAST_DUE = "Due dates must be today or later.";

    private final Clock clock;

    /** Creates a parser that reads today's date from the system clock. */
    public TaskAddCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Creates a parser that reads today's date, for due dates, from {@code clock}. */
    public TaskAddCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses the given {@code String} of arguments in the context of the TaskAddCommand
     * and returns a TaskAddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TaskAddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_LESSON,
                PREFIX_HOMEWORK, PREFIX_PAYMENT, PREFIX_DESCRIPTION, PREFIX_DUE);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    TaskAddCommand.MESSAGE_USAGE));
        }
        long linkCount = Stream.of(PREFIX_STUDENT, PREFIX_LESSON, PREFIX_HOMEWORK, PREFIX_PAYMENT)
                .filter(prefix -> argMultimap.getValue(prefix).isPresent()).count();
        if (linkCount != 1) {
            throw new ParseException(TaskAddCommand.MESSAGE_USAGE);
        }
        ParserUtil.requirePrefixesPresent(argMultimap, TaskAddCommand.MESSAGE_USAGE, PREFIX_DESCRIPTION);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_LESSON, PREFIX_HOMEWORK, PREFIX_PAYMENT,
                PREFIX_DESCRIPTION, PREFIX_DUE);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_LESSON, PREFIX_HOMEWORK, PREFIX_PAYMENT,
                PREFIX_DESCRIPTION, PREFIX_DUE);

        ParserUtil.ValueParser<Index> linkIndex = value -> ParserUtil.parseIndex(value, TaskAddCommand.MESSAGE_USAGE);
        Index studentIndex = ParserUtil.parseOptional(argMultimap, PREFIX_STUDENT, linkIndex);
        Index lessonIndex = ParserUtil.parseOptional(argMultimap, PREFIX_LESSON, linkIndex);
        Index homeworkIndex = ParserUtil.parseOptional(argMultimap, PREFIX_HOMEWORK, linkIndex);
        Index paymentIndex = ParserUtil.parseOptional(argMultimap, PREFIX_PAYMENT, linkIndex);
        String description = parseDescription(argMultimap.getValue(PREFIX_DESCRIPTION).get());
        LocalDate due = argMultimap.getValue(PREFIX_DUE).isPresent()
                ? parseDue(argMultimap.getValue(PREFIX_DUE).get(), LocalDate.now(clock)) : null;

        return new TaskAddCommand(studentIndex, lessonIndex, homeworkIndex, paymentIndex, description, due);
    }

    /**
     * Parses a due date, which may be written as {@code today} or {@code tmr}, and checks that it is not before
     * {@code today}.
     *
     * @throws ParseException if the date is invalid or in the past.
     */
    static LocalDate parseDue(String value, LocalDate today) throws ParseException {
        LocalDate due = ParserUtil.parseDate(value, today).value;
        if (due.isBefore(today)) {
            throw new ParseException(MESSAGE_PAST_DUE);
        }
        return due;
    }

    /**
     * Parses the description of a task, trimming the ends and collapsing repeated spaces.
     *
     * @throws ParseException if the description is too long or contains non-printable characters.
     */
    static String parseDescription(String description) throws ParseException {
        String stripped = description.strip().replaceAll(" {2,}", " ");
        if (!Task.isValidDescription(stripped)) {
            throw new ParseException(Task.MESSAGE_DESCRIPTION_CONSTRAINTS);
        }
        return stripped;
    }
}
