package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class TourOverlayTest {
    private static final List<TourStep> STEPS = List.of(
            TourStep.of("First", "Welcome."),
            TourStep.of("Second", "Look here.").pointingAt("#target").withExample("Try it", "help"),
            TourStep.of("Third", "Nothing to point at here.").pointingAt("#missing"),
            TourStep.of("Last", "Done."));

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    /** A window of 800 by 600 with a label as the target, and an overlay over it. */
    private static final class Setup {
        final StackPane root = new StackPane();
        final Pane content = new Pane();
        final Label target = new Label("target");
        final List<String> examples = new ArrayList<>();
        final AtomicInteger closed = new AtomicInteger();
        final TourOverlay tour;

        Setup(List<TourStep> steps) {
            target.setId("target");
            content.setPrefSize(800, 600);
            target.relocate(100, 450);
            content.getChildren().add(target);
            tour = new TourOverlay(steps, selector -> Optional.ofNullable(root.lookup(selector)), examples::add,
                    closed::incrementAndGet);
            root.getChildren().addAll(content, tour.getRoot());
            Scene scene = new Scene(root, 800, 600);
            assertTrue(scene.getWidth() > 0);
            layout();
        }

        void layout() {
            root.applyCss();
            root.layout();
        }

        Label label(String id) {
            return (Label) tour.getRoot().lookup(id);
        }

        Button button(String id) {
            return (Button) tour.getRoot().lookup(id);
        }

        Node card() {
            return tour.getRoot().lookup(".tour-card");
        }
    }

    private static void assertConstructionFails(List<TourStep> steps, Runnable onClosed) {
        Callable<TourOverlay> create = () -> new TourOverlay(steps, selector -> Optional.empty(), example -> { },
                onClosed);

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> onFx(create));

        assertTrue(thrown.getCause() instanceof NullPointerException);
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertConstructionFails(null, () -> { });
        assertConstructionFails(STEPS, null);
    }

    @Test
    public void start_showsTheFirstStepWithItsNumberAndNoBackButton() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            assertFalse(setup.tour.isShowing());
            assertEquals(0, setup.tour.getStepNumber());
            setup.tour.start();
            setup.layout();
            assertTrue(setup.tour.isShowing());
            assertEquals(1, setup.tour.getStepNumber());
            assertEquals(4, setup.tour.getStepCount());
            assertEquals("Step 1 of 4", setup.label("#progress").getText());
            assertEquals("First", setup.label("#title").getText());
            assertEquals("Welcome.", setup.label("#text").getText());
            assertTrue(setup.button("#backButton").isDisabled());
            assertEquals(TourOverlay.MESSAGE_NEXT, setup.button("#nextButton").getText());
            assertFalse(setup.button("#exampleButton").isVisible());
            return null;
        });
    }

    @Test
    public void next_andBack_moveBetweenTheStepsAndStopAtTheEnds() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.tour.back();
            assertEquals(1, setup.tour.getStepNumber(), "there is nothing before the first step");
            setup.tour.next();
            setup.tour.next();
            assertEquals(3, setup.tour.getStepNumber());
            assertFalse(setup.button("#backButton").isDisabled());
            setup.tour.back();
            assertEquals("Second", setup.label("#title").getText());
            return null;
        });
    }

    @Test
    public void lastStep_saysFinishHidesSkipAndClosesOnce() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.tour.next();
            setup.tour.next();
            setup.tour.next();
            assertEquals("Last", setup.label("#title").getText());
            assertEquals(TourOverlay.MESSAGE_FINISH, setup.button("#nextButton").getText());
            assertFalse(setup.button("#skipButton").isVisible());
            setup.tour.next();
            setup.tour.next();
            assertFalse(setup.tour.isShowing());
            assertEquals(0, setup.tour.getStepNumber());
            return null;
        });
        assertEquals(1, setup.closed.get(), "a finished tour reports once, however often Next is pressed");
    }

    @Test
    public void skip_closesOnceAndLaterMovesDoNothing() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.button("#skipButton").fire();
            setup.tour.skip();
            setup.tour.next();
            setup.tour.back();
            assertFalse(setup.tour.isShowing());
            return null;
        });
        assertEquals(1, setup.closed.get());
    }

    @Test
    public void escape_leavesTheTourFromAnywhereInTheWindow() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            Event.fireEvent(setup.target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false,
                    false, false));
            assertFalse(setup.tour.isShowing());
            return null;
        });
        assertEquals(1, setup.closed.get());
    }

    @Test
    public void escapeAfterTheTourEnded_isNoLongerHandled() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        boolean isConsumed = onFx(() -> {
            setup.tour.start();
            setup.tour.skip();
            KeyEvent escape = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
            Event.fireEvent(setup.target, escape);
            return escape.isConsumed();
        });

        assertFalse(isConsumed);
        assertEquals(1, setup.closed.get());
    }

    @Test
    public void exampleButton_isShownOnlyForAStepWithAnExampleAndPutsItInTheCommandBox() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.tour.next();
            Button example = setup.button("#exampleButton");
            assertTrue(example.isVisible());
            assertTrue(example.isManaged());
            assertEquals("Try it", example.getText());
            assertTrue(example.getAccessibleText().contains("Nothing runs until you press Enter"));
            example.fire();
            return null;
        });

        assertEquals(List.of("help"), setup.examples);
        assertTrue(onFx(() -> setup.tour.isShowing()), "trying an example does not end the step");
    }

    @Test
    public void card_staysInsideTheWindowForEveryStep() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            for (int i = 0; i < STEPS.size(); i++) {
                setup.layout();
                Bounds card = setup.card().getBoundsInParent();
                assertTrue(card.getMinX() >= 0 && card.getMaxX() <= 800 + 0.5, "x of step " + i + ": " + card);
                assertTrue(card.getMinY() >= 0 && card.getMaxY() <= 600 + 0.5, "y of step " + i + ": " + card);
                setup.tour.next();
            }
            return null;
        });
    }

    @Test
    public void stepWithoutATarget_isInTheMiddleWithoutAFrame() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.layout();
            Bounds card = setup.card().getBoundsInParent();
            assertEquals(400, card.getCenterX(), 1.5);
            assertEquals(300, card.getCenterY(), 1.5);
            assertFalse(setup.tour.getRoot().lookup(".tour-spotlight").isVisible());
            return null;
        });
    }

    @Test
    public void stepWithATarget_framesItAndPutsTheCardAwayFromIt() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.tour.next();
            setup.layout();
            Rectangle frame = (Rectangle) setup.tour.getRoot().lookup(".tour-spotlight");
            Bounds card = setup.card().getBoundsInParent();
            Bounds target = setup.target.localToScene(setup.target.getBoundsInLocal());
            assertTrue(frame.isVisible());
            assertTrue(frame.getX() < target.getMinX() && frame.getY() < target.getMinY(),
                    "the frame starts before the target: " + frame + " and " + target);
            assertTrue(frame.getX() + frame.getWidth() > target.getMaxX()
                    && frame.getY() + frame.getHeight() > target.getMaxY(),
                    "the frame ends after the target: " + frame + " and " + target);
            assertTrue(card.getMaxY() <= frame.getY() || card.getMinY() >= frame.getY() + frame.getHeight(),
                    "the card does not cover what it points at: " + card + " and " + frame);
            return null;
        });
    }

    @Test
    public void stepWhoseTargetCannotBeFound_isShownInTheMiddleWithoutAFrame() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.tour.start();
            setup.tour.next();
            setup.tour.next();
            setup.layout();
            assertEquals("Third", setup.label("#title").getText());
            assertFalse(setup.tour.getRoot().lookup(".tour-spotlight").isVisible());
            return null;
        });
    }

    @Test
    public void hiddenTarget_isTreatedAsMissing() throws Exception {
        Setup setup = onFx(() -> new Setup(STEPS));

        onFx(() -> {
            setup.target.setVisible(false);
            setup.tour.start();
            setup.tour.next();
            setup.layout();
            assertFalse(setup.tour.getRoot().lookup(".tour-spotlight").isVisible());
            return null;
        });
    }
}
