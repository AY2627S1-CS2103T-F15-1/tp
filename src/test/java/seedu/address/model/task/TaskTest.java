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
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.homework.Homework;
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
        UUID student = ALICE.getId();
        assertThrows(NullPointerException.class, () -> new Task(null, "x", null, false, student, null, null));
        assertThrows(NullPointerException.class, () -> new Task("id", null, null, false, student, null, null));
        assertThrows(NullPointerException.class, () -> new Task("id", "x", null, false, null, null, null));
        assertThrows(IllegalArgumentException.class, Task.MESSAGE_DESCRIPTION_CONSTRAINTS, ()
                -> new Task("id", " ", null, false, student, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Task(" ", "x", null, false, student, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Task("id", "x", null, false, student,
                UUID.randomUUID(), UUID.randomUUID())); // a lesson and homework at once
    }

    @Test
    public void create_assignsDistinctIds() {
        Task first = Task.create("Same text", null, ALICE);
        Task second = Task.create("Same text", null, ALICE);

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
    public void isAbout_studentLessonAndHomework() {
        Task studentTask = new TaskBuilder().withStudent(ALICE).build();
        Task lessonTask = new TaskBuilder().withLesson(ALICE_MATH).build();
        Homework homework = Homework.assign(ALICE.getId(), "Practice set", LocalDate.of(2030, 1, 1));
        Task homeworkTask = new TaskBuilder().withHomework(homework).build();

        assertTrue(studentTask.isAbout(ALICE));
        assertFalse(studentTask.isAbout(BENSON));
        assertFalse(studentTask.isAbout(ALICE_MATH)); // a task about a student is not about their lessons
        assertFalse(studentTask.isAbout(homework));
        assertTrue(lessonTask.isAbout(ALICE_MATH));
        assertTrue(lessonTask.isAbout(ALICE)); // but a task about a lesson is also about the student
        assertFalse(lessonTask.isAbout(BENSON));
        assertTrue(homeworkTask.isAbout(homework));
        assertTrue(homeworkTask.isAbout(ALICE));
        assertFalse(homeworkTask.isAbout(ALICE_MATH));
        assertFalse(homeworkTask.isAbout(Homework.assign(ALICE.getId(), "Practice set", LocalDate.of(2030, 1, 1))));
    }

    @Test
    public void isAbout_lessonFollowsTheLessonIdNotTheSlotOrStatus() {
        Task lessonTask = new TaskBuilder().withLesson(ALICE_MATH).build();

        // the same lesson moved, renamed or cancelled is still the lesson the task is about
        assertTrue(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withDate("2030-01-01").build()));
        assertTrue(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withSubject("Science").build()));
        assertTrue(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).build().cancel("Cancelled")));
        // a different lesson in exactly the same slot, such as a rebooking, is not
        assertFalse(lessonTask.isAbout(new LessonBuilder(ALICE_MATH).withId(UUID.randomUUID()).build()));
    }

    @Test
    public void isAbout_studentFollowsTheStudentIdNotNameOrPhone() {
        Task studentTask = new TaskBuilder().withStudent(ALICE).build();

        assertTrue(studentTask.isAbout(new PersonBuilder(ALICE).withName("Alice Tan").build().withId(ALICE.getId())));
        assertFalse(studentTask.isAbout(new PersonBuilder(ALICE).build().withId(UUID.randomUUID())));
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
        assertNotEquals(task, new TaskBuilder().withHomework(
                Homework.assign(ALICE.getId(), "Practice set", LocalDate.of(2030, 1, 1))).build());
    }

    @Test
    public void toStringMethod() {
        Task task = new TaskBuilder().build();
        String expected = Task.class.getCanonicalName() + "{id=task-1, description=Call parent, due=null, "
                + "isDone=false, studentId=" + ALICE.getId() + ", lessonId=null, homeworkId=null}";
        assertEquals(expected, task.toString());
    }
}
