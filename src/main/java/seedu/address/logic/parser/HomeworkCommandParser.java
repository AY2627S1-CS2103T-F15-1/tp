package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkDeleteCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkFeedbackCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.commands.HomeworkStatusCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;

/** Parses homework operations with an injectable local calendar clock. */
public class HomeworkCommandParser implements Parser<Command> {
    public static final String MESSAGE_SUBCOMMANDS =
            "Homework commands: hw add, hw edit, hw list, hw status, hw feedback, hw delete";
    public static final String MESSAGE_NOT_PRINTABLE = "Commands must contain printable text on one line.";

    private static final Prefix STATUS = new Prefix("s/");
    private static final Prefix FEEDBACK = new Prefix("f/");
    private static final Prefix DESCRIPTION = new Prefix("desc/");
    private static final Prefix DUE = new Prefix("due/");
    private static final Prefix STUDENT = new Prefix("st/");
    private static final Prefix OVERDUE = new Prefix("overdue/");
    private static final Prefix OUTSTANDING = new Prefix("outstanding/");
    private static final String CONFIRM = "confirm";

    private final Clock clock;

    /** Uses the application's local time zone. */
    public HomeworkCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a supplied clock for deterministic relative dates and due-date bounds. */
    public HomeworkCommandParser(Clock clock) {
        requireNonNull(clock);
        this.clock = clock;
    }

    @Override
    public Command parse(String args) throws ParseException {
        requireNonNull(args);
        ParserUtil.requirePrintable(args, MESSAGE_NOT_PRINTABLE);
        String[] parts = args.strip().split("\\s+", 2);
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (parts[0]) {
            case "add" -> parseAdd(" " + arguments);
            case "edit" -> parseEdit(" " + arguments);
            case "list" -> parseList(" " + arguments);
            case "status" -> parseStatus(" " + arguments);
            case "feedback" -> parseFeedback(" " + arguments);
            case "delete" -> parseDelete(arguments);
            default -> throw new ParseException(MESSAGE_SUBCOMMANDS);
        };
    }

    /** Parses a persisted lifecycle status; overdue is deliberately excluded. */
    private Command parseStatus(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STATUS);
        values.verifyNoDuplicatePrefixesFor(STATUS);
        values.verifyNoEmptyValuesFor(STATUS);
        ParserUtil.requirePrefixesFound(values, HomeworkStatusCommand.MESSAGE_USAGE, STATUS);
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkStatusCommand.MESSAGE_USAGE);
        HomeworkStatus status = HomeworkStatus.fromText(values.getValue(STATUS).orElseThrow())
                .orElseThrow(() -> new ParseException(HomeworkStatus.MESSAGE_CONSTRAINTS));
        return new HomeworkStatusCommand(index, status);
    }

    /** Parses feedback using the shared printable one-line notes boundary. */
    private Command parseFeedback(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, FEEDBACK);
        values.verifyNoDuplicatePrefixesFor(FEEDBACK);
        values.verifyNoEmptyValuesFor(FEEDBACK);
        ParserUtil.requirePrefixesFound(values, HomeworkFeedbackCommand.MESSAGE_USAGE, FEEDBACK);
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkFeedbackCommand.MESSAGE_USAGE);
        return new HomeworkFeedbackCommand(index, ParserUtil.parseNotes(values.getValue(FEEDBACK).orElseThrow()));
    }

    /** Parses exactly an index and optional confirmation word. */
    private Command parseDelete(String args) throws ParseException {
        String[] words = args.strip().split("\\s+");
        boolean isConfirmed = words.length == 2 && words[1].equals(CONFIRM);
        if (words.length > 2 || words.length == 2 && !isConfirmed) {
            throw ParserUtil.invalidFormat(HomeworkDeleteCommand.MESSAGE_USAGE);
        }
        return new HomeworkDeleteCommand(ParserUtil.parseIndex(words[0], HomeworkDeleteCommand.MESSAGE_USAGE),
                isConfirmed);
    }

    /** Parses required assignment fields without treating prose as unrelated command prefixes. */
    private Command parseAdd(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STUDENT, DESCRIPTION, DUE);
        values.verifyNoDuplicatePrefixesFor(STUDENT, DESCRIPTION, DUE);
        values.verifyNoEmptyValuesFor(STUDENT, DESCRIPTION, DUE);
        ParserUtil.requirePrefixesPresent(values, HomeworkAddCommand.MESSAGE_USAGE, STUDENT, DESCRIPTION, DUE);
        return new HomeworkAddCommand(
                ParserUtil.parseIndex(values.getValue(STUDENT).orElseThrow(), HomeworkAddCommand.MESSAGE_USAGE),
                parseDescription(values.getValue(DESCRIPTION).orElseThrow()),
                parseDate(values.getValue(DUE).orElseThrow()), LocalDate.now(clock));
    }

    /** Parses optional corrections, requiring at least one changed field. */
    private Command parseEdit(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, DESCRIPTION, DUE);
        values.verifyNoDuplicatePrefixesFor(DESCRIPTION, DUE);
        values.verifyNoEmptyValuesFor(DESCRIPTION, DUE);
        if (values.getValue(DESCRIPTION).isEmpty() && values.getValue(DUE).isEmpty()) {
            throw new ParseException(HomeworkEditCommand.MESSAGE_USAGE);
        }
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkEditCommand.MESSAGE_USAGE);
        return new HomeworkEditCommand(index,
                ParserUtil.parseOptional(values, DESCRIPTION, this::parseDescription),
                ParserUtil.parseOptional(values, DUE, this::parseDate), LocalDate.now(clock));
    }

    /** Validates description text before any whitespace can conceal control characters. */
    private String parseDescription(String text) throws ParseException {
        try {
            return Homework.normalizeDescription(text);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /** Resolves relative dates against the injected clock and delegates explicit date formats. */
    private LocalDate parseDate(String text) throws ParseException {
        return switch (text.strip().toLowerCase(Locale.ENGLISH)) {
            case "today" -> LocalDate.now(clock);
            case "tmr" -> LocalDate.now(clock).plusDays(1);
            default -> ParserUtil.parseDate(text).value;
        };
    }

    /** Parses only the supported value prefix and two mutually exclusive flags. */
    private Command parseList(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STUDENT, OVERDUE, OUTSTANDING);
        values.verifyNoDuplicatePrefixesFor(STUDENT, OVERDUE, OUTSTANDING);
        values.verifyNoEmptyValuesFor(STUDENT);
        if (!values.getPreamble().isEmpty()) {
            throw ParserUtil.invalidFormat(HomeworkListCommand.MESSAGE_USAGE);
        }
        boolean isOverdueOnly = parseListFlag(values, OVERDUE);
        boolean isOutstandingOnly = parseListFlag(values, OUTSTANDING);
        if (isOverdueOnly && isOutstandingOnly) {
            throw new ParseException(HomeworkListCommand.MESSAGE_FLAGS_EXCLUSIVE);
        }
        Index student = ParserUtil.parseOptional(values, STUDENT,
                value -> ParserUtil.parseIndex(value, HomeworkListCommand.MESSAGE_USAGE));
        return new HomeworkListCommand(student, isOverdueOnly, isOutstandingOnly, LocalDate.now(clock));
    }

    /** Reads a flag of the list command, answering a value given to it with the usage string. */
    private static boolean parseListFlag(ArgumentMultimap values, Prefix flag) throws ParseException {
        try {
            return ParserUtil.parseFlag(values, flag);
        } catch (ParseException e) {
            throw new ParseException(HomeworkListCommand.MESSAGE_USAGE, e);
        }
    }
}
