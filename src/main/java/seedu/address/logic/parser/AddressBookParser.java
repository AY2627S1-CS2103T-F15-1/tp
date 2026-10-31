package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.BalanceCommand;
import seedu.address.logic.commands.ChargeCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DuesCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PaymentCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.SaveCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.commands.TravelCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.WithdrawCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    public static final String MESSAGE_TAKES_NO_PARAMETERS = "The '%1$s' command takes no parameters.";
    public static final String MESSAGE_NOT_PRINTABLE = "Commands must contain printable text on one line.";
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);
    private static final Pattern RETIRED_ADDRESS_TOKEN = Pattern.compile("(?<=\\s)a/");
    private static final Set<String> NON_PREFIX_COMMAND_PATHS = Set.of(
            "hw delete", "lesson missed", "lesson series", "pay reverse", "task delete", "task done");

    private final Clock clock;

    /** Uses the local system date for interactive commands. */
    public AddressBookParser() {
        this(Clock.systemDefaultZone());
    }

    /** Shares one injectable date source across finance and lesson commands. */
    public AddressBookParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        ParserUtil.requirePrintable(userInput, MESSAGE_NOT_PRINTABLE);
        final Optional<CommandInput> input = CommandInput.split(userInput);
        if (input.isEmpty()) {
            throw ParserUtil.invalidFormat(HelpCommand.MESSAGE_USAGE);
        }

        final String commandWord = input.get().getWord();
        final String arguments = input.get().getArguments();
        if (NON_PREFIX_COMMAND_PATHS.contains(commandPath(commandWord, arguments))
                && RETIRED_ADDRESS_TOKEN.matcher(arguments).find()) {
            throw new ParseException(CliSyntax.MESSAGE_RETIRED_ADDRESS_PREFIX);
        }

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Parsing command");

        return switch (commandWord) {
            case BalanceCommand.COMMAND_WORD -> new BalanceCommandParser().parse(arguments);
            case ChargeCommand.COMMAND_WORD -> new ChargeCommandParser(clock).parse(arguments);
            case PaymentCommand.COMMAND_WORD -> new PayCommandParser(clock).parse(arguments);
            case WithdrawCommand.COMMAND_WORD -> new WithdrawCommandParser(clock).parse(arguments);
            case DuesCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new DuesCommand(clock);
            }
            case SaveCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new SaveCommand();
            }
            case DemoCommandParser.COMMAND_WORD -> new DemoCommandParser().parse(arguments);
            case HomeworkCommandParser.COMMAND_WORD -> new HomeworkCommandParser(clock).parse(arguments);
            case StudentCommandParser.COMMAND_WORD -> new StudentCommandParser().parse(arguments);
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
            case AgendaCommand.COMMAND_WORD -> new AgendaCommandParser(clock).parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case TodayCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new TodayCommand(LocalDate.now(clock));
            }
            case LessonCommandParser.COMMAND_WORD -> new LessonCommandParser(clock).parse(arguments);
            case TaskCommandParser.COMMAND_WORD -> new TaskCommandParser(clock).parse(arguments);
            case TravelCommand.COMMAND_WORD -> new TravelCommandParser().parse(arguments);
            case UndoCommand.COMMAND_WORD -> {
                requireNoArguments(commandWord, arguments);
                yield new UndoCommand();
            }
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

    /**
     * Throws a {@code ParseException} if the user typed anything after {@code commandWord}, so that a command that
     * takes no parameters is not run when the user meant something else, such as {@code exit now}.
     */
    private static void requireNoArguments(String commandWord, String arguments) throws ParseException {
        if (!arguments.isBlank()) {
            throw new ParseException(String.format(MESSAGE_TAKES_NO_PARAMETERS, commandWord));
        }
    }

    /** Returns the command and its first sub-command, if any, for matching a non-prefix command path. */
    private static String commandPath(String commandWord, String arguments) {
        return CommandInput.split(arguments)
                .map(input -> commandWord + " " + input.getWord())
                .orElse(commandWord);
    }

}
