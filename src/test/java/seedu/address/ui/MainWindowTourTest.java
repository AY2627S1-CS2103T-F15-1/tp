package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.awaitFx;
import static seedu.address.ui.FxTestSupport.onFx;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;

/** Tests when the guided tour opens over the main window, what it offers and what it leaves behind. */
public class MainWindowTourTest {
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

    private MainWindow openOnFirstRun() throws Exception {
        MainWindow window = fixture.open(new AddressBook(), new UserPrefs(), "");
        awaitFx(() -> tourOverlay(window) != null);
        return window;
    }

    private static Node tourOverlay(MainWindow window) {
        return window.getRoot().getScene().lookup(".tour-card");
    }

    private boolean isTourShowing() throws Exception {
        return onFx(() -> {
            Node card = fixture.find(".tour-card");
            return card != null && card.getParent().isVisible();
        });
    }

    private String tourTitle() throws Exception {
        return onFx(() -> ((Label) fixture.find(".tour-card").lookup("#title")).getText());
    }

    private void press(String id) throws Exception {
        onFx(() -> {
            Button button = (Button) fixture.find(id);
            button.fire();
            return null;
        });
    }

    @Test
    public void firstRun_opensTheTourAtOnceOnACleanSlate() throws Exception {
        MainWindow window = openOnFirstRun();

        assertTrue(isTourShowing());
        assertEquals(0, fixture.logic().getFilteredPersonList().size());
        assertFalse(fixture.logic().isTourSeen(), "a tour that is on screen has not been seen to the end");
        assertEquals("Welcome to TutorFlow", tourTitle());
        assertEquals(PanelTabs.Panel.STUDENTS, onFx(() -> window.getPanelTabs().getSelected()));
    }

    @Test
    public void skip_closesTheTourAndRemembersItWasSeen() throws Exception {
        openOnFirstRun();

        press("#skipButton");

        assertFalse(isTourShowing());
        assertTrue(fixture.logic().isTourSeen());
        assertTrue(fixture.model().getUserPrefs().isTourSeen());
    }

    @Test
    public void escape_leavesTheTourAndFocusGoesBackToTheCommandBox() throws Exception {
        openOnFirstRun();

        onFx(() -> {
            Event.fireEvent(fixture.find("#commandTextField"), new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
                    KeyCode.ESCAPE, false, false, false, false));
            return null;
        });

        assertFalse(isTourShowing());
        assertTrue(fixture.logic().isTourSeen());
        assertTrue(onFx(() -> fixture.find("#commandTextField").isFocused()));
    }

    @Test
    public void alreadySeen_doesNotOpenTheTourAtStart() throws Exception {
        fixture.open("");

        assertFalse(isTourShowing());
    }

    @Test
    public void tourCommand_opensTheTourEvenAfterItWasSeenAndEndingItAgainChangesNothingElse() throws Exception {
        fixture.open("");

        String result = fixture.type("tour");

        assertEquals("Starting the guided tour. Press Esc at any time to leave it.", result);
        assertTrue(isTourShowing());
        assertEquals(1, fixture.logic().getFilteredPersonList().size());
        press("#skipButton");
        assertFalse(isTourShowing());
        assertTrue(fixture.logic().isTourSeen());
    }

    @Test
    public void tourCommand_whileTheTourIsOnScreen_doesNotStackAnotherTour() throws Exception {
        fixture.open("");
        fixture.type("tour");

        fixture.type("tour");

        assertEquals(1, onFx(() -> fixture.find(".tour-card").getScene().getRoot().lookupAll(".tour-card").size()));
    }

    @Test
    public void helpMenu_hasAGuidedTourItemThatOpensTheTour() throws Exception {
        fixture.open("");

        onFx(() -> {
            MenuBar bar = (MenuBar) fixture.find("#menuBar");
            MenuItem item = bar.getMenus().get(1).getItems().stream()
                    .filter(candidate -> "Guided tour".equals(candidate.getText())).findFirst().orElseThrow();
            item.fire();
            return null;
        });

        assertTrue(isTourShowing());
    }

    @Test
    public void exampleButton_putsTheExampleInTheCommandBoxWithoutRunningItAndTheTourStaysOpen() throws Exception {
        openOnFirstRun();
        press("#nextButton");
        press("#nextButton");

        press("#exampleButton");

        TextField input = (TextField) fixture.find("#commandTextField");
        assertEquals("add n/Alex Tan p/91234567 l/S3 s/Math r/50", onFx(input::getText));
        assertEquals(0, fixture.logic().getFilteredPersonList().size(), "nothing runs until the user presses Enter");
        assertTrue(isTourShowing());
        assertTrue(onFx(input::isFocused));
    }

    @Test
    public void tourStaysOpenWhileTheUserRunsTheExampleAndKeepsWhatTheyTyped() throws Exception {
        openOnFirstRun();
        press("#nextButton");
        press("#nextButton");
        press("#exampleButton");

        fixture.type("add n/Alex Tan p/91234567 l/S3 s/Math r/50");

        assertEquals(1, fixture.logic().getFilteredPersonList().size());
        assertTrue(isTourShowing());
        press("#skipButton");
        assertEquals(1, fixture.logic().getFilteredPersonList().size(), "leaving the tour keeps the student");
    }

    @Test
    public void finishing_afterAllStepsRemembersTheTourAndLeavesTheCommandBoxAsItWas() throws Exception {
        openOnFirstRun();
        onFx(() -> {
            TextField input = (TextField) fixture.find("#commandTextField");
            input.setText("half-typed");
            return null;
        });
        int steps = GuidedTour.steps(LocalDate.now()).size();

        for (int i = 1; i < steps; i++) {
            press("#nextButton");
        }
        assertEquals("Finish", onFx(() -> ((Button) fixture.find("#nextButton")).getText()));
        press("#nextButton");

        assertFalse(isTourShowing());
        assertTrue(fixture.logic().isTourSeen());
        assertEquals("half-typed", onFx(() -> ((TextField) fixture.find("#commandTextField")).getText()));
    }

    @Test
    public void everyStep_putsTheCardInsideTheWindowAtTheSmallestSize() throws Exception {
        MainWindow window = openOnFirstRun();
        onFx(() -> {
            fixture.stage().setWidth(640);
            fixture.stage().setHeight(480);
            return null;
        });
        awaitFx(() -> fixture.stage().getScene().getHeight() <= 480 && fixture.stage().getScene().getWidth() <= 640);
        int steps = GuidedTour.steps(LocalDate.now()).size();

        for (int i = 0; i < steps; i++) {
            double[] box = onFx(() -> {
                MainWindowFixture.layout(window);
                Node card = fixture.find(".tour-card");
                Bounds overlay = card.getParent().getLayoutBounds();
                return new double[] {card.getLayoutX(), card.getLayoutY(), card.getLayoutBounds().getWidth(),
                    card.getLayoutBounds().getHeight(), overlay.getWidth(), overlay.getHeight()};
            });
            String where = "step " + (i + 1) + ": " + Arrays.toString(box);
            assertTrue(box[0] >= 0 && box[0] + box[2] <= box[4] + 0.5, where);
            assertTrue(box[1] >= 0 && box[1] + box[3] <= box[5] + 0.5, where);
            if (i < steps - 1) {
                press("#nextButton");
            }
        }
    }
}
