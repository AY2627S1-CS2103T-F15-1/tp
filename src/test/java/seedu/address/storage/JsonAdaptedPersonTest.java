package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LEVEL = "S9";
    private static final String INVALID_SUBJECT = "#math";
    private static final String INVALID_RATE = "-5";
    private static final String INVALID_VENUE = " ";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().get().toString();
    private static final String VALID_LEVEL = BENSON.getLevel().toString();
    private static final List<JsonAdaptedSubject> VALID_SUBJECTS = BENSON.getSubjects().stream()
            .map(JsonAdaptedSubject::new)
            .collect(Collectors.toList());
    private static final String VALID_RATE = BENSON.getRate().toString();
    private static final String VALID_VENUE = BENSON.getVenue().get().toString();
    private static final String VALID_REMARK = "";

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_personWithRemark_returnsPerson() throws Exception {
        String remark = "Likes baseball";
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, remark);
        assertEquals(new PersonBuilder(BENSON).withRemark(remark).build(), person.toModelType());
    }

    @Test
    public void toModelType_nullRemark_returnsPersonWithEmptyRemark() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, null);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_personWithoutOptionalFields_returnsPerson() throws Exception {
        Person personWithoutOptionalFields = new PersonBuilder(BENSON).withoutEmail().withoutVenue().build();
        JsonAdaptedPerson person = new JsonAdaptedPerson(personWithoutOptionalFields);
        assertEquals(personWithoutOptionalFields, person.toModelType());
    }

    @Test
    public void toModelType_nullOptionalFields_returnsPersonWithoutOptionalFields() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, null, VALID_REMARK);
        Person modelPerson = person.toModelType();
        assertTrue(modelPerson.getEmail().isEmpty());
        assertTrue(modelPerson.getVenue().isEmpty());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Phone.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Level.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_SUBJECTS, VALID_RATE, VALID_VENUE, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Level.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidSubjects_throwsIllegalValueException() {
        List<JsonAdaptedSubject> invalidSubjects = new ArrayList<>(VALID_SUBJECTS);
        invalidSubjects.add(new JsonAdaptedSubject(INVALID_SUBJECT));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                invalidSubjects, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullSubjects_returnsPersonWithoutSubjects() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                null, VALID_RATE, VALID_VENUE, VALID_REMARK);
        assertTrue(person.toModelType().getSubjects().isEmpty());
    }

    @Test
    public void toModelType_invalidRate_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, INVALID_RATE, VALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Rate.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullRate_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, null, VALID_VENUE, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Rate.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidVenue_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LEVEL,
                VALID_SUBJECTS, VALID_RATE, INVALID_VENUE, VALID_REMARK);
        assertThrows(IllegalValueException.class, Venue.MESSAGE_CONSTRAINTS, person::toModelType);
    }

}
