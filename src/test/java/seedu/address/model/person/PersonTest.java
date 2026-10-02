package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_RATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_PHYSICS;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VENUE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_nullRequiredField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(null, ALICE.getPhone(), null, ALICE.getLevel(),
                ALICE.getSubjects(), ALICE.getRate(), null));
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(), null,
                ALICE.getLevel(), ALICE.getSubjects(), null, null));
    }

    @Test
    public void constructor_noRemark_remarkIsEmpty() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), null, ALICE.getLevel(), ALICE.getSubjects(),
                ALICE.getRate(), null);
        assertEquals(new Remark(""), person.getRemark());
    }

    @Test
    public void getSubjects_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getSubjects().remove(new Subject("Math")));
    }

    @Test
    public void getEmailAndVenue_optionalFieldsAbsent_returnsEmpty() {
        Person person = new PersonBuilder().withoutEmail().withoutVenue().build();
        assertTrue(person.getEmail().isEmpty());
        assertTrue(person.getVenue().isEmpty());
    }

    @Test
    public void getEmailAndVenue_optionalFieldsPresent_returnsValue() {
        assertEquals(new Email("alice@example.com"), ALICE.getEmail().get());
        assertEquals(new Venue("123, Jurong West Ave 6, #08-111"), ALICE.getVenue().get());
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).withLevel(VALID_LEVEL_BOB)
                .withSubjects(VALID_SUBJECT_PHYSICS).withRate(VALID_RATE_BOB).withVenue(VALID_VENUE_BOB)
                .withRemark("Likes swimming").build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, same phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, same phone -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, same phone -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void hasIdentity() {
        // same name and phone -> returns true
        assertTrue(ALICE.hasIdentity(ALICE.getName(), ALICE.getPhone()));

        // name differs in case -> returns true
        assertTrue(ALICE.hasIdentity(new Name(ALICE.getName().fullName.toUpperCase()), ALICE.getPhone()));

        // different name -> returns false
        assertFalse(ALICE.hasIdentity(BOB.getName(), ALICE.getPhone()));

        // different phone -> returns false
        assertFalse(ALICE.hasIdentity(ALICE.getName(), BOB.getPhone()));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // missing email -> returns false
        editedAlice = new PersonBuilder(ALICE).withoutEmail().build();
        assertFalse(ALICE.equals(editedAlice));

        // different level -> returns false
        editedAlice = new PersonBuilder(ALICE).withLevel(VALID_LEVEL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different subjects -> returns false
        editedAlice = new PersonBuilder(ALICE).withSubjects(VALID_SUBJECT_PHYSICS).build();
        assertFalse(ALICE.equals(editedAlice));

        // different rate -> returns false
        editedAlice = new PersonBuilder(ALICE).withRate(VALID_RATE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different venue -> returns false
        editedAlice = new PersonBuilder(ALICE).withVenue(VALID_VENUE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // missing venue -> returns false
        editedAlice = new PersonBuilder(ALICE).withoutVenue().build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Likes swimming").build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void hashCode_equalPersons_sameHashCode() {
        assertEquals(ALICE.hashCode(), new PersonBuilder(ALICE).build().hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail().get() + ", level=" + ALICE.getLevel()
                + ", subjects=" + ALICE.getSubjects() + ", rate=" + ALICE.getRate()
                + ", venue=" + ALICE.getVenue().get() + ", remark=" + ALICE.getRemark() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
