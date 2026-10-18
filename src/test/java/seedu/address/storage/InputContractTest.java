package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Venue;
import seedu.address.testutil.PersonBuilder;

/** Acceptance tests for strict new input and explicit legacy loading. */
public class InputContractTest {
    @Test
    public void newContact_shortPhoneOrNumericName_rejected() throws Exception {
        for (String phone : new String[] {"911", "1234567", "+12 345-67"}) {
            assertFalse(Phone.isValidPhone(phone));
            assertThrows(ParseException.class, () -> ParserUtil.parsePhone(phone));
        }
        assertTrue(Phone.isValidPhone("12345678"));
        assertFalse(Name.isValidName("12345"));
        assertTrue(Name.isValidName("John @ Tan"));
        assertEquals("John @ Tan", ParserUtil.parseName("  John   @ Tan ").fullName);
    }

    @Test
    public void newText_controlsAndLineSeparators_rejectedBeforeNormalization() {
        for (String separator : new String[] {"\n", "\r", "\t", "\u0000", "\u2028", "\u2029"}) {
            assertFalse(Venue.isValidVenue("Home" + separator + "room"));
            assertThrows(IllegalArgumentException.class, () -> new Venue("Home" + separator + "room"));
            assertFalse(Lesson.isValidCancelReason("unwell" + separator));
        }
    }

    @Test
    public void newReason_repeatedSpaces_normalized() throws Exception {
        assertEquals("Student unwell", ParserUtil.parseReason("  Student   unwell  "));
    }

    @Test
    public void newEmail_writtenContract_allowsPercentAndComparesIgnoringCase() {
        assertTrue(Email.isValidEmail("name%tag@example.com"));
        assertFalse(Email.isValidEmail("name@domain.12"));
        assertEquals(new Email("A@Example.com"), new Email("a@example.COM"));
        assertEquals(new Email("A@Example.com").hashCode(), new Email("a@example.COM").hashCode());
    }

    @Test
    public void newStudent_emptySubjectSet_rejected() {
        Person student = new PersonBuilder().build();
        assertThrows(IllegalArgumentException.class, () -> new Person(student.getName(), student.getPhone(), null,
                student.getLevel(), Set.of(), student.getRate(), null));
        assertThrows(ParseException.class, () -> ParserUtil.parseSubjects(List.of()));
    }

    @Test
    public void load_legacyContact_preservesEveryValue() throws Exception {
        Person loaded = new JsonAdaptedPerson("12345", "911", "a@domain.12", "J1",
                List.of(new JsonAdaptedSubject("Math")), "100", "Home\u0000", "legacy remark").toModelType();
        assertEquals("12345", loaded.getName().fullName);
        assertEquals("911", loaded.getPhone().value);
        assertEquals("a@domain.12", loaded.getEmail().orElseThrow().value);
        assertEquals("Home\u0000", loaded.getVenue().orElseThrow().value);
        assertEquals(loaded, new JsonAdaptedPerson(loaded).toModelType());
    }

    @Test
    public void load_invalidSubjectCounts_rejectedWithoutDiscardingIndividualSubjects() {
        for (int count : new int[] {0, 11}) {
            List<JsonAdaptedSubject> subjects = new ArrayList<>();
            for (int index = 0; index < count; index++) {
                subjects.add(new JsonAdaptedSubject("Subject " + index));
            }
            JsonAdaptedPerson loaded = new JsonAdaptedPerson("Alice", "91234567", null, "J1", subjects,
                    "100", null, "");
            assertThrows(IllegalValueException.class, loaded::toModelType);
        }
    }

    @Test
    public void load_invalidCancellationReason_rejected() {
        for (String reason : new String[] {"", "unwell\nnext", "x".repeat(201), "unwell\u0000"}) {
            JsonAdaptedLesson lesson = new JsonAdaptedLesson("Alice", "91234567", "Math", "2026-10-18",
                    "16:00", 60, null, "cancelled", null, reason);
            assertThrows(IllegalValueException.class, lesson::toModelType);
        }
    }
}
