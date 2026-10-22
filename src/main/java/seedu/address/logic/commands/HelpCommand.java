package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Shows every command with a one-line summary, or the usage of one command.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";
    public static final String MESSAGE_USER_GUIDE =
            "Read the full User Guide from the Help menu or F1 (works offline).";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows every command with a one-line summary, "
            + "or how to use one command.\n"
            + "Parameters: [COMMAND]\n"
            + "Examples: " + COMMAND_WORD + ", " + COMMAND_WORD + " lesson add, " + COMMAND_WORD + " lesson";

    public static final String MESSAGE_ALL_COMMANDS = MESSAGE_USER_GUIDE
            + "\nCommands (type 'help COMMAND' to see how to use one):";
    public static final String MESSAGE_COMMANDS_STARTING_WITH = "Commands that start with '%1$s' (type 'help COMMAND' "
            + "to see how to use one):\n" + MESSAGE_USER_GUIDE;
    public static final String MESSAGE_NO_HELP = "No help available for '%1$s'. Type 'help' for the full list.";

    /** Every command, in the order that it is listed. */
    private static final List<CommandHelp> COMMANDS = List.of(
            new CommandHelp("add", "Adds a student", AddCommand.MESSAGE_USAGE),
            new CommandHelp("edit", "Edits a student's details", EditCommand.MESSAGE_USAGE),
            new CommandHelp("delete", "Deletes a student and linked history", DeleteCommand.MESSAGE_USAGE),
            new CommandHelp("list", "Filters active, archived, or all students", ListCommand.MESSAGE_USAGE),
            new CommandHelp("student", "Groups student commands",
                    "student add|edit|delete|find|view|list|archive|restore (same arguments as top-level commands)",
                    "Example: student find wei"),
            new CommandHelp("archive", "Archives a student while retaining history",
                    ArchiveCommand.MESSAGE_USAGE_ARCHIVE,
                    "Parameters: INDEX of a student in the displayed list. The student leaves the active list; "
                            + "lessons and homework are kept.\nExample: archive 3"),
            new CommandHelp("restore", "Restores an archived student", ArchiveCommand.MESSAGE_USAGE_RESTORE,
                    "Parameters: INDEX of a student in the archived list, so type 'list archived/' first.\n"
                            + "Example: restore 1"),
            new CommandHelp("demo", "Previews sample data for an empty dataset", "demo [confirm]",
                    "Parameters: confirm loads the sample students; without it, demo only says what it would do. "
                            + "It works only on an empty dataset.\nExamples: demo, demo confirm"),
            new CommandHelp("find", "Finds students by name", FindCommand.MESSAGE_USAGE),
            new CommandHelp("view", "Shows a student's overview", ViewCommand.MESSAGE_USAGE),
            new CommandHelp("balance", "Shows a student's outstanding balance or credit", BalanceCommand.MESSAGE_USAGE),
            new CommandHelp("pay", "Records a student's payment with optional date and notes",
                    PaymentCommand.MESSAGE_USAGE),
            new CommandHelp("payment", "Alias for pay using the earlier a/ amount prefix",
                    PaymentCommand.MESSAGE_USAGE),
            new CommandHelp("dues", "Lists all students who owe money", DuesCommand.MESSAGE_USAGE),
            new CommandHelp("sort", "Sorts students by name", SortCommand.MESSAGE_USAGE),
            new CommandHelp("remark", "Sets a student's remark", RemarkCommand.MESSAGE_USAGE),
            new CommandHelp("lesson add", "Schedules a lesson", LessonAddCommand.MESSAGE_USAGE),
            new CommandHelp("lesson repeat", "Schedules a weekly series of lessons", LessonRepeatCommand.MESSAGE_USAGE),
            new CommandHelp("lesson list", "Lists a student's lessons", LessonListCommand.MESSAGE_USAGE),
            new CommandHelp("lesson prep", "Records preparation notes", LessonPrepCommand.MESSAGE_USAGE,
                    "Parameters: INDEX of a lesson in the lesson list shown; NOTES is 1-1000 characters on one line. "
                            + "Preparation is kept apart from the notes written after the lesson.\n"
                            + "Example: lesson prep 1 n/Bring the mock paper"),
            new CommandHelp("lesson move", "Reschedules a lesson", LessonMoveCommand.MESSAGE_USAGE),
            new CommandHelp("lesson cancel", "Cancels a lesson", LessonCancelCommand.MESSAGE_USAGE),
            new CommandHelp("lesson done", "Completes a lesson and records notes", LessonDoneCommand.MESSAGE_USAGE),
            new CommandHelp("lesson missed", "Marks a lesson as missed (no-show)", LessonMissedCommand.MESSAGE_USAGE),
            new CommandHelp("hw add", "Assigns homework", HomeworkAddCommand.MESSAGE_USAGE,
                    "Parameters: STUDENT_INDEX of an active student; DESCRIPTION is 1-200 characters; DATE is today "
                            + "or later, up to a year ahead.\nExample: hw add st/1 desc/Algebra Q1-10 due/tmr"),
            new CommandHelp("hw list", "Lists homework and complete student history",
                    HomeworkListCommand.MESSAGE_USAGE,
                    "Parameters: STUDENT_INDEX is optional; overdue/ or outstanding/, but not both, limits the list. "
                            + "The numbers stay fixed until the next hw list.\n"
                            + "Examples: hw list, hw list st/1 overdue/"),
            new CommandHelp("hw edit", "Corrects description or due date", HomeworkEditCommand.MESSAGE_USAGE,
                    "Parameters: INDEX from the last hw list, and at least one of desc/ and due/. Status and "
                            + "feedback are kept.\nExample: hw edit 1 due/2026-10-21"),
            new CommandHelp("hw status", "Updates or corrects homework status", HomeworkStatusCommand.MESSAGE_USAGE,
                    "Parameters: INDEX from the last hw list; STATUS is assigned, submitted or reviewed. Overdue is "
                            + "worked out from the due date and cannot be set.\n"
                            + "Example: hw status 1 s/submitted"),
            new CommandHelp("hw feedback", "Records feedback after submission", HomeworkFeedbackCommand.MESSAGE_USAGE,
                    "Parameters: INDEX from the last hw list; FEEDBACK is 1-1000 characters. The homework must be "
                            + "submitted or reviewed, and new feedback replaces the old.\n"
                            + "Example: hw feedback 1 f/Sign errors throughout Q8-12"),
            new CommandHelp("hw delete", "Deletes homework with feedback protection",
                    HomeworkDeleteCommand.MESSAGE_USAGE,
                    "Parameters: INDEX from the last hw list; confirm is needed when the homework has feedback.\n"
                            + "Example: hw delete 2 confirm"),
            new CommandHelp("agenda", "Shows the lessons of a day or a week", AgendaCommand.MESSAGE_USAGE),
            new CommandHelp("today", "Shows today's lessons", TodayCommand.MESSAGE_USAGE),
            new CommandHelp("clear", "Deletes all data, after confirmation", ClearCommand.MESSAGE_USAGE),
            new CommandHelp("save", "Retries saving unsaved changes", SaveCommand.MESSAGE_USAGE, "Example: save"),
            new CommandHelp("help", "Shows this list, or how to use one command", MESSAGE_USAGE),
            new CommandHelp("exit", "Exits TutorFlow", ExitCommand.MESSAGE_USAGE));

    private final String commandName; // empty if every command is to be shown

    /**
     * Creates a {@code HelpCommand} that shows every command.
     */
    public HelpCommand() {
        this("");
    }

    /**
     * Creates a {@code HelpCommand} that shows help for {@code commandName}, which is every command if it is empty.
     * Runs of spaces in {@code commandName} are treated as a single space.
     */
    public HelpCommand(String commandName) {
        requireNonNull(commandName);
        this.commandName = commandName.trim().replaceAll("\\s+", " ");
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (commandName.isEmpty()) {
            return new CommandResult(MESSAGE_ALL_COMMANDS + format(COMMANDS));
        }

        String lookup = commandName.startsWith("student ") ? commandName.substring("student ".length()) : commandName;
        Optional<CommandHelp> exactMatch = COMMANDS.stream()
                .filter(command -> command.name.equals(lookup)).findFirst();
        if (exactMatch.isPresent()) {
            return new CommandResult(exactMatch.get().describe() + "\n" + MESSAGE_USER_GUIDE);
        }

        // A word such as "lesson" is not a command on its own, so show the commands that start with it
        List<CommandHelp> matches = COMMANDS.stream()
                .filter(command -> command.name.startsWith(commandName + " ")).collect(Collectors.toList());
        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_HELP, commandName));
        }
        return new CommandResult(String.format(MESSAGE_COMMANDS_STARTING_WITH, commandName) + format(matches));
    }

    private static String format(List<CommandHelp> commands) {
        int width = commands.stream().mapToInt(command -> command.name.length()).max().orElse(0);
        return commands.stream()
                .map(command -> String.format("\n  %-" + width + "s  %s", command.name, command.summary))
                .collect(Collectors.joining());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof HelpCommand otherCommand)) {
            return false;
        }

        return commandName.equals(otherCommand.commandName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("commandName", commandName)
                .toString();
    }

    /**
     * What is shown for one command: its name, a one-line summary and how to use it.
     */
    private record CommandHelp(String name, String summary, String usage, String details) {
        CommandHelp(String name, String summary, String usage) {
            this(name, summary, usage, "");
        }

        /** Returns the usage string, followed by the details if there are any. */
        String describe() {
            return details.isEmpty() ? usage : usage + "\n" + details;
        }
    }
}
