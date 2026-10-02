package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalLessons.BENSON_PHYSICS;
import static seedu.address.testutil.TypicalLessons.CARL_ENGLISH;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.exceptions.DuplicateLessonException;
import seedu.address.model.lesson.exceptions.LessonNotFoundException;
import seedu.address.testutil.LessonBuilder;

public class UniqueLessonListTest {

    private final UniqueLessonList uniqueLessonList = new UniqueLessonList();

    @Test
    public void contains_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.contains(null));
    }

    @Test
    public void contains_lessonNotInList_returnsFalse() {
        assertFalse(uniqueLessonList.contains(ALICE_MATH));
    }

    @Test
    public void contains_lessonInList_returnsTrue() {
        uniqueLessonList.add(ALICE_MATH);
        assertTrue(uniqueLessonList.contains(ALICE_MATH));
    }

    @Test
    public void contains_lessonWithSameIdentityFieldsInList_returnsTrue() {
        uniqueLessonList.add(ALICE_MATH);
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withVenue("Online").withNotes("Covered algebra").build();
        assertTrue(uniqueLessonList.contains(editedLesson));
    }

    @Test
    public void add_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.add(null));
    }

    @Test
    public void add_duplicateLesson_throwsDuplicateLessonException() {
        uniqueLessonList.add(ALICE_MATH);
        assertThrows(DuplicateLessonException.class, () -> uniqueLessonList.add(ALICE_MATH));
    }

    @Test
    public void setLesson_nullTargetLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.setLesson(null, ALICE_MATH));
    }

    @Test
    public void setLesson_nullEditedLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.setLesson(ALICE_MATH, null));
    }

    @Test
    public void setLesson_targetLessonNotInList_throwsLessonNotFoundException() {
        assertThrows(LessonNotFoundException.class, () -> uniqueLessonList.setLesson(ALICE_MATH, ALICE_MATH));
    }

    @Test
    public void setLesson_editedLessonIsSameLesson_success() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.setLesson(ALICE_MATH, ALICE_MATH);
        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(ALICE_MATH);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void setLesson_editedLessonHasSameIdentity_success() {
        uniqueLessonList.add(ALICE_MATH);
        Lesson editedLesson = new LessonBuilder(ALICE_MATH).withVenue("Online").withNotes("Covered algebra").build();
        uniqueLessonList.setLesson(ALICE_MATH, editedLesson);
        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(editedLesson);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void setLesson_editedLessonHasDifferentIdentity_success() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.setLesson(ALICE_MATH, BENSON_PHYSICS);
        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(BENSON_PHYSICS);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void setLesson_editedLessonHasNonUniqueIdentity_throwsDuplicateLessonException() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.add(BENSON_PHYSICS);
        assertThrows(DuplicateLessonException.class, () -> uniqueLessonList.setLesson(ALICE_MATH, BENSON_PHYSICS));
    }

    @Test
    public void remove_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.remove(null));
    }

    @Test
    public void remove_lessonDoesNotExist_throwsLessonNotFoundException() {
        assertThrows(LessonNotFoundException.class, () -> uniqueLessonList.remove(ALICE_MATH));
    }

    @Test
    public void remove_existingLesson_removesLesson() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.remove(ALICE_MATH);
        assertEquals(new UniqueLessonList(), uniqueLessonList);
    }

    @Test
    public void removeIf_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.removeIf(null));
    }

    @Test
    public void removeIf_predicate_removesMatchingLessons() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.add(BENSON_PHYSICS);
        uniqueLessonList.add(CARL_ENGLISH);

        uniqueLessonList.removeIf(lesson -> lesson.isWith(ALICE));

        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(BENSON_PHYSICS);
        expectedUniqueLessonList.add(CARL_ENGLISH);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void getOverlapping_nullLesson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.getOverlapping(null));
    }

    @Test
    public void getOverlapping_lessonsInList_returnsOnlyOverlappingLessons() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.add(CARL_ENGLISH);

        assertEquals(List.of(ALICE_MATH), uniqueLessonList.getOverlapping(BENSON_PHYSICS));
        assertEquals(List.of(), uniqueLessonList.getOverlapping(new LessonBuilder().withTime("18:00").build()));
    }

    @Test
    public void setLessons_nullUniqueLessonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.setLessons((UniqueLessonList) null));
    }

    @Test
    public void setLessons_uniqueLessonList_replacesOwnListWithProvidedUniqueLessonList() {
        uniqueLessonList.add(ALICE_MATH);
        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(BENSON_PHYSICS);
        uniqueLessonList.setLessons(expectedUniqueLessonList);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void setLessons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniqueLessonList.setLessons((List<Lesson>) null));
    }

    @Test
    public void setLessons_list_replacesOwnListWithProvidedList() {
        uniqueLessonList.add(ALICE_MATH);
        List<Lesson> lessonList = List.of(BENSON_PHYSICS);
        uniqueLessonList.setLessons(lessonList);
        UniqueLessonList expectedUniqueLessonList = new UniqueLessonList();
        expectedUniqueLessonList.add(BENSON_PHYSICS);
        assertEquals(expectedUniqueLessonList, uniqueLessonList);
    }

    @Test
    public void setLessons_listWithDuplicateLessons_throwsDuplicateLessonException() {
        List<Lesson> listWithDuplicateLessons = List.of(ALICE_MATH, ALICE_MATH);
        assertThrows(DuplicateLessonException.class, () -> uniqueLessonList.setLessons(listWithDuplicateLessons));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
                -> uniqueLessonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void iterator_returnsLessonsInOrder() {
        uniqueLessonList.add(ALICE_MATH);
        uniqueLessonList.add(CARL_ENGLISH);

        Iterator<Lesson> iterator = uniqueLessonList.iterator();

        assertEquals(ALICE_MATH, iterator.next());
        assertEquals(CARL_ENGLISH, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void equals() {
        UniqueLessonList other = new UniqueLessonList();

        // same object -> returns true
        assertTrue(uniqueLessonList.equals(uniqueLessonList));

        // same values -> returns true
        assertTrue(uniqueLessonList.equals(other));

        // null -> returns false
        assertFalse(uniqueLessonList.equals(null));

        // different types -> returns false
        assertFalse(uniqueLessonList.equals(5));

        // different values -> returns false
        other.add(ALICE_MATH);
        assertFalse(uniqueLessonList.equals(other));
    }

    @Test
    public void hashCode_equalLists_sameHashCode() {
        UniqueLessonList other = new UniqueLessonList();
        uniqueLessonList.add(ALICE_MATH);
        other.add(ALICE_MATH);
        assertEquals(uniqueLessonList.hashCode(), other.hashCode());
    }

    @Test
    public void toStringMethod() {
        uniqueLessonList.add(ALICE_MATH);
        assertEquals(List.of(ALICE_MATH).toString(), uniqueLessonList.toString());
    }
}
