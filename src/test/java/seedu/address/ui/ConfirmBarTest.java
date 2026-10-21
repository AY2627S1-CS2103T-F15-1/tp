package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ConfirmBarTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    private static Button button(ConfirmBar bar, String id) {
        return (Button) bar.getRoot().lookup(id);
    }

    private static void assertConstructionFails(PanelActions actions, Runnable onCancel) {
        Callable<ConfirmBar> create = () -> new ConfirmBar(actions, onCancel);

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> onFx(create));

        assertTrue(thrown.getCause() instanceof NullPointerException);
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertConstructionFails(null, () -> { });
        assertConstructionFails(new RecordingActions(), null);
    }

    @Test
    public void bar_isHiddenAndTakesNoRoomUntilItAsks() throws Exception {
        ConfirmBar bar = onFx(() -> new ConfirmBar(new RecordingActions(), () -> { }));

        onFx(() -> {
            assertFalse(bar.isAsking());
            assertFalse(bar.getRoot().isVisible());
            assertFalse(bar.getRoot().isManaged());
            assertEquals(Optional.empty(), bar.getCommand());
            return null;
        });
    }

    @Test
    public void ask_showsTheQuestionAndOffersTheCommand() throws Exception {
        ConfirmBar bar = onFx(() -> new ConfirmBar(new RecordingActions(), () -> { }));

        onFx(() -> {
            bar.ask("Delete student 1?", "delete 1");
            assertTrue(bar.isAsking());
            assertTrue(bar.getRoot().isVisible());
            assertTrue(bar.getRoot().isManaged());
            assertEquals(Optional.of("delete 1"), bar.getCommand());
            assertEquals("Delete student 1?", ((Label) bar.getRoot().lookup("#question")).getText());
            assertEquals("Confirm. Runs: delete 1", button(bar, "#confirmButton").getAccessibleText());
            return null;
        });
    }

    @Test
    public void confirm_runsTheCommandOnceAndHidesTheBar() throws Exception {
        RecordingActions actions = new RecordingActions();
        ConfirmBar bar = onFx(() -> new ConfirmBar(actions, () -> { }));

        onFx(() -> {
            bar.ask("Delete student 1?", "delete 1");
            button(bar, "#confirmButton").fire();
            button(bar, "#confirmButton").fire();
            return null;
        });

        assertEquals(List.of("run delete 1"), actions.calls());
        assertFalse(onFx(bar::isAsking));
    }

    @Test
    public void cancel_hidesTheBarWithoutRunningAnything() throws Exception {
        RecordingActions actions = new RecordingActions();
        AtomicInteger cancels = new AtomicInteger();
        ConfirmBar bar = onFx(() -> new ConfirmBar(actions, cancels::incrementAndGet));

        onFx(() -> {
            bar.ask("Delete student 1?", "delete 1");
            button(bar, "#cancelButton").fire();
            return null;
        });

        assertEquals(List.of(), actions.calls());
        assertEquals(1, cancels.get());
        assertFalse(onFx(bar::isAsking));
    }

    @Test
    public void ask_nullArgument_throwsNullPointerException() throws Exception {
        ConfirmBar bar = onFx(() -> new ConfirmBar(new RecordingActions(), () -> { }));

        assertThrows(NullPointerException.class, () -> bar.ask(null, "delete 1"));
        assertThrows(NullPointerException.class, () -> bar.ask("Delete?", null));
    }
}
