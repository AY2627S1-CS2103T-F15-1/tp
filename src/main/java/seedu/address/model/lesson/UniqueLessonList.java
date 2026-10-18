package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.lesson.exceptions.DuplicateLessonException;
import seedu.address.model.lesson.exceptions.LessonNotFoundException;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

/**
 * A list of lessons that enforces uniqueness between its elements and does not allow nulls.
 * A lesson is considered unique by comparing using {@code Lesson#isSameLesson(Lesson)}. As such, adding and updating of
 * lessons uses Lesson#isSameLesson(Lesson) for equality so as to ensure that the lesson being added or updated is
 * unique in terms of identity in the UniqueLessonList. However, the removal of a lesson uses Lesson#equals(Object) so
 * as to ensure that the lesson with exactly the same fields will be removed.
 *
 * The list keeps an index of the slots of its scheduled lessons, so that a duplicate is found without comparing the
 * new lesson with every lesson in the list. Only the methods of this class change the list, which keeps the index
 * in step.
 *
 * Supports a minimal set of list operations.
 *
 * @see Lesson#isSameLesson(Lesson)
 */
public class UniqueLessonList implements Iterable<Lesson> {

    private final ObservableList<Lesson> internalList = FXCollections.observableArrayList();
    private final ObservableList<Lesson> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** How many scheduled (not cancelled) lessons occupy each slot; a cancelled lesson occupies none. */
    private Map<Slot, Integer> scheduledPerSlot = new HashMap<>();

    /**
     * Returns true if the list contains a lesson that {@code toCheck} would duplicate. A cancelled lesson never
     * counts as a duplicate, so that its slot can be booked again.
     */
    public boolean contains(Lesson toCheck) {
        requireNonNull(toCheck);
        return !toCheck.isCancelled() && scheduledPerSlot.containsKey(Slot.of(toCheck));
    }

    /**
     * Adds a lesson to the list.
     * The lesson must not already exist in the list.
     */
    public void add(Lesson toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            throw new DuplicateLessonException();
        }
        internalList.add(toAdd);
        occupy(toAdd);
    }

    /**
     * Replaces the lesson {@code target} in the list with {@code editedLesson}.
     * {@code target} must exist in the list.
     * The lesson identity of {@code editedLesson} must not be the same as another existing lesson in the list.
     */
    public void setLesson(Lesson target, Lesson editedLesson) {
        requireAllNonNull(target, editedLesson);

        int index = indexOfRecord(target);
        if (index == -1) {
            throw new LessonNotFoundException();
        }

        if (!target.isSameLesson(editedLesson) && contains(editedLesson)) {
            throw new DuplicateLessonException();
        }

        vacate(internalList.get(index));
        internalList.set(index, editedLesson);
        occupy(editedLesson);
    }

    /**
     * Removes the equivalent lesson from the list.
     * The lesson must exist in the list.
     */
    public void remove(Lesson toRemove) {
        requireNonNull(toRemove);
        int index = indexOfRecord(toRemove);
        if (index < 0) {
            throw new LessonNotFoundException();
        }
        vacate(internalList.get(index));
        internalList.remove(index);
    }

    /** Finds the persisted record first, retaining legacy value lookup for detached callers. */
    private int indexOfRecord(Lesson target) {
        for (int i = 0; i < internalList.size(); i++) {
            if (internalList.get(i).getId().equals(target.getId())) {
                return i;
            }
        }
        return internalList.indexOf(target);
    }

    /**
     * Removes every lesson in the list that satisfies {@code predicate}.
     *
     * @return true if at least one lesson was removed.
     */
    public boolean removeIf(Predicate<Lesson> predicate) {
        requireNonNull(predicate);
        boolean isRemoved = internalList.removeIf(predicate);
        if (isRemoved) {
            scheduledPerSlot = indexSlots(internalList);
        }
        return isRemoved;
    }

    /**
     * Returns the lessons in the list, other than {@code toCheck} itself, that overlap with {@code toCheck}.
     */
    public List<Lesson> getOverlapping(Lesson toCheck) {
        requireNonNull(toCheck);
        return internalList.stream()
                .filter(lesson -> !lesson.equals(toCheck) && lesson.overlaps(toCheck))
                .collect(Collectors.toList());
    }

    public void setLessons(UniqueLessonList replacement) {
        requireNonNull(replacement);
        internalList.setAll(replacement.internalList);
        scheduledPerSlot = new HashMap<>(replacement.scheduledPerSlot);
    }

    /**
     * Replaces the contents of this list with {@code lessons}.
     * {@code lessons} must not contain duplicate lessons.
     */
    public void setLessons(List<Lesson> lessons) {
        requireAllNonNull(lessons);
        Map<Slot, Integer> slots = indexSlots(lessons);
        if (!slotsAreUnique(slots)) {
            throw new DuplicateLessonException();
        }

        internalList.setAll(lessons);
        scheduledPerSlot = slots;
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Lesson> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Lesson> iterator() {
        return internalUnmodifiableList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniqueLessonList otherUniqueLessonList)) {
            return false;
        }

        return internalList.equals(otherUniqueLessonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /** Records that {@code lesson} occupies its slot, unless it is cancelled. */
    private void occupy(Lesson lesson) {
        if (!lesson.isCancelled()) {
            scheduledPerSlot.merge(Slot.of(lesson), 1, Integer::sum);
        }
    }

    /** Records that {@code lesson} no longer occupies its slot, unless it was cancelled and so occupied none. */
    private void vacate(Lesson lesson) {
        if (!lesson.isCancelled()) {
            scheduledPerSlot.computeIfPresent(Slot.of(lesson), (slot, count) -> count > 1 ? count - 1 : null);
        }
    }

    /** Counts the scheduled lessons of {@code lessons} per slot. */
    private static Map<Slot, Integer> indexSlots(Iterable<Lesson> lessons) {
        Map<Slot, Integer> slots = new HashMap<>();
        for (Lesson lesson : lessons) {
            if (!lesson.isCancelled()) {
                slots.merge(Slot.of(lesson), 1, Integer::sum);
            }
        }
        return slots;
    }

    /** Returns true if no slot holds more than one scheduled lesson. */
    private static boolean slotsAreUnique(Map<Slot, Integer> slots) {
        return slots.values().stream().allMatch(count -> count == 1);
    }

    /**
     * What makes two lessons the same lesson (see {@link Lesson#isSameLesson(Lesson)}). The student's name is
     * compared without regard to case, as {@link String#equalsIgnoreCase(String)} compares it.
     */
    private record Slot(String studentName, Phone studentPhone, Subject subject, LessonDate date, LessonTime time,
            LessonDuration duration) {
        static Slot of(Lesson lesson) {
            return new Slot(foldCase(lesson.getStudentName().fullName), lesson.getStudentPhone(),
                    lesson.getSubject(), lesson.getDate(), lesson.getTime(), lesson.getDuration());
        }

        /** Maps each character as {@code equalsIgnoreCase} does, so that names it calls equal get equal keys. */
        private static String foldCase(String text) {
            return text.codePoints().map(point -> Character.toLowerCase(Character.toUpperCase(point)))
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        }
    }
}
