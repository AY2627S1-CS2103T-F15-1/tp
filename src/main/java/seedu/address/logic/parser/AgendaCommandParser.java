package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEEK;

import java.time.LocalDate;

import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new AgendaCommand object
 */
public class AgendaCommandParser implements Parser<AgendaCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AgendaCommand
     * and returns an AgendaCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AgendaCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_WEEK);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AgendaCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DATE);

        LocalDate date = LocalDate.now();
        if (argMultimap.getValue(PREFIX_DATE).isPresent()) {
            date = ParserUtil.parseDate(argMultimap.getValue(PREFIX_DATE).get()).value;
        }
        return new AgendaCommand(date, ParserUtil.parseFlag(argMultimap, PREFIX_WEEK));
    }

}
