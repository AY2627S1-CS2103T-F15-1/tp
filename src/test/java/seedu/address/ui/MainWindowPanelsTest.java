package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.view.HomeworkRow;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.LessonBuilder;

/** Tests the tabs of students, lessons and homework in the main window, and the buttons on their rows. */
public class MainWindowPanelsTest {
    @TempDir
    public Path temporaryFolder;

    private MainWindowFixture fixture;
    private MainWindow window;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @BeforeEach
    public void openWindow() throws Exception {
        fixture = new MainWindowFixture(temporaryFolder);
        window = fixture.open("");
    }

    @AfterEach
    public void closeWindow() throws Exception {
        fixture.close();
    }

    private PanelTabs.Panel selected() throws Exception {
        return onFx(() -> window.getPanelTabs().getSelected());
    }

    private Button findButton(String description) {
        MainWindowFixture.layout(window);
        return window.getRoot().getScene().getRoot().lookupAll("." + IconButton.STYLE_CLASS).stream()
                .map(node -> (Button) node).filter(button -> description.equals(button.getAccessibleText()))
                .findFirst().orElseThrow(() -> new AssertionError("No button: " + description));
    }

    private void press(String description) throws Exception {
        onFx(() -> {
            findButton(description).fire();
            return null;
        });
    }

    private List<String> iconDescriptions() throws Exception {
        return onFx(() -> {
            MainWindowFixture.layout(window);
            return window.getRoot().getScene().getRoot().lookupAll("." + IconButton.STYLE_CLASS).stream()
                    .map(node -> ((Button) node).getAccessibleText()).toList();
        });
    }

    private void pressQuick(String label) throws Exception {
        onFx(() -> {
            Button found = window.getRoot().getScene().getRoot().lookupAll("." + QuickCommand.STYLE_CLASS).stream()
                    .map(node -> (Button) node).filter(button -> label.equals(button.getText())).findFirst()
                    .orElseThrow();
            found.fire();
            return null;
        });
    }

    private void confirmBarConfirm() throws Exception {
        onFx(() -> {
            Button confirm = (Button) window.getConfirmBar().getRoot().lookup("#confirmButton");
            confirm.fire();
            return null;
        });
    }

    @Test
    public void tabs_areStudentsLessonsAndHomeworkInThatOrderWithStudentsShownFirst() throws Exception {
        List<String> titles = onFx(() -> ((TabPane) window.getPanelTabs().getRoot()).getTabs().stream()
                .map(tab -> tab.getText()).toList());

        assertEquals(List.of("Students", "Lessons", "Homework"), titles);
        assertEquals(PanelTabs.Panel.STUDENTS, selected());
    }

