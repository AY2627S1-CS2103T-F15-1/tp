package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;

import java.util.Set;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_RATE, PREFIX_VENUE);

        ParserUtil.requirePrefixesPresent(argMultimap, AddCommand.MESSAGE_USAGE, PREFIX_NAME, PREFIX_PHONE,
                PREFIX_LEVEL, PREFIX_SUBJECT, PREFIX_RATE);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL,
                PREFIX_RATE, PREFIX_VENUE);
        argMultimap.verifyNoEmptyValuesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL,
                PREFIX_RATE, PREFIX_VENUE, PREFIX_SUBJECT);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Level level = ParserUtil.parseLevel(argMultimap.getValue(PREFIX_LEVEL).get());
        Set<Subject> subjects = ParserUtil.parseSubjects(argMultimap.getAllValues(PREFIX_SUBJECT));
        Rate rate = ParserUtil.parseRate(argMultimap.getValue(PREFIX_RATE).get());

        // The email and venue are optional, so they stay null when they are not given
        Email email = null;
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        }
        Venue venue = null;
        if (argMultimap.getValue(PREFIX_VENUE).isPresent()) {
            venue = ParserUtil.parseVenue(argMultimap.getValue(PREFIX_VENUE).get());
        }

        Person person = new Person(name, phone, email, level, subjects, rate, venue);

        return new AddCommand(person);
    }

}
