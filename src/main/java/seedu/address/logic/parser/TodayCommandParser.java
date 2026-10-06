package seedu.address.logic.parser;

import java.time.LocalDate;

import seedu.address.logic.commands.TodayCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new TodayCommand object
 */
public class TodayCommandParser implements Parser<TodayCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TodayCommand
     * and returns a TodayCommand object for execution.
     * @throws ParseException if any arguments are given, as the command takes none
     */
    public TodayCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(TodayCommand.MESSAGE_NO_PARAMETERS);
        }
        return new TodayCommand(LocalDate.now());
    }

}
