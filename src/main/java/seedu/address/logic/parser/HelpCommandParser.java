package seedu.address.logic.parser;

import seedu.address.logic.commands.HelpCommand;

/**
 * Parses input arguments and creates a new HelpCommand object
 */
public class HelpCommandParser implements Parser<HelpCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the HelpCommand
     * and returns a HelpCommand object for execution. Any text is accepted, because help for an unknown
     * command is reported by the command itself.
     */
    public HelpCommand parse(String args) {
        return new HelpCommand(args);
    }

}
