package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_LEVEL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_RATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_SUBJECT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_VENUE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.RATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.RATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_MATH;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_PHYSICS;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_RATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_MATH;
import static seedu.address.logic.commands.CommandTestUtil.VENUE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VENUE_DESC_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_VENUE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + LEVEL_DESC_BOB + SUBJECT_DESC_PHYSICS + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB,
                new AddCommand(expectedPerson));

        // multiple subjects - all accepted
        Person expectedPersonOneSubject = new PersonBuilder(BOB).withSubjects(VALID_SUBJECT_MATH).build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB, new AddCommand(expectedPersonOneSubject));

        // fields in a different order
        assertParseSuccess(parser, VENUE_DESC_BOB + RATE_DESC_BOB + SUBJECT_DESC_MATH + SUBJECT_DESC_PHYSICS
                + LEVEL_DESC_BOB + EMAIL_DESC_BOB + PHONE_DESC_BOB + NAME_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_repeatedNonSubjectValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_PHYSICS + RATE_DESC_BOB + VENUE_DESC_BOB;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple levels
        assertParseFailure(parser, LEVEL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));

        // multiple rates
        assertParseFailure(parser, RATE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));

        // multiple venues
        assertParseFailure(parser, VENUE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_VENUE));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + VENUE_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL,
                        PREFIX_RATE, PREFIX_VENUE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid level
        assertParseFailure(parser, INVALID_LEVEL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));

        // invalid rate
        assertParseFailure(parser, INVALID_RATE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));

        // invalid venue
        assertParseFailure(parser, INVALID_VENUE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_VENUE));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid level
        assertParseFailure(parser, validExpectedPersonString + INVALID_LEVEL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));

        // invalid rate
        assertParseFailure(parser, validExpectedPersonString + INVALID_RATE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));

        // invalid venue
        assertParseFailure(parser, validExpectedPersonString + INVALID_VENUE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_VENUE));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // no email and no venue
        Person expectedPerson = new PersonBuilder(BOB).withoutEmail().withoutVenue().build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_PHYSICS
                + SUBJECT_DESC_MATH + RATE_DESC_BOB, new AddCommand(expectedPerson));

        // no email
        expectedPerson = new PersonBuilder(BOB).withoutEmail().build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_PHYSICS
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB, new AddCommand(expectedPerson));

        // no venue
        expectedPerson = new PersonBuilder(BOB).withoutVenue().build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_PHYSICS + SUBJECT_DESC_MATH + RATE_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_MATH
                + RATE_DESC_BOB, expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_MATH
                + RATE_DESC_BOB, expectedMessage);

        // missing level prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_LEVEL_BOB + SUBJECT_DESC_MATH
                + RATE_DESC_BOB, expectedMessage);

        // missing subject prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + LEVEL_DESC_BOB + VALID_SUBJECT_MATH
                + RATE_DESC_BOB, expectedMessage);

        // missing rate prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_MATH
                + VALID_RATE_BOB, expectedMessage);

        // optional fields only
        assertParseFailure(parser, EMAIL_DESC_BOB + VENUE_DESC_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_LEVEL_BOB
                + VALID_SUBJECT_MATH + VALID_RATE_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        String validFieldsAfterName = PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_MATH
                + RATE_DESC_BOB + VENUE_DESC_BOB;

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validFieldsAfterName, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB, Email.MESSAGE_CONSTRAINTS);

        // invalid level
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_LEVEL_DESC
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + VENUE_DESC_BOB, Level.MESSAGE_CONSTRAINTS);

        // invalid subject
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + INVALID_SUBJECT_DESC + RATE_DESC_BOB + VENUE_DESC_BOB, Subject.MESSAGE_CONSTRAINTS);

        // one invalid subject among valid subjects
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + INVALID_SUBJECT_DESC + RATE_DESC_BOB + VENUE_DESC_BOB,
                Subject.MESSAGE_CONSTRAINTS);

        // invalid rate
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + INVALID_RATE_DESC + VENUE_DESC_BOB, Rate.MESSAGE_CONSTRAINTS);

        // invalid venue
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_MATH + RATE_DESC_BOB + INVALID_VENUE_DESC, Venue.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_LEVEL_DESC
                + SUBJECT_DESC_MATH + RATE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + validFieldsAfterName,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
