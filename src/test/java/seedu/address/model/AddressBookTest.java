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
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.exceptions.DuplicateLessonException;
import seedu.address.model.lesson.exceptions.LessonNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.task.Task;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TaskBuilder;

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
    public void setLesson_replacementWithDifferentId_isRejected() {
        addressBook.addLesson(ALICE_MATH);
        Lesson replacement = new LessonBuilder(ALICE_MATH).withId(java.util.UUID.randomUUID()).build();

        assertThrows(IllegalArgumentException.class, () -> addressBook.setLesson(ALICE_MATH, replacement));
        assertEquals(List.of(ALICE_MATH), addressBook.getLessonList());
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
                + ", lessons=" + addressBook.getLessonList()
                + ", ledgerEntries=" + addressBook.getLedgerEntries()
                + ", tasks=" + addressBook.getTaskList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void resetData_copiesTheHomeworkOfAnyImplementation() {
        AddressBookStub newData = new AddressBookStub(List.of(ALICE));
        Homework item = new HomeworkBuilder(ALICE).build();
        newData.getHomeworkList().add(item);

        addressBook.resetData(newData);

        assertEquals(List.of(item), addressBook.getHomeworkList());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Lesson> lessons = FXCollections.observableArrayList();
        private final ObservableList<Homework> homework = FXCollections.observableArrayList();

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

        @Override
        public ObservableList<Task> getTaskList() {
            return FXCollections.observableArrayList();
        }

        @Override
        public ObservableList<seedu.address.model.finance.LedgerEntry> getLedgerEntries() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<Homework> getHomeworkList() {
            return homework;
        }
    }

    @Test
    public void removePerson_personWithTasks_removesTasksAboutPersonOnly() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);
        addressBook.addLesson(ALICE_MATH);
        Task aboutStudent = new TaskBuilder().withId("s").withStudent(ALICE).build();
        Task aboutLesson = new TaskBuilder().withId("l").withLesson(ALICE_MATH).build();
        Task aboutOther = new TaskBuilder().withId("o").withStudent(BENSON).build();
        addressBook.addTask(aboutStudent);
        addressBook.addTask(aboutLesson);
        addressBook.addTask(aboutOther);

        addressBook.removePerson(ALICE);

        assertEquals(List.of(aboutOther), addressBook.getTaskList());
    }

    @Test
    public void setPerson_nameChanged_updatesStudentOfTasks() {
        addressBook.addPerson(ALICE);
        addressBook.addLesson(ALICE_MATH);
        addressBook.addTask(new TaskBuilder().withId("s").withStudent(ALICE).build());
        addressBook.addTask(new TaskBuilder().withId("l").withLesson(ALICE_MATH).build());
        Person renamed = new PersonBuilder(ALICE).withName("Alice Tan").build();

        addressBook.setPerson(ALICE, renamed);

        assertTrue(addressBook.getTaskList().stream().allMatch(task -> task.isAbout(renamed)));
        Lesson renamedLesson = addressBook.getLessonList().get(0);
        assertTrue(addressBook.getTaskList().stream().filter(task -> task.getLessonLink().isPresent())
                .allMatch(task -> task.isAbout(renamedLesson)));
    }

    @Test
    public void setPerson_otherDetailsChanged_keepsTasksUntouched() {
        addressBook.addPerson(ALICE);
        Task task = new TaskBuilder().withStudent(ALICE).build();
        addressBook.addTask(task);

        addressBook.setPerson(ALICE, new PersonBuilder(ALICE).withVenue(VALID_VENUE_BOB).build());

        assertEquals(List.of(task), addressBook.getTaskList());
    }

    @Test
    public void setLesson_lessonMoved_tasksFollowTheLesson() {
        addressBook.addPerson(ALICE);
        addressBook.addLesson(ALICE_MATH);
        addressBook.addTask(new TaskBuilder().withLesson(ALICE_MATH).build());
        Lesson moved = ALICE_MATH.reschedule(new seedu.address.model.lesson.LessonDate("2031-05-04"),
                ALICE_MATH.getTime(), ALICE_MATH.getDuration(), null);

        addressBook.setLesson(ALICE_MATH, moved);

        assertTrue(addressBook.getTaskList().get(0).isAbout(moved));
        assertFalse(addressBook.getTaskList().get(0).isAbout(ALICE_MATH));
    }

    @Test
    public void setLesson_lessonCancelled_taskStaysAttached() {
        addressBook.addPerson(ALICE);
        addressBook.addLesson(ALICE_MATH);
        addressBook.addTask(new TaskBuilder().withLesson(ALICE_MATH).build());

        addressBook.setLesson(ALICE_MATH, ALICE_MATH.cancel("Student unwell"));

        assertTrue(addressBook.getTaskList().get(0).isAbout(ALICE_MATH));
        assertEquals(1, addressBook.getTaskList().size());
    }

    @Test
    public void removeLesson_deletesTasksAboutLessonButKeepsStudentTasks() {
        addressBook.addPerson(ALICE);
        addressBook.addLesson(ALICE_MATH);
        Task aboutStudent = new TaskBuilder().withId("s").withStudent(ALICE).build();
        addressBook.addTask(aboutStudent);
        addressBook.addTask(new TaskBuilder().withId("l").withLesson(ALICE_MATH).build());

        addressBook.removeLesson(ALICE_MATH);

        assertEquals(List.of(aboutStudent), addressBook.getTaskList());
    }

    @Test
    public void taskOperations_invalidArguments_throw() {
        Task task = new TaskBuilder().build();
        addressBook.addTask(task);

        assertThrows(NullPointerException.class, () -> addressBook.addTask(null));
        assertThrows(IllegalArgumentException.class, () -> addressBook.addTask(task)); // same id
        assertThrows(IllegalArgumentException.class, () -> addressBook.setTasks(List.of(task, task)));
        assertThrows(IllegalArgumentException.class, () -> addressBook.setTask(task,
                new TaskBuilder().withId("other").build()));
        assertThrows(IllegalArgumentException.class, () -> addressBook.setTask(
                new TaskBuilder().withId("gone").build(), new TaskBuilder().withId("gone").done().build()));
        assertThrows(IllegalArgumentException.class, () -> addressBook.removeTask(
                new TaskBuilder().withId("gone").build()));
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getTaskList().clear());
    }

    @Test
    public void resetData_copiesTasks_andEqualsConsidersTasks() {
        AddressBook source = new AddressBook();
        source.addPerson(ALICE);
        source.addTask(new TaskBuilder().build());

        addressBook.resetData(source);

        assertEquals(source, addressBook);
        assertEquals(source.hashCode(), addressBook.hashCode());
        addressBook.setTask(addressBook.getTaskList().get(0), addressBook.getTaskList().get(0).markDone());
        assertFalse(source.equals(addressBook));
    }
}
