package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.awaitFx;
import static seedu.address.ui.FxTestSupport.onFx;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import seedu.address.logic.view.DetailView;
import seedu.address.testutil.LessonBuilder;

/** Tests the layout of the main window and what its detail pane shows, on the JavaFX application thread. */
public class MainWindowDetailPaneTest {
    private static final String LESSON_TODAY = "lesson add st/1 d/" + LocalDate.now() + " t/16:30";
    private static final String ADD_BOB = "add n/Bob Tan p/91234567 l/S3 s/Math r/50";

    @TempDir
    public Path temporaryFolder;

    private MainWindowFixture fixture;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @BeforeEach
    public void createFixture() {
        fixture = new MainWindowFixture(temporaryFolder);
    }

    @AfterEach
    public void closeWindow() throws Exception {
        fixture.close();
    }

    private MainWindow open(String startupWarning) throws Exception {
        return fixture.open(startupWarning);
    }

    private static void layout(MainWindow window) {
        MainWindowFixture.layout(window);
    }

    private Node find(MainWindow window, String selector) {
        return fixture.find(selector);
    }

    private String type(MainWindow window, String command) throws Exception {
        return fixture.type(command);
    }

    private String resultBox(MainWindow window) throws Exception {
        return fixture.resultBox();
    }

    private DetailView shown(MainWindow window) throws Exception {
        return onFx(() -> window.getDetailPane().getShown().orElseThrow());
    }

    @Test
    public void layout_listOnTheLeftPaneOnTheRightAndResultAndCommandBoxesUnderThem() throws Exception {
        MainWindow window = open("");

        onFx(() -> {
            SplitPane split = window.getSplitPane();
            assertEquals(List.of(window.getPanelTabs().getRoot().getParent(),
                    window.getDetailPane().getRoot().getParent()), split.getItems());
            Node list = split.getItems().get(0);
            Node pane = split.getItems().get(1);
            assertTrue(list.localToScene(list.getBoundsInLocal()).getMaxX()
                    <= pane.localToScene(pane.getBoundsInLocal()).getMinX() + 1);
            Bounds splitBounds = split.localToScene(split.getBoundsInLocal());
            Bounds resultBounds = find(window, "#resultDisplay").localToScene(
                    find(window, "#resultDisplay").getBoundsInLocal());
            Bounds commandBounds = find(window, "#commandTextField").localToScene(
                    find(window, "#commandTextField").getBoundsInLocal());
            assertTrue(splitBounds.getMaxY() <= resultBounds.getMinY() + 1, "the result box is under the list");
            assertTrue(resultBounds.getMaxY() <= commandBounds.getMinY() + 1,
                    "the command box is under the result box");
            return null;
        });
    }

    @Test
    public void layout_smallestWindow_isSixHundredFortyByFourHundredEightyWithTheCommandBoxVisible()
            throws Exception {
        MainWindow window = open("");

        onFx(() -> {
            assertEquals(640, fixture.stage().getMinWidth(), 0.1);
            assertEquals(480, fixture.stage().getMinHeight(), 0.1);
            fixture.stage().setWidth(640);
            fixture.stage().setHeight(480);
            layout(window);
            Scene scene = fixture.stage().getScene();
            Node command = find(window, "#commandTextField");
            Bounds commandBounds = command.localToScene(command.getBoundsInLocal());
            assertTrue(commandBounds.getMaxY() <= scene.getHeight() + 0.5,
                    "the command box is inside the window: " + commandBounds + " in " + scene.getHeight());
            assertTrue(commandBounds.getHeight() > 10);
            return null;
        });
    }

    @Test
    public void layout_dividerBetweenListAndPane_canBeDragged() throws Exception {
        MainWindow window = open("");

        double moved = onFx(() -> {
            window.getSplitPane().setDividerPositions(0.7);
            layout(window);
            return window.getSplitPane().getDividerPositions()[0];
        });

        assertEquals(0.7, moved, 0.05);
    }

    @Test
    public void startup_showsTheDashboardInThePaneAndNamesItInTheResultBox() throws Exception {
        MainWindow window = open("");

        DetailView dashboard = shown(window);

        assertTrue(dashboard.isLive());
        assertEquals(fixture.logic().getDashboardView(), dashboard);
        assertEquals(dashboard.title() + "\nShown in the detail pane.", resultBox(window));
    }

    @Test
    public void startup_withAWarning_putsTheWarningBeforeTheNameOfTheDashboard() throws Exception {
        MainWindow window = open("Welcome to TutorFlow.");

        assertEquals("Welcome to TutorFlow.\n\n" + shown(window).title() + "\nShown in the detail pane.",
                resultBox(window));
    }

    @Test
    public void agendaAndView_replaceTheDashboardAndAreNotRedrawnByLaterCommands() throws Exception {
        MainWindow window = open("");

        String agendaResult = type(window, "agenda week/");
        DetailView agenda = shown(window);
        assertTrue(agenda.title().startsWith("Week of "));
        assertEquals(agenda.title() + "\nShown in the detail pane.", agendaResult);
        assertFalse(agenda.isLive());

        type(window, LESSON_TODAY);
        assertEquals(agenda, shown(window), "an agenda shows the data as it was when it was requested");

        assertEquals("Viewing: Alice Pauline\nShown in the detail pane.", type(window, "view 1"));
        assertEquals("Viewing: Alice Pauline", shown(window).title());
    }

