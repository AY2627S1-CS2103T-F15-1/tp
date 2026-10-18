package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HomeworkListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses homework operations with an injectable local calendar clock. */
public class HomeworkCommandParser implements Parser<Command> {
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
        if (!parts[0].equals("list")) {
            throw new ParseException("Homework commands: hw list");
        }
        return parseList(" " + arguments);
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