    @Test
    public void shortcuts_controlOneTwoAndThreeShowTheTabs() throws Exception {
        onFx(() -> {
            for (KeyCode key : new KeyCode[] {KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT1}) {
                window.getRoot().getScene().getAccelerators()
                        .get(new KeyCodeCombination(key, KeyCombination.SHORTCUT_DOWN)).run();
            }
            return null;
        });

        assertEquals(PanelTabs.Panel.STUDENTS, selected());
        onFx(() -> {
            window.getRoot().getScene().getAccelerators()
                    .get(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN)).run();
            return null;
        });
        assertEquals(PanelTabs.Panel.HOMEWORK, selected());
    }

    @Test
    public void studentRows_haveButtonsAndTheEditButtonPutsTheCommandInTheCommandBox() throws Exception {
        assertEquals(List.of("Change student 1, Alice Pauline", "Archive student 1, Alice Pauline",
                "Delete student 1, Alice Pauline"), iconDescriptions());

        press("Change student 1, Alice Pauline");

        TextField input = (TextField) fixture.find("#commandTextField");
        assertEquals("edit 1 ", onFx(input::getText));
        assertEquals(7, onFx(input::getCaretPosition));
        assertTrue(onFx(input::isFocused));
    }

    @Test
    public void deleteButton_asksFirstAndOnlyThenDeletesTheStudent() throws Exception {
        press("Delete student 1, Alice Pauline");

        assertEquals(Optional.of("delete 1"), onFx(() -> window.getConfirmBar().getCommand()));
        assertEquals(1, fixture.logic().getFilteredPersonList().size(), "nothing is deleted by the first press");

        confirmBarConfirm();

        assertEquals(0, fixture.logic().getFilteredPersonList().size());
        assertFalse(onFx(() -> window.getConfirmBar().isAsking()));
        assertTrue(fixture.resultBox().startsWith("Deleted"), fixture.resultBox());
    }

    @Test
    public void deleteButton_ofAStudentWithLessons_goesOnToAskAgainWithTheStructuredConfirmation()
            throws Exception {
        fixture.model().addLesson(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(3))
                .build());
        press("Delete student 1, Alice Pauline");

        confirmBarConfirm();

        assertEquals(1, fixture.logic().getFilteredPersonList().size(), "the preview deletes nothing");
        assertTrue(fixture.resultBox().contains("delete 1 confirm"), fixture.resultBox());
        assertEquals(Optional.of("delete 1 confirm"), onFx(() -> window.getConfirmBar().getCommand()));

        confirmBarConfirm();

        assertEquals(0, fixture.logic().getFilteredPersonList().size());
        assertFalse(onFx(() -> window.getConfirmBar().isAsking()));
    }

    @Test
    public void escape_cancelsTheQuestionAndSaysNothingWasChanged() throws Exception {
        press("Delete student 1, Alice Pauline");

        onFx(() -> {
            Event.fireEvent(fixture.find("#commandTextField"), new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
                    KeyCode.ESCAPE, false, false, false, false));
            return null;
        });

        assertFalse(onFx(() -> window.getConfirmBar().isAsking()));
        assertEquals(ConfirmBar.MESSAGE_CANCELLED, fixture.resultBox());
        assertEquals(1, fixture.logic().getFilteredPersonList().size());
    }

    @Test
    public void anyOtherCommand_takesTheQuestionAway() throws Exception {
        press("Delete student 1, Alice Pauline");

        fixture.type("list");

        assertFalse(onFx(() -> window.getConfirmBar().isAsking()));
    }

    @Test
    public void archiveButton_archivesAtOnceAndTheRowIsRestoredFromTheAllList() throws Exception {
        press("Archive student 1, Alice Pauline");
        assertEquals(0, fixture.logic().getFilteredPersonList().size());

        pressQuick("Archived");
        assertEquals(List.of("Change student 1, Alice Pauline", "Restore student 1, Alice Pauline",
                "Delete student 1, Alice Pauline"), iconDescriptions());
        press("Restore student 1, Alice Pauline");

        pressQuick("Active");
        assertEquals(1, fixture.logic().getFilteredPersonList().size());
        assertFalse(fixture.logic().getFilteredPersonList().get(0).isArchived());
    }

    @Test
    public void lessonsTab_isEmptyAtStartupAndAgendaFillsItWithNumberedRowsAndShowsTheTab() throws Exception {
        assertEquals(0, onFx(() -> window.getLessonListPanel().getListView().getItems().size()));
        fixture.model().addLesson(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build());

        pressQuick("Today");

        assertEquals(PanelTabs.Panel.LESSONS, selected());
        assertEquals(1, onFx(() -> window.getLessonListPanel().getListView().getItems().size()));
        assertTrue(iconDescriptions().contains("Cancel lesson 1 with Alice Pauline"), iconDescriptions().toString());
    }

    @Test
    public void cancelButton_ofALesson_asksThenCancelsItAndKeepsTheRowStruckThrough() throws Exception {
        fixture.model().addLesson(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build());
        fixture.type("agenda");

        press("Cancel lesson 1 with Alice Pauline");
        assertEquals(Optional.of("lesson cancel 1"), onFx(() -> window.getConfirmBar().getCommand()));
        confirmBarConfirm();

        assertTrue(fixture.logic().getFilteredLessonList().get(0).isCancelled());
        assertFalse(iconDescriptions().contains("Cancel lesson 1 with Alice Pauline"));
    }

    @Test
    public void rescheduleButton_putsTheLessonMoveCommandInTheCommandBox() throws Exception {
        fixture.model().addLesson(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now()).build());
        fixture.type("agenda");

        press("Reschedule lesson 1 with Alice Pauline");

        assertEquals("lesson move 1 ", onFx(() -> ((TextField) fixture.find("#commandTextField")).getText()));
    }

    @Test
    public void homeworkTab_isEmptyAtStartupAndHwListFillsItAndShowsTheTab() throws Exception {
        Homework item = new HomeworkBuilder(ALICE).withDescription("Fractions").build();
        fixture.model().addHomework(item);
        assertEquals(0, onFx(() -> window.getHomeworkListPanel().getListView().getItems().size()));

        pressQuick("All");

        assertEquals(PanelTabs.Panel.HOMEWORK, selected());
        List<HomeworkRow> rows = onFx(() -> List.copyOf(window.getHomeworkListPanel().getListView().getItems()));
        assertEquals(1, rows.size());
        assertEquals("Fractions", rows.get(0).description());
        assertTrue(iconDescriptions().contains("Delete homework 1, Fractions"), iconDescriptions().toString());
    }

    @Test
    public void homeworkRows_showTheCurrentStatusAfterACommandChangesIt() throws Exception {
        fixture.model().addHomework(new HomeworkBuilder(ALICE).withDescription("Fractions").build());
        fixture.type("hw list");

        fixture.type("hw status 1 s/submitted");

        List<HomeworkRow> rows = onFx(() -> List.copyOf(window.getHomeworkListPanel().getListView().getItems()));
        assertEquals("submitted", rows.get(0).status());
    }

    @Test
    public void deleteButton_ofHomeworkWithFeedback_asksTwiceAndLeavesADeletedRow() throws Exception {
        fixture.model().addHomework(new HomeworkBuilder(ALICE).withDescription("Fractions")
                .withStatus(HomeworkStatus.SUBMITTED).withFeedback("Neat").build());
        fixture.type("hw list");

        press("Delete homework 1, Fractions");
        confirmBarConfirm();
        assertEquals(Optional.of("hw delete 1 confirm"), onFx(() -> window.getConfirmBar().getCommand()));
        assertEquals(1, fixture.model().getAddressBook().getHomeworkList().size());
        confirmBarConfirm();

        assertEquals(0, fixture.model().getAddressBook().getHomeworkList().size());
        List<HomeworkRow> rows = onFx(() -> List.copyOf(window.getHomeworkListPanel().getListView().getItems()));
        assertTrue(rows.get(0).isDeleted(), "the row stays so that no number comes to mean another item");
        assertFalse(iconDescriptions().stream().anyMatch(text -> text.startsWith("Delete homework")));
    }

    @Test
    public void editButton_ofHomework_putsTheCurrentDetailsInTheCommandBox() throws Exception {
        fixture.model().addHomework(new HomeworkBuilder(ALICE).withDescription("Fractions")
                .withDueDate(LocalDate.now().plusDays(5)).build());
        fixture.type("hw list");

        press("Change homework 1, Fractions");

        assertEquals("hw edit 1 desc/Fractions due/" + LocalDate.now().plusDays(5),
                onFx(() -> ((TextField) fixture.find("#commandTextField")).getText()));
    }

    @Test
    public void addingAStudentWhileAnotherTabIsShown_bringsTheStudentsTabBack() throws Exception {
        onFx(() -> {
            window.getPanelTabs().select(PanelTabs.Panel.HOMEWORK);
            return null;
        });

        fixture.type("add n/Bob Tan p/91234567 l/S3 s/Math r/50");

        assertEquals(PanelTabs.Panel.STUDENTS, selected());
    }

    @Test
    public void toolbars_runTheCommandsTheirTooltipsName() throws Exception {
        pressQuick("Overdue");

        Node toolbarButton = onFx(() -> window.getRoot().getScene().getRoot()
                .lookupAll("." + QuickCommand.STYLE_CLASS).stream()
                .filter(node -> "Overdue".equals(((Button) node).getText())).findFirst().orElseThrow());
        assertEquals("Same as typing: hw list overdue/", onFx(() -> ((Button) toolbarButton).getTooltip().getText()));
        assertEquals(PanelTabs.Panel.HOMEWORK, selected());
    }
}
