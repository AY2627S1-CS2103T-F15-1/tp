package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.logic.view.HomeworkRow;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/** Tests what the cards of the students, lessons and homework show and what their buttons ask for. */
public class PanelCardsTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    private static List<Button> buttons(Region card) {
        return card.lookupAll("." + IconButton.STYLE_CLASS).stream().map(node -> (Button) node).toList();
    }

    private static List<String> descriptions(Region card) {
        return buttons(card).stream().map(Button::getAccessibleText).toList();
    }

    private static Button buttonStartingWith(Region card, String start) {
        return buttons(card).stream().filter(button -> button.getAccessibleText().startsWith(start)).findFirst()
                .orElseThrow();
    }

    private static Label label(Region card, String id) {
        return (Label) card.lookup(id);
    }

    @Test
    public void studentCard_withoutActions_hasNoButtons() throws Exception {
        PersonCard card = onFx(() -> new PersonCard(ALICE, 1));

        onFx(() -> {
            assertEquals(List.of(), buttons(card.getRoot()));
            return null;
        });
    }

    @Test
    public void studentCard_buttonsChangeArchiveAndDeleteTheStudentWithItsNumber() throws Exception {
        RecordingActions actions = new RecordingActions();
        PersonCard card = onFx(() -> new PersonCard(ALICE, 3, actions));

        onFx(() -> {
            assertEquals(List.of("Change student 3, Alice Pauline", "Archive student 3, Alice Pauline",
                    "Delete student 3, Alice Pauline"), descriptions(card.getRoot()));
            buttonStartingWith(card.getRoot(), "Change").fire();
            buttonStartingWith(card.getRoot(), "Archive").fire();
            buttonStartingWith(card.getRoot(), "Delete").fire();
            return null;
        });

        assertEquals(List.of("prefill edit 3 ", "run archive 3",
                "ask Delete student 3, Alice Pauline? -> delete 3"), actions.calls());
        assertFalse(onFx(() -> label(card.getRoot(), "#archivedBadge").isVisible()));
    }

    @Test
    public void studentCard_archivedStudent_offersRestoreAndShowsABadge() throws Exception {
        RecordingActions actions = new RecordingActions();
        Person archived = new PersonBuilder(ALICE).withArchived(true).build();
        PersonCard card = onFx(() -> new PersonCard(archived, 2, actions));

        onFx(() -> {
            assertEquals(List.of("Change student 2, Alice Pauline", "Restore student 2, Alice Pauline",
                    "Delete student 2, Alice Pauline"), descriptions(card.getRoot()));
            buttonStartingWith(card.getRoot(), "Restore").fire();
            assertTrue(label(card.getRoot(), "#archivedBadge").isVisible());
            assertTrue(label(card.getRoot(), "#archivedBadge").isManaged());
            return null;
        });

        assertEquals(List.of("run restore 2"), actions.calls());
    }

    @Test
    public void lessonCard_scheduledLesson_hasItsNumberDetailsAndButtons() throws Exception {
        RecordingActions actions = new RecordingActions();
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(2))
                .withNotes("Bring the workbook").build();
        LessonCard card = onFx(() -> new LessonCard(lesson, 2, actions));

        onFx(() -> {
            Region root = card.getRoot();
            assertEquals("2. ", label(root, "#number").getText());
            assertEquals("Alice Pauline — " + lesson.getSubject(), label(root, "#title").getText());
            assertEquals("scheduled", label(root, "#status").getText());
            assertTrue(label(root, "#slot").getText().contains(lesson.getTimeRange()));
            assertEquals("Notes: Bring the workbook", label(root, "#note").getText());
            assertEquals(List.of("Reschedule lesson 2 with Alice Pauline", "Cancel lesson 2 with Alice Pauline"),
                    descriptions(root));
            buttonStartingWith(root, "Reschedule").fire();
            buttonStartingWith(root, "Cancel").fire();
            return null;
        });

        assertEquals(List.of("prefill lesson move 2 ",
                "ask Cancel lesson 2 with Alice Pauline? -> lesson cancel 2"), actions.calls());
    }

    @Test
    public void lessonCard_cancelledLesson_isStruckThroughWithItsReasonAndHasNoButtons() throws Exception {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withStatus(LessonStatus.CANCELLED)
                .withCancelReason("Student unwell").build();
        LessonCard card = onFx(() -> new LessonCard(lesson, 1, new RecordingActions()));

        onFx(() -> {
            Region root = card.getRoot();
            assertEquals("cancelled", label(root, "#status").getText());
            assertEquals("Reason: Student unwell", label(root, "#note").getText());
            assertTrue(label(root, "#title").getStyleClass().contains(LessonCard.STYLE_CLASS_STRUCK));
            assertTrue(label(root, "#slot").getStyleClass().contains(LessonCard.STYLE_CLASS_STRUCK));
            assertEquals(List.of(), buttons(root));
            return null;
        });
    }

    @Test
    public void lessonCard_completedLessonWithoutNotes_hidesTheNoteAndHasNoButtons() throws Exception {
        Lesson lesson = new LessonBuilder().withStudent(ALICE).withStatus(LessonStatus.COMPLETED).build();
        LessonCard card = onFx(() -> new LessonCard(lesson, 1, new RecordingActions()));

        onFx(() -> {
            Node note = card.getRoot().lookup("#note");
            assertFalse(note.isVisible());
            assertFalse(note.isManaged());
            assertEquals(List.of(), buttons(card.getRoot()));
            return null;
        });
    }

    private static HomeworkRow row(int number, String status, boolean isOverdue, String feedback,
            boolean isDeleted) {
        return new HomeworkRow(number, "Alice Pauline", "Algebra Q1-10", LocalDate.of(2030, 1, 12), status,
                isOverdue, Optional.ofNullable(feedback), isDeleted);
    }

    @Test
    public void homeworkCard_showsTheRowAndItsButtonsCarryTheNumberAndCurrentDetails() throws Exception {
        RecordingActions actions = new RecordingActions();
        HomeworkCard card = onFx(() -> new HomeworkCard(row(4, "submitted", false, "Neat working", false), actions));

        onFx(() -> {
            Region root = card.getRoot();
            assertEquals("4. ", label(root, "#number").getText());
            assertEquals("Algebra Q1-10", label(root, "#title").getText());
            assertEquals("Alice Pauline", label(root, "#student").getText());
            assertEquals("Due 2030-01-12 — submitted", label(root, "#due").getText());
            assertEquals("Feedback: Neat working", label(root, "#feedback").getText());
            assertEquals(List.of("Change homework 4, Algebra Q1-10", "Delete homework 4, Algebra Q1-10"),
                    descriptions(root));
            buttonStartingWith(root, "Change").fire();
            buttonStartingWith(root, "Delete").fire();
            return null;
        });

        assertEquals(List.of("prefill hw edit 4 desc/Algebra Q1-10 due/2030-01-12",
                "ask Delete homework 4, Algebra Q1-10? -> hw delete 4"), actions.calls());
    }

    @Test
    public void homeworkCard_overdueRow_isDrawnAsAWarning() throws Exception {
        HomeworkCard card = onFx(() -> new HomeworkCard(row(1, "OVERDUE", true, null, false),
                new RecordingActions()));

        onFx(() -> {
            assertTrue(label(card.getRoot(), "#due").getStyleClass().contains(HomeworkCard.STYLE_CLASS_WARNING));
            Node feedback = card.getRoot().lookup("#feedback");
            assertFalse(feedback.isVisible());
            assertFalse(feedback.isManaged());
            return null;
        });
    }

    @Test
    public void homeworkCard_deletedRow_isStruckThroughAndHasNoButtons() throws Exception {
        HomeworkCard card = onFx(() -> new HomeworkCard(row(2, HomeworkRow.STATUS_DELETED, false, null, true),
                new RecordingActions()));

        onFx(() -> {
            Region root = card.getRoot();
            assertTrue(label(root, "#title").getStyleClass().contains(HomeworkCard.STYLE_CLASS_STRUCK));
            assertTrue(label(root, "#due").getText().endsWith(HomeworkRow.STATUS_DELETED));
            assertEquals(List.of(), buttons(root));
            return null;
        });
    }
}
