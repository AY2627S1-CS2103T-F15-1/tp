package seedu.address.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalLessons.ALICE_MATH;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TaskBuilder;

public class TaskTest {

    @Test
    public void isValidDescription() {
        assertFalse(Task.isValidDescription(null));
        assertFalse(Task.isValidDescription(""));
        assertFalse(Task.isValidDescription("   "));
        assertFalse(Task.isValidDescription("two\nlines"));
        assertFalse(Task.isValidDescription("tab\there"));
        assertFalse(Task.isValidDescription("a".repeat(Task.MAX_DESCRIPTION_LENGTH + 1)));

        assertTrue(Task.isValidDescription("a"));
        assertTrue(Task.isValidDescription("a".repeat(Task.MAX_DESCRIPTION_LENGTH)));
        assertTrue(Task.isValidDescription("  padded  ")); // trimmed when stored
        assertTrue(Task.isValidDescription("Call parent re: S$ fees (50%) - ok?"));
    }

    @Test
    public void constructor_invalidArguments_throws() {
        Name name = ALICE.getName();
        Phone phone = ALICE.getPhone();
        assertThrows(NullPointerException.class, () -> new Task(null, "x", null, false, name, phone, null));
        assertThrows(NullPointerException.class, () -> new Task("id", null, null, false, name, phone, null));
        assertThrows(NullPointerException.class, () -> new Task("id", "x", null, false, null, phone, null));
        assertThrows(NullPointerException.class, () -> new Task("id", "x", null, false, name, null, null));
        assertThrows(IllegalArgumentException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS, ()
                -> new Task("id", " ", null, false, name, phone, null));
        assertThrows(IllegalArgumentException.class, () -> new Task(" ", "x", null, false, name, phone, null));
    }

    @Test
    public void create_assignsDistinctIds() {
        Task first = Task.create("Same text", null, ALICE, null);
        Task second = Task.create("Same text", null, ALICE, null);

        assertNotEquals(first.getId(), second.getId());
        assertNotEquals(first, second); // two tasks with the same text are allowed and stay distinct
        assertFalse(first.isDone());
    }

    @Test
    public void descriptionIsStripped() {
        assertEquals("padded", new TaskBuilder().withDescription("  padded  ").build().getDescription());
    }

    @Test
    public void copies_changeOnlyTheirOwnField() {
        LocalDate due = LocalDate.of(2026, 12, 1);
        Task task = new TaskBuilder().withDue(due).build();

        assertEquals("New", task.withDescription("New").getDescription());
        assertEquals(due, task.withDescription("New").getDue().get());
        assertTrue(task.withDue(null).getDue().isEmpty());
        assertTrue(task.markDone().isDone());
        assertEquals(task.getId(), task.markDone().getId());
        assertEquals(due, task.markDone().getDue().get());
    }

    @Test
    public void isAbout_studentAndLesson() {
        Task studentTask = new TaskBuilder().withStudent(ALICE).build();
        Task lessonTask = new TaskBuilder().withLesson(ALICE_MATH).build();

        assertTrue(studentTask.isAbout(ALICE));
        assertFalse(studentTask.isAbout(BENSON));
        assertFalse(studentTask.isAbout(ALICE_MATH)); // a task about a student is not about their lessons
        assertTrue(lessonTask.isAbout(ALICE_MATH));
        assertTrue(lessonTask.isAbout(ALICE)); // but a task about a lesson is also about the student
        assertFalse(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withDate("2030-01-01").build()));
        assertFalse(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withSubject("Science").build()));
        assertFalse(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withStudent(BENSON).build()));
        assertTrue(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).build().cancel("Cancelled")));
    }

    @Test
    public void withStudentAndWithLesson_relinkTheTask() {
        Task studentTask = new TaskBuilder().withStudent(ALICE).build();
        Task renamed = studentTask.withStudent(new PersonBuilder(ALICE).withName("Alice Tan").build());
        assertEquals("Alice Tan", renamed.getStudentName().toString());
        assertEquals(studentTask.getId(), renamed.getId());

        Task lessonTask = new TaskBuilder().withLesson(ALICE_MATH).build();
        var moved = new LessonBuilder(ALICE_MATH).withDate("2031-05-04").withTime("09:00").build();
        Task followed = lessonTask.withLesson(moved);
        assertTrue(followed.isAbout(moved));
        assertFalse(followed.isAbout(ALICE_MATH));
    }

    @Test
    public void equalsAndHashCode() {
        Task task = new TaskBuilder().build();

        assertEquals(task, new TaskBuilder().build());
        assertEquals(task.hashCode(), new TaskBuilder().build().hashCode());
        assertEquals(task, task);
        assertNotEquals(task, null);
        assertNotEquals(task, "task");
        assertNotEquals(task, new TaskBuilder().withId("other").build());
        assertNotEquals(task, new TaskBuilder().withDescription("Other").build());
        assertNotEquals(task, new TaskBuilder().withDue(LocalDate.of(2026, 1, 1)).build());
        assertNotEquals(task, new TaskBuilder().done().build());
        assertNotEquals(task, new TaskBuilder().withStudent(BENSON).build());
        assertNotEquals(task, new TaskBuilder().withLesson(ALICE_MATH).build());
    }

    @Test
    public void toStringMethod() {
        Task task = new TaskBuilder().build();
        String expected = Task.class.getCanonicalName() + "{id=task-1, description=Call parent, due=null, "
                + "isDone=false, studentName=" + ALICE.getName() + ", studentPhone=" + ALICE.getPhone()
                + ", lessonLink=null}";
        assertEquals(expected, task.toString());
    }

    @Test
    public void lessonLink_matchesSlotButNotStudent() {
        TaskLessonLink link = TaskLessonLink.of(ALICE_MATH);

        assertTrue(link.matches(ALICE_MATH));
        assertTrue(link.matches(new LessonBuilder(ALICE_MATH).withStudent(BENSON).build()));
        assertFalse(link.matches(new LessonBuilder(ALICE_MATH).withDuration(60).build()));
        assertEquals(link, TaskLessonLink.of(ALICE_MATH));
        assertEquals(link.hashCode(), TaskLessonLink.of(ALICE_MATH).hashCode());
        assertNotEquals(link, null);
        assertThrows(NullPointerException.class, () -> TaskLessonLink.of(null));
    }
}
