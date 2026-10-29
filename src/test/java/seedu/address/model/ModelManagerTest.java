package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_LESSONS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalLessons.BENSON_PHYSICS;
import static seedu.address.testutil.TypicalLessons.CARL_ENGLISH;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.GEORGE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.StudentOrder;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.task.Task;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.LessonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager;

    @BeforeEach
    public void resetModelManager() {
        modelManager = new ModelManager();
    }

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void sortPersons_nullOrder_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.sortPersons(null));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void hasLesson_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasLesson(null));
    }

    @Test
    public void hasLesson_lessonNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasLesson(ALICE_MATH));
    }

    @Test
    public void hasLesson_lessonInAddressBook_returnsTrue() {
        modelManager.addLesson(ALICE_MATH);
        assertTrue(modelManager.hasLesson(ALICE_MATH));
    }

    @Test
    public void addLesson_filteredLessonList_keepsTheShownLessonsUnchanged() {
        modelManager.addLesson(ALICE_MATH);
        modelManager.updateFilteredLessonList(lesson -> false);

        modelManager.addLesson(CARL_ENGLISH);

        assertEquals(List.of(), modelManager.getFilteredLessonList());
    }

    @Test
    public void deleteLesson_existingLesson_removesLesson() {
        modelManager.addLesson(ALICE_MATH);
        modelManager.deleteLesson(ALICE_MATH);
        assertFalse(modelManager.hasLesson(ALICE_MATH));
    }

    @Test
    public void setLesson_nullEditedLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setLesson(ALICE_MATH, null));
    }

    @Test
    public void setLesson_existingLesson_replacesLesson() {
        modelManager.addLesson(ALICE_MATH);
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withNotes("Covered algebra").build();
        modelManager.setLesson(ALICE_MATH, editedLesson);
        assertEquals(List.of(editedLesson), modelManager.getFilteredLessonList());
    }

    @Test
    public void getConflictingLessons_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.getConflictingLessons(null));
    }

    @Test
    public void getConflictingLessons_overlappingLesson_returnsOverlappingLessons() {
        modelManager.addLesson(ALICE_MATH);
        modelManager.addLesson(CARL_ENGLISH);
        assertEquals(List.of(ALICE_MATH), modelManager.getConflictingLessons(BENSON_PHYSICS));
    }

    @Test
    public void getFilteredLessonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredLessonList().remove(0));
    }

    @Test
    public void updateFilteredLessonList_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.updateFilteredLessonList(null));
    }

    @Test
    public void updateFilteredLessonList_predicate_filtersLessons() {
        modelManager.addLesson(ALICE_MATH);
        modelManager.addLesson(CARL_ENGLISH);

        modelManager.updateFilteredLessonList(lesson -> lesson.getSubject().equals(CARL_ENGLISH.getSubject()));
        assertEquals(List.of(CARL_ENGLISH), modelManager.getFilteredLessonList());

        modelManager.updateFilteredLessonList(PREDICATE_SHOW_ALL_LESSONS);
        assertEquals(List.of(ALICE_MATH, CARL_ENGLISH), modelManager.getFilteredLessonList());
    }

    private ModelManager copyOfModelManager() {
        return new ModelManager(modelManager.getAddressBook(), modelManager.getUserPrefs());
    }

    @Test
    public void undoDestructiveAction_nothing_returnsDestructiveActionNone() {
        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
    }

    @Test
    public void undoDestructiveAction_addPerson_returnsDestructiveActionNone() {
        modelManager.addPerson(DANIEL);
        ModelManager before = copyOfModelManager();

        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_addLesson_returnsDestructiveActionNone() {
        modelManager.addLesson(BENSON_PHYSICS);
        ModelManager before = copyOfModelManager();

        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_addHomework_returnsDestructiveActionNone() {
        modelManager.addPerson(FIONA);
        Homework hw = Homework.assign(
                FIONA.getId(), "Homework Test",
                LocalDate.of(2026, 11, 30));
        modelManager.addHomework(hw);
        ModelManager before = copyOfModelManager();

        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_addTask_returnsDestructiveActionNone() {
        modelManager.addPerson(ELLE);
        Task task = Task.create(
                 "Task Test",
                LocalDate.of(2026, 11, 30), ELLE);
        modelManager.addTask(task);
        ModelManager before = copyOfModelManager();

        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_removePerson_returnsDestructiveActionDeleteStudentAndUndoes() {
        LocalDate date = LocalDate.of(2027, 8, 31);
        modelManager.addPerson(CARL);
        modelManager.addLesson(CARL_ENGLISH);
        modelManager.addHomework(Homework.assign(CARL.getId(), "A", date));
        modelManager.addLedgerEntry(LedgerEntry.charge(CARL, CARL_ENGLISH));
        modelManager.addLedgerEntry(LedgerEntry.payment(CARL, new BigDecimal(10), date));
        modelManager.addTask(Task.create("B", date, CARL));
        ModelManager before = copyOfModelManager();

        modelManager.deletePerson(CARL);

        assertTrue(modelManager.getAddressBook().isEmpty());
        assertEquals(DestructiveActionType.DELETE_STUDENT, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_removeLesson_returnsDestructiveActionDeleteLessonAndUndoes() {
        modelManager.addLesson(BENSON_PHYSICS);
        ModelManager before = copyOfModelManager();
        modelManager.deleteLesson(BENSON_PHYSICS);

        assertEquals(DestructiveActionType.DELETE_LESSON, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_removeHomework_returnsDestructiveActionDeleteHwAndUndoes() {
        modelManager.addPerson(GEORGE);
        Homework hw = Homework.assign(
                GEORGE.getId(), "Test Homework",
                LocalDate.of(2026, 10, 31));
        modelManager.addHomework(hw);

        ModelManager before = copyOfModelManager();
        modelManager.deleteHomework(hw);

        assertEquals(DestructiveActionType.DELETE_HW, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_removeTask_returnsDestructiveActionDeleteTaskAndUndoes() {
        modelManager.addPerson(ALICE);
        Task task = Task.create(
                "Test Task",
                LocalDate.of(2026, 10, 31), ALICE);
        modelManager.addTask(task);

        ModelManager before = copyOfModelManager();
        modelManager.deleteTask(task);

        assertEquals(DestructiveActionType.DELETE_TASK, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_clear_returnsDestructiveActionClearAndUndoes() {
        modelManager.addPerson(BENSON);
        modelManager.addLesson(BENSON_PHYSICS);
        modelManager.addPerson(CARL);
        modelManager.addLesson(CARL_ENGLISH);
        modelManager.addPerson(GEORGE);
        modelManager.addHomework(Homework.assign(
                GEORGE.getId(), "Test Homework",
                LocalDate.of(2026, 10, 31)));
        modelManager.addTask(Task.create(
                "Test Task",
                LocalDate.of(2026, 10, 31), BENSON));
        modelManager.setTravelBuffer(45);
        modelManager.addLedgerEntry(LedgerEntry.charge(CARL, CARL_ENGLISH));
        modelManager.sortPersons(StudentOrder.DESCENDING);

        ModelManager before = copyOfModelManager();

        modelManager.clearAddressBook();
        modelManager.clearAddressBook();
        modelManager.clearAddressBook();

        assertEquals(DestructiveActionType.CLEAR, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_informationalMethods_doesNotResetUndo() {
        modelManager.addLesson(CARL_ENGLISH);
        ModelManager before = copyOfModelManager();
        modelManager.deleteLesson(CARL_ENGLISH);

        modelManager.getFilteredLessonList();
        modelManager.hasLesson(ALICE_MATH);
        modelManager.hasPerson(DANIEL);

        assertEquals(DestructiveActionType.DELETE_LESSON, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_addingThings_resetsUndo() {
        modelManager.addPerson(BENSON);
        modelManager.addPerson(ELLE);
        modelManager.addPerson(FIONA);
        modelManager.deletePerson(ELLE);

        modelManager.addLesson(BENSON_PHYSICS);
        ModelManager before = copyOfModelManager();

        assertEquals(DestructiveActionType.NONE, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }

    @Test
    public void undoDestructiveAction_failedDestruct_doesNotResetUndo() {
        modelManager.addPerson(BENSON);
        modelManager.addLesson(BENSON_PHYSICS);
        modelManager.addPerson(ELLE);
        modelManager.deleteLesson(BENSON_PHYSICS);
        ModelManager before = copyOfModelManager();
        assertThrows(PersonNotFoundException.class, () -> modelManager.deletePerson(FIONA));

        assertEquals(DestructiveActionType.DELETE_LESSON, modelManager.undoDestructiveAction());
        assertEquals(before, modelManager);
    }


    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different filteredLessonList -> returns false
        ModelManager modelManagerWithLessons = new ModelManager(
                new AddressBookBuilder(new AddressBook(addressBook)).build(), userPrefs);
        modelManagerWithLessons.addLesson(ALICE_MATH);
        ModelManager sameModelManagerWithLessons = new ModelManager(modelManagerWithLessons.getAddressBook(),
                userPrefs);
        assertTrue(modelManagerWithLessons.equals(sameModelManagerWithLessons));
        modelManagerWithLessons.updateFilteredLessonList(lesson -> false);
        assertFalse(modelManagerWithLessons.equals(sameModelManagerWithLessons));

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void getFilteredLessonList_lessonsAddedOutOfOrder_isInAgendaOrder() {
        modelManager.addLesson(CARL_ENGLISH);
        modelManager.addLesson(ALICE_MATH);

        assertEquals(List.of(ALICE_MATH, CARL_ENGLISH), modelManager.getFilteredLessonList());
    }
}
