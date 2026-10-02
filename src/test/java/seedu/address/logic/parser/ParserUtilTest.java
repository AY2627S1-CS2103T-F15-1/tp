package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LEVEL = "S9";
    private static final String INVALID_SUBJECT = "#math";
    private static final String INVALID_RATE = "-5";
    private static final String INVALID_VENUE = " ";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_LEVEL = "S3";
    private static final String VALID_SUBJECT_1 = "Math";
    private static final String VALID_SUBJECT_2 = "Physics";
    private static final String VALID_RATE = "62.50";
    private static final String VALID_VENUE = "123 Main Street #0505";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseLevel_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseLevel(null));
    }

    @Test
    public void parseLevel_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Level.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseLevel(INVALID_LEVEL));
    }

    @Test
    public void parseLevel_validValueWithoutWhitespace_returnsLevel() throws Exception {
        Level expectedLevel = new Level(VALID_LEVEL);
        assertEquals(expectedLevel, ParserUtil.parseLevel(VALID_LEVEL));
    }

    @Test
    public void parseLevel_validValueWithWhitespace_returnsTrimmedLevel() throws Exception {
        String levelWithWhitespace = WHITESPACE + VALID_LEVEL + WHITESPACE;
        Level expectedLevel = new Level(VALID_LEVEL);
        assertEquals(expectedLevel, ParserUtil.parseLevel(levelWithWhitespace));
    }

    @Test
    public void parseSubject_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseSubject(null));
    }

    @Test
    public void parseSubject_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Subject.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseSubject(INVALID_SUBJECT));
    }

    @Test
    public void parseSubject_validValueWithoutWhitespace_returnsSubject() throws Exception {
        Subject expectedSubject = new Subject(VALID_SUBJECT_1);
        assertEquals(expectedSubject, ParserUtil.parseSubject(VALID_SUBJECT_1));
    }

    @Test
    public void parseSubject_validValueWithWhitespace_returnsTrimmedSubject() throws Exception {
        String subjectWithWhitespace = WHITESPACE + VALID_SUBJECT_1 + WHITESPACE;
        Subject expectedSubject = new Subject(VALID_SUBJECT_1);
        assertEquals(expectedSubject, ParserUtil.parseSubject(subjectWithWhitespace));
    }

    @Test
    public void parseSubjects_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseSubjects(null));
    }

    @Test
    public void parseSubjects_collectionWithInvalidSubjects_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseSubjects(List.of(VALID_SUBJECT_1, INVALID_SUBJECT)));
    }

    @Test
    public void parseSubjects_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseSubjects(List.of()).isEmpty());
    }

    @Test
    public void parseSubjects_collectionWithValidSubjects_returnsSubjectSet() throws Exception {
        Set<Subject> actualSubjectSet = ParserUtil.parseSubjects(List.of(VALID_SUBJECT_1, VALID_SUBJECT_2));
        Set<Subject> expectedSubjectSet = Set.of(new Subject(VALID_SUBJECT_1), new Subject(VALID_SUBJECT_2));

        assertEquals(expectedSubjectSet, actualSubjectSet);
    }

    @Test
    public void parseRate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseRate(null));
    }

    @Test
    public void parseRate_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Rate.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseRate(INVALID_RATE));
    }

    @Test
    public void parseRate_validValueWithoutWhitespace_returnsRate() throws Exception {
        Rate expectedRate = new Rate(VALID_RATE);
        assertEquals(expectedRate, ParserUtil.parseRate(VALID_RATE));
    }

    @Test
    public void parseRate_validValueWithWhitespace_returnsTrimmedRate() throws Exception {
        String rateWithWhitespace = WHITESPACE + VALID_RATE + WHITESPACE;
        Rate expectedRate = new Rate(VALID_RATE);
        assertEquals(expectedRate, ParserUtil.parseRate(rateWithWhitespace));
    }

    @Test
    public void parseVenue_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseVenue(null));
    }

    @Test
    public void parseVenue_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Venue.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseVenue(INVALID_VENUE));
    }

    @Test
    public void parseVenue_validValueWithoutWhitespace_returnsVenue() throws Exception {
        Venue expectedVenue = new Venue(VALID_VENUE);
        assertEquals(expectedVenue, ParserUtil.parseVenue(VALID_VENUE));
    }

    @Test
    public void parseVenue_validValueWithWhitespace_returnsTrimmedVenue() throws Exception {
        String venueWithWhitespace = WHITESPACE + VALID_VENUE + WHITESPACE;
        Venue expectedVenue = new Venue(VALID_VENUE);
        assertEquals(expectedVenue, ParserUtil.parseVenue(venueWithWhitespace));
    }
}
