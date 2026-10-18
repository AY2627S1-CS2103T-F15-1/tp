package seedu.address.logic.parser;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.TaskAddCommand;
import seedu.address.logic.commands.TaskDeleteCommand;
import seedu.address.logic.commands.TaskDoneCommand;
import seedu.address.logic.commands.TaskEditCommand;
import seedu.address.logic.commands.TaskListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.task.FollowUpTask;

/** Parses linked follow-up commands using an injectable local clock for date aliases. */
public class TaskCommandParser implements Parser<Command> {
    private static final Prefix STUDENT = new Prefix("st/");
    private static final Prefix LESSON = new Prefix("lesson/");
    private static final Prefix HOMEWORK = new Prefix("hw/");
    private static final Prefix PAYMENT = new Prefix("pay/");
    private static final Prefix DESCRIPTION = new Prefix("desc/");
    private static final Prefix DUE = new Prefix("due/");
    private static final Prefix ALL = new Prefix("all/");
    private final Clock clock;

    /** Uses the application's local time zone. */
    public TaskCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses a fixed clock for deterministic date interpretation. */
    public TaskCommandParser(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Command parse(String args) throws ParseException {
        ParserUtil.requirePrintable(args, "Commands must contain printable text on one line.");
        String[] parts = args.strip().split("\\s+", 2);
        String arguments = parts.length == 2 ? parts[1] : "";
        return switch (parts[0]) {
            case "add" -> parseAdd(" " + arguments);
            case "list" -> parseList(" " + arguments);
            case "edit" -> parseEdit(" " + arguments);
            case "done" -> new TaskDoneCommand(ParserUtil.parseIndex(arguments));
            case "delete" -> parseDelete(arguments);
            default -> throw new ParseException(
                    "Task commands: task add, task list, task edit, task done, task delete");
        };
    }

    /** Requires precisely one supported link and a nonempty description. */
    private Command parseAdd(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STUDENT, LESSON, HOMEWORK, PAYMENT,
                DESCRIPTION, DUE);
        values.verifyNoDuplicatePrefixesFor(STUDENT, LESSON, HOMEWORK, PAYMENT, DESCRIPTION, DUE);
        values.verifyNoEmptyValuesFor(STUDENT, LESSON, HOMEWORK, PAYMENT, DESCRIPTION, DUE);
        ParserUtil.requirePrefixesPresent(values, TaskAddCommand.MESSAGE_USAGE, DESCRIPTION);
        List<Prefix> links = List.of(STUDENT, LESSON, HOMEWORK, PAYMENT).stream()
                .filter(prefix -> values.getValue(prefix).isPresent()).toList();
        if (links.size() != 1) {
            throw new ParseException(TaskAddCommand.MESSAGE_USAGE);
        }
        Prefix link = links.getFirst();
        FollowUpTask.LinkType type = link.equals(STUDENT) ? FollowUpTask.LinkType.STUDENT
                : link.equals(LESSON) ? FollowUpTask.LinkType.LESSON
                : link.equals(HOMEWORK) ? FollowUpTask.LinkType.HOMEWORK : FollowUpTask.LinkType.PAYMENT;
        return new TaskAddCommand(type, ParserUtil.parseIndex(values.getValue(link).orElseThrow()),
                description(values.getValue(DESCRIPTION).orElseThrow()),
                ParserUtil.parseOptional(values, DUE, text -> DateParser.parse(text, clock)));
    }

    /** Lists open tasks by default or all history, optionally for one student. */
    private Command parseList(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, STUDENT, ALL);
        values.verifyNoDuplicatePrefixesFor(STUDENT, ALL);
        values.verifyNoEmptyValuesFor(STUDENT);
        if (!values.getPreamble().isBlank()) {
            throw new ParseException(TaskListCommand.MESSAGE_USAGE);
        }
        return new TaskListCommand(ParserUtil.parseOptional(values, STUDENT, ParserUtil::parseIndex),
                ParserUtil.parseFlag(values, ALL), LocalDate.now(clock));
    }

    /** An omitted due date is retained; due/- explicitly clears it. */
    private Command parseEdit(String args) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(args, DESCRIPTION, DUE);
        values.verifyNoDuplicatePrefixesFor(DESCRIPTION, DUE);
        values.verifyNoEmptyValuesFor(DESCRIPTION, DUE);
        if (values.getValue(DESCRIPTION).isEmpty()) {
            throw new ParseException(TaskEditCommand.MESSAGE_USAGE);
        }
        String due = values.getValue(DUE).orElse(null);
        return new TaskEditCommand(ParserUtil.parseIndex(values.getPreamble()),
                description(values.getValue(DESCRIPTION).orElseThrow()),
                due == null || due.equals("-") ? null : DateParser.parse(due, clock), due != null);
    }

    /** Accepts only a shown index and an optional literal confirmation word. */
    private Command parseDelete(String args) throws ParseException {
        String[] words = args.strip().split("\\s+");
        if (words.length > 2 || words.length == 2 && !words[1].equals("confirm")) {
            throw new ParseException(TaskDeleteCommand.MESSAGE_USAGE);
        }
        return new TaskDeleteCommand(ParserUtil.parseIndex(words[0]), words.length == 2);
    }

    /** Converts invalid task prose into a parser error. */
    private static String description(String text) throws ParseException {
        try {
            return FollowUpTask.normalizeDescription(text);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
