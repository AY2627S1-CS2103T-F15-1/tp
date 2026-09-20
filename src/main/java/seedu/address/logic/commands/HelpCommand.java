package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Formats full help instructions for every command for display.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.\n"
            + "TutorFlow Commands:\n"
            + "1. student add n/NAME p/PHONE l/LEVEL s/SUBJECT... r/RATE [e/EMAIL] [v/VENUE]\n"
            + "2. student list [l/LEVEL] [s/SUBJECT]\n"
            + "3. student find KEYWORD...\n"
            + "4. student edit INDEX [n/NAME] [p/PHONE] [l/LEVEL] [s/SUBJECT]... [r/RATE] [e/EMAIL] [v/VENUE]\n"
            + "5. student delete INDEX [confirm]\n"
            + "6. student view INDEX\n"
            + "7. lesson add st/STUDENT_INDEX s/SUBJECT d/DATE t/TIME dur/DURATION [v/VENUE]\n"
            + "8. lesson list st/STUDENT_INDEX [all/]\n"
            + "9. lesson move INDEX [d/DATE] [t/TIME] [dur/DURATION] [v/VENUE]\n"
            + "10. lesson cancel INDEX [r/REASON]\n"
            + "11. lesson done INDEX [n/NOTES]\n"
            + "12. lesson missed INDEX\n"
            + "13. today\n"
            + "14. agenda [d/DATE] [week/]\n"
            + "15. clear [confirm]\n"
            + "16. exit\n"
            + "17. help";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
