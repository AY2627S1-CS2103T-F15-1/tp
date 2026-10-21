package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/** Tests the strip that marks the demo, alone and inside the main window. */
public class DemoBannerTest {
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

    @Test
    public void constructor_nullAction_throwsNullPointerException() {
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> onFx(() -> new DemoBanner(null)));

        assertTrue(thrown.getCause() instanceof NullPointerException);
    }

    @Test
    public void banner_isHiddenAndTakesNoRoomUntilTheDemoRuns() throws Exception {
        DemoBanner banner = onFx(() -> new DemoBanner(() -> { }));

        onFx(() -> {
            assertFalse(banner.getRoot().isVisible());
            assertFalse(banner.getRoot().isManaged());
            banner.setDemoMode(true);
            assertTrue(banner.getRoot().isVisible());
            assertTrue(banner.getRoot().isManaged());
            banner.setDemoMode(false);
            assertFalse(banner.getRoot().isVisible());
            assertFalse(banner.getRoot().isManaged());
            return null;
        });
    }

    @Test
    public void banner_saysNothingIsSavedAndHasAnAccessibleExitButton() throws Exception {
        AtomicInteger exits = new AtomicInteger();
        DemoBanner banner = onFx(() -> new DemoBanner(exits::incrementAndGet));

        onFx(() -> {
            Label message = (Label) banner.getRoot().lookup("#message");
            Button button = (Button) banner.getRoot().lookup("#exitButton");
            assertEquals(DemoBanner.MESSAGE, message.getText());
            assertTrue(message.getText().contains("nothing here is saved"));
            assertEquals("Exit demo", button.getText());
            assertTrue(button.getAccessibleText() != null && !button.getAccessibleText().isBlank());
            assertTrue(button.getTooltip() != null);
            button.fire();
            return null;
        });

        assertEquals(1, exits.get());
    }

    @Test
    public void window_startsWithoutTheBannerWhenThereIsNoDemo() throws Exception {
        MainWindow window = fixture.open("");

        assertFalse(onFx(() -> window.getDemoBanner().getRoot().isVisible()));
    }

    @Test
    public void window_showsTheBannerWhileTheDemoRunsAndTheButtonBringsTheDataBack() throws Exception {
        MainWindow window = fixture.open("");

        fixture.type("demo confirm");
        assertTrue(onFx(() -> window.getDemoBanner().getRoot().isVisible()));
        assertTrue(fixture.logic().getFilteredPersonList().size() > 1);

        onFx(() -> {
            Button exit = (Button) fixture.find("#exitButton");
            exit.fire();
            return null;
        });

        assertEquals("Demo ended. Your own data is back.", fixture.resultBox());
        assertFalse(onFx(() -> window.getDemoBanner().getRoot().isVisible()));
        assertEquals(List.of(ALICE), fixture.logic().getFilteredPersonList());
    }
}
