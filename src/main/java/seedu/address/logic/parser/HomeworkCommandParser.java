package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DUE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FEEDBACK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OUTSTANDING;
import static seedu.address.logic.parser.CliSyntax.PREFIX_OVERDUE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

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
    public static final String COMMAND_WORD = "hw";
    public static final String MESSAGE_SUBCOMMANDS =
            "Homework commands: hw add, hw edit, hw list, hw status, hw feedback, hw delete";
    public static final String MESSAGE_NOT_PRINTABLE = AddressBookParser.MESSAGE_NOT_PRINTABLE;

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
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_STATUS);
        values.verifyNoDuplicatePrefixesFor(PREFIX_STATUS);
        values.verifyNoEmptyValuesFor(PREFIX_STATUS);
        ParserUtil.requirePrefixesFound(values, HomeworkStatusCommand.MESSAGE_USAGE, PREFIX_STATUS);
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkStatusCommand.MESSAGE_USAGE);
        HomeworkStatus status = HomeworkStatus.fromText(values.getValue(PREFIX_STATUS).orElseThrow())
                .orElseThrow(() -> new ParseException(HomeworkStatus.MESSAGE_CONSTRAINTS));
        return new HomeworkStatusCommand(index, status);
    }

    /** Parses feedback using the shared printable one-line notes boundary. */
    private Command parseFeedback(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_FEEDBACK);
        values.verifyNoDuplicatePrefixesFor(PREFIX_FEEDBACK);
        values.verifyNoEmptyValuesFor(PREFIX_FEEDBACK);
        ParserUtil.requirePrefixesFound(values, HomeworkFeedbackCommand.MESSAGE_USAGE, PREFIX_FEEDBACK);
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkFeedbackCommand.MESSAGE_USAGE);
        return new HomeworkFeedbackCommand(index,
                ParserUtil.parseNotes(values.getValue(PREFIX_FEEDBACK).orElseThrow()));
    }

    /** Parses exactly an index and optional confirmation word. */
    private Command parseDelete(String args) throws ParseException {
        ConfirmableIndex target = ConfirmableIndex.parse(args, CONFIRM, HomeworkDeleteCommand.MESSAGE_USAGE);
        return new HomeworkDeleteCommand(target.getIndex(), target.isConfirmed());
    }

    /** Parses required assignment fields without treating prose as unrelated command prefixes. */
    private Command parseAdd(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_DESCRIPTION, PREFIX_DUE);
        values.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_DESCRIPTION, PREFIX_DUE);
        values.verifyNoEmptyValuesFor(PREFIX_STUDENT, PREFIX_DESCRIPTION, PREFIX_DUE);
        ParserUtil.requirePrefixesPresent(values, HomeworkAddCommand.MESSAGE_USAGE, PREFIX_STUDENT,
                PREFIX_DESCRIPTION, PREFIX_DUE);
        return new HomeworkAddCommand(
                ParserUtil.parseIndex(values.getValue(PREFIX_STUDENT).orElseThrow(), HomeworkAddCommand.MESSAGE_USAGE),
                parseDescription(values.getValue(PREFIX_DESCRIPTION).orElseThrow()),
                parseDate(values.getValue(PREFIX_DUE).orElseThrow()), LocalDate.now(clock));
    }

    /** Parses optional corrections, requiring at least one changed field. */
    private Command parseEdit(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_DESCRIPTION, PREFIX_DUE);
        values.verifyNoDuplicatePrefixesFor(PREFIX_DESCRIPTION, PREFIX_DUE);
        values.verifyNoEmptyValuesFor(PREFIX_DESCRIPTION, PREFIX_DUE);
        if (values.getValue(PREFIX_DESCRIPTION).isEmpty() && values.getValue(PREFIX_DUE).isEmpty()) {
            throw new ParseException(HomeworkEditCommand.MESSAGE_USAGE);
        }
        Index index = ParserUtil.parseIndex(values.getPreamble(), HomeworkEditCommand.MESSAGE_USAGE);
        return new HomeworkEditCommand(index,
                ParserUtil.parseOptional(values, PREFIX_DESCRIPTION, this::parseDescription),
                ParserUtil.parseOptional(values, PREFIX_DUE, this::parseDate), LocalDate.now(clock));
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
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, PREFIX_STUDENT, PREFIX_OVERDUE, PREFIX_OUTSTANDING);
        values.verifyNoDuplicatePrefixesFor(PREFIX_STUDENT, PREFIX_OVERDUE, PREFIX_OUTSTANDING);
        values.verifyNoEmptyValuesFor(PREFIX_STUDENT);
        if (!values.getPreamble().isEmpty()) {
            throw ParserUtil.invalidFormat(HomeworkListCommand.MESSAGE_USAGE);
        }
        boolean isOverdueOnly = parseListFlag(values, PREFIX_OVERDUE);
        boolean isOutstandingOnly = parseListFlag(values, PREFIX_OUTSTANDING);
        if (isOverdueOnly && isOutstandingOnly) {
            throw new ParseException(HomeworkListCommand.MESSAGE_FLAGS_EXCLUSIVE);
        }
        Index student = ParserUtil.parseOptional(values, PREFIX_STUDENT,
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