    @Test
    public void dashboard_isRedrawnAfterEveryCommandWhileItIsShown() throws Exception {
        MainWindow window = open("");
        assertTrue(shown(window).sections().getFirst().heading().startsWith("TODAY'S LESSONS (0"));

        type(window, LESSON_TODAY);

        assertTrue(shown(window).sections().getFirst().heading().startsWith("TODAY'S LESSONS (1"),
                shown(window).sections().getFirst().heading());
        assertTrue(resultBox(window).startsWith("Scheduled: Alice Pauline"), resultBox(window));
    }

    @Test
    public void dashboard_isRedrawnAfterAFailedCommandToo() throws Exception {
        MainWindow window = open("");
        fixture.failWrites(true);

        String result = type(window, LESSON_TODAY);

        assertTrue(result.contains("could not be saved"), result);
        assertTrue(shown(window).sections().getFirst().heading().startsWith("TODAY'S LESSONS (1"));
    }

    @Test
    public void today_bringsTheDashboardBackAndKeepsItLive() throws Exception {
        MainWindow window = open("");
        type(window, "agenda");

        type(window, "today");
        assertTrue(shown(window).isLive());
        type(window, LESSON_TODAY);

        assertTrue(shown(window).sections().getFirst().heading().startsWith("TODAY'S LESSONS (1"));
    }

    @Test
    public void unsavedChanges_warningStaysInTheResultBoxWhileAViewIsShown() throws Exception {
        MainWindow window = open("");
        fixture.failWrites(true);
        type(window, ADD_BOB);

        String result = type(window, "today");

        assertTrue(result.endsWith("Unsaved changes: type 'save' to retry before closing TutorFlow."), result);
        assertTrue(result.contains("Shown in the detail pane."));
    }

    @Test
    public void pageKeysAndControlArrows_scrollThePaneFromTheCommandBox() throws Exception {
        MainWindow window = open("");
        for (int i = 0; i < 60; i++) {
            fixture.model().addLesson(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now())
                    .withTime(String.format("%02d:%02d", 6 + i / 4, (i % 4) * 15)).withDuration(15).build());
        }
        type(window, "today");

        double[] positions = onFx(() -> {
            ScrollPane scroll = (ScrollPane) window.getDetailPane().getRoot();
            TextField input = (TextField) find(window, "#commandTextField");
            input.requestFocus();
            assertTrue(scroll.getViewportBounds().getHeight() > 0);
            input.fireEvent(key(KeyCode.PAGE_DOWN, false));
            double afterPage = scroll.getVvalue();
            input.fireEvent(key(KeyCode.PAGE_UP, false));
            double backAtTop = scroll.getVvalue();
            input.fireEvent(key(KeyCode.DOWN, true));
            double afterLines = scroll.getVvalue();
            input.fireEvent(key(KeyCode.UP, true));
            double afterLinesBack = scroll.getVvalue();
            input.fireEvent(key(KeyCode.DOWN, false));
            return new double[] {afterPage, backAtTop, afterLines, afterLinesBack, scroll.getVvalue()};
        });

        assertTrue(positions[0] > 0, "page down scrolls");
        assertEquals(0, positions[1], 0.0001);
        assertTrue(positions[2] > 0 && positions[2] < positions[0], "ctrl+down scrolls less than a page");
        assertEquals(0, positions[3], 0.0001);
        assertEquals(0, positions[4], 0.0001, "down alone does not scroll the pane");
    }

    private static KeyEvent key(KeyCode code, boolean isControlDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, isControlDown, false, false);
    }

    @Test
    public void addedStudent_isSelectedScrolledToAndHighlightedForAWhile() throws Exception {
        MainWindow window = open("");
        onFx(() -> {
            window.getPersonListPanel().getHighlightTimer().setDuration(Duration.millis(300));
            return null;
        });

        type(window, ADD_BOB);

        PersonListPanel list = window.getPersonListPanel();
        List<String> names = onFx(() -> list.getSelectedStudents().stream()
                .map(student -> student.getName().fullName).toList());
        assertEquals(List.of("Bob Tan"), names);
        assertEquals(1, onFx(() -> list.getHighlightedIds().size()));
        assertEquals(1, onFx(() -> list.getRoot().lookupAll("." + PersonListPanel.HIGHLIGHT_STYLE_CLASS).size()));
        awaitFx(() -> list.getHighlightedIds().isEmpty());
        assertEquals(0, onFx(() -> list.getRoot().lookupAll("." + PersonListPanel.HIGHLIGHT_STYLE_CLASS).size()));
        assertEquals(1, onFx(() -> list.getSelectedStudents().size()), "the student stays selected");
    }

    @Test
    public void commandsThatAddNobody_doNotHighlightAnything() throws Exception {
        MainWindow window = open("");

        type(window, "list");
        type(window, "find Alice");

        assertEquals(0, onFx(() -> window.getPersonListPanel().getHighlightedIds().size()));
    }
}
