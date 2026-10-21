package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.SaveCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    public static final String MESSAGE_TAKES_NO_PARAMETERS = "The '%1$s' command takes no parameters.";

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        validatePrintableInput(userInput);
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Parsing command");

        return switch (commandWord) {
            case SaveCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new SaveCommand();
            }
            case "demo" -> {
                if (!arguments.isBlank() && !arguments.strip().equals("confirm")) {
                    throw new ParseException("Usage: demo [confirm]");
                }
                yield new DemoCommand(arguments.strip().equals("confirm"));
            }
            case "hw" -> new HomeworkCommandParser().parse(arguments);
            case "student" -> new StudentCommandParser().parse(arguments);
            case ArchiveCommand.COMMAND_WORD_ARCHIVE -> new ArchiveCommandParser(true).parse(arguments);
            case ArchiveCommand.COMMAND_WORD_RESTORE -> new ArchiveCommandParser(false).parse(arguments);
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommandParser().parse(arguments);
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case RemarkCommand.COMMAND_WORD -> new RemarkCommandParser().parse(arguments);
            case SortCommand.COMMAND_WORD -> new SortCommandParser().parse(arguments);
            case AgendaCommand.COMMAND_WORD -> new AgendaCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case TodayCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new TodayCommand(LocalDate.now());
            }
            case LessonCommandParser.COMMAND_WORD -> new LessonCommandParser().parse(arguments);
            case UndoCommand.COMMAND_WORD -> new UndoCommand();
            case ExitCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new ExitCommand();
            }
            case HelpCommand.COMMAND_WORD -> new HelpCommandParser().parse(arguments);
            default -> {
                logger.finer("Command rejected: unknown command");
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

    /** Rejects control characters before trimming can conceal invalid values. */
    private static void validatePrintableInput(String input) throws ParseException {
        if (input.codePoints().anyMatch(character -> Character.isISOControl(character)
                || character == '\u2028' || character == '\u2029')) {
            throw new ParseException("Commands must contain printable text on one line.");
        }
    }

    /**
     * Throws a {@code ParseException} if the user typed anything after {@code commandWord}, so that a command that
     * takes no parameters is not run when the user meant something else, such as {@code exit now}.
     */
    private static void requireNoArguments(String commandWord, String arguments) throws ParseException {
        if (!arguments.isBlank()) {
            throw new ParseException(String.format(MESSAGE_TAKES_NO_PARAMETERS, commandWord));
        }
    }

}
