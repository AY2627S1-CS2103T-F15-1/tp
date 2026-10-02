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

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.LessonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

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
    public void sortPersons_nullComparator_throwsNullPointerException() {
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
    public void addLesson_filteredLessonList_showsAddedLesson() {
        modelManager.addLesson(ALICE_MATH);
        modelManager.updateFilteredLessonList(lesson -> false);

        modelManager.addLesson(CARL_ENGLISH);

        assertEquals(List.of(ALICE_MATH, CARL_ENGLISH), modelManager.getFilteredLessonList());
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
}
