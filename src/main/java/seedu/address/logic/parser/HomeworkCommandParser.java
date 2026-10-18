package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HomeworkAddCommand;
import seedu.address.logic.commands.HomeworkEditCommand;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.homework.Homework;

/** Parses homework operations with an injectable local calendar clock. */
public class HomeworkCommandParser implements Parser<Command> {
    private static final Prefix DESCRIPTION = new Prefix("desc/");
    private static final Prefix DUE = new Prefix("due/");
    private static final Prefix STUDENT = new Prefix("st/");
    private static final Prefix OVERDUE = new Prefix("overdue/");
    private static final Prefix OUTSTANDING = new Prefix("outstanding/");

    private final Clock clock;

    /** Uses the application's local time zone. */
    public HomeworkCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a supplied clock for deterministic relative dates and due-date bounds. */
    public HomeworkCommandParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Command parse(String args) throws ParseException {
        String[] parts = args.strip().split("\\s+", 2);
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (parts[0]) {
            case "add" -> parseAdd(" " + arguments);
            case "edit" -> parseEdit(" " + arguments);
            case "list" -> parseList(" " + arguments);
            default -> throw new ParseException("Homework commands: hw add, hw edit, hw list");
        };
    }

    /** Parses required assignment fields without treating prose as unrelated command prefixes. */
    private Command parseAdd(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STUDENT, DESCRIPTION, DUE);
        values.verifyNoDuplicatePrefixesFor(STUDENT, DESCRIPTION, DUE);
        values.verifyNoEmptyValuesFor(STUDENT, DESCRIPTION, DUE);
        ParserUtil.requirePrefixesPresent(values, HomeworkAddCommand.MESSAGE_USAGE, STUDENT, DESCRIPTION, DUE);
        return new HomeworkAddCommand(ParserUtil.parseIndex(values.getValue(STUDENT).orElseThrow()),
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
        return new HomeworkEditCommand(ParserUtil.parseIndex(values.getPreamble()),
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
        return switch (text.strip().toLowerCase(java.util.Locale.ENGLISH)) {
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
            throw new ParseException(HomeworkListCommand.MESSAGE_USAGE);
        }
        boolean overdue = ParserUtil.parseFlag(values, OVERDUE);
        boolean outstanding = ParserUtil.parseFlag(values, OUTSTANDING);
        if (overdue && outstanding) {
            throw new ParseException("Choose overdue/ or outstanding/, not both.");
        }
        Index student = ParserUtil.parseOptional(values, STUDENT, ParserUtil::parseIndex);
        return new HomeworkListCommand(student, overdue, outstanding, LocalDate.now(clock));
    }
}
