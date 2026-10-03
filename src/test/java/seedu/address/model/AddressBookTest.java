package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_PHYSICS;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VENUE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalLessons.BENSON_PHYSICS;
import static seedu.address.testutil.TypicalLessons.CARL_ENGLISH;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.exceptions.DuplicateLessonException;
import seedu.address.model.lesson.exceptions.LessonNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
        assertEquals(List.of(), addressBook.getLessonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withVenue(VALID_VENUE_BOB).withSubjects(VALID_SUBJECT_PHYSICS)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBookWithLessons_replacesLessons() {
        AddressBook newData = new AddressBook();
        newData.addPerson(ALICE);
        newData.addLesson(ALICE_MATH);
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
        assertEquals(List.of(ALICE_MATH), addressBook.getLessonList());
    }

    @Test
    public void resetData_withDuplicateLessons_throwsDuplicateLessonException() {
        Lesson sameAliceMath = new LessonBuilder(ALICE_MATH).withVenue("Online").build();
        AddressBookStub newData = new AddressBookStub(List.of(ALICE), List.of(ALICE_MATH, sameAliceMath));

        assertThrows(DuplicateLessonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withVenue(VALID_VENUE_BOB).withSubjects(VALID_SUBJECT_PHYSICS)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void hasLesson_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasLesson(null));
    }

    @Test
    public void hasLesson_lessonNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasLesson(ALICE_MATH));
    }

    @Test
    public void hasLesson_lessonInAddressBook_returnsTrue() {
        addressBook.addLesson(ALICE_MATH);
        assertTrue(addressBook.hasLesson(ALICE_MATH));
    }

    @Test
    public void hasLesson_lessonWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addLesson(ALICE_MATH);
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withVenue("Online").withNotes("Covered algebra").build();
        assertTrue(addressBook.hasLesson(editedLesson));
    }

    @Test
    public void addLesson_duplicateLesson_throwsDuplicateLessonException() {
        addressBook.addLesson(ALICE_MATH);
        assertThrows(DuplicateLessonException.class, () -> addressBook.addLesson(ALICE_MATH));
    }

    @Test
    public void setLesson_existingLesson_replacesLesson() {
        addressBook.addLesson(ALICE_MATH);
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withNotes("Covered algebra").build();
        addressBook.setLesson(ALICE_MATH, editedLesson);
        assertEquals(List.of(editedLesson), addressBook.getLessonList());
    }

    @Test
    public void setLesson_nullEditedLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.setLesson(ALICE_MATH, null));
    }

    @Test
    public void setLesson_lessonNotInAddressBook_throwsLessonNotFoundException() {
        assertThrows(LessonNotFoundException.class, () -> addressBook.setLesson(ALICE_MATH, ALICE_MATH));
    }

    @Test
    public void removeLesson_existingLesson_removesLesson() {
        addressBook.addLesson(ALICE_MATH);
        addressBook.removeLesson(ALICE_MATH);
        assertEquals(List.of(), addressBook.getLessonList());
    }

    @Test
    public void getConflictingLessons_overlappingLessons_returnsOverlappingLessons() {
        addressBook.addLesson(ALICE_MATH);
        addressBook.addLesson(CARL_ENGLISH);
        assertEquals(List.of(ALICE_MATH), addressBook.getConflictingLessons(BENSON_PHYSICS));
    }

    @Test
    public void removePerson_personWithLessons_removesLessonsOfPerson() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addLesson(ALICE_MATH);
        addressBook.addLesson(BENSON_PHYSICS);

        addressBook.removePerson(ALICE);

        assertEquals(List.of(BENSON_PHYSICS), addressBook.getLessonList());
    }

    @Test
    public void setPerson_nameAndPhoneChanged_updatesStudentOfLessons() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addLesson(ALICE_MATH);
        addressBook.addLesson(BENSON_PHYSICS);
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB + " Jr").withPhone(VALID_PHONE_BOB)
                .build();

        addressBook.setPerson(ALICE, editedAlice);

        Lesson expectedLesson = new LessonBuilder(ALICE_MATH).withStudent(editedAlice).build();
        assertEquals(List.of(expectedLesson, BENSON_PHYSICS), addressBook.getLessonList());
    }

    @Test
    public void setPerson_nameAndPhoneNotChanged_keepsLessons() {
        addressBook.addPerson(ALICE);
        addressBook.addLesson(ALICE_MATH);
        Person editedAlice = new PersonBuilder(ALICE).withVenue(VALID_VENUE_BOB).build();

        addressBook.setPerson(ALICE, editedAlice);

        assertEquals(List.of(ALICE_MATH), addressBook.getLessonList());
    }

    @Test
    public void getLessonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getLessonList().remove(0));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void hashCode_equalAddressBooks_sameHashCode() {
        AddressBook otherAddressBook = new AddressBook();
        addressBook.addLesson(ALICE_MATH);
        otherAddressBook.addLesson(ALICE_MATH);
        assertEquals(addressBook.hashCode(), otherAddressBook.hashCode());
    }

    @Test
    public void equals_differentLessons_returnsFalse() {
        AddressBook otherAddressBook = new AddressBook();
        otherAddressBook.addLesson(ALICE_MATH);
        assertFalse(addressBook.equals(otherAddressBook));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", lessons=" + addressBook.getLessonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Lesson> lessons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        AddressBookStub(Collection<Person> persons, Collection<Lesson> lessons) {
            this.persons.setAll(persons);
            this.lessons.setAll(lessons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Lesson> getLessonList() {
            return lessons;
        }
    }

}
