package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * The guided tour: a card that explains one part of the window at a time, with a frame around that part. It lies
 * over the window without covering it, so the user can still type in the command box and run the example that a
 * step offers. Next and Back move between the steps, and Skip tour or Esc leaves.
 */
public class TourOverlay extends UiPart<Pane> {
    public static final String MESSAGE_FINISH = "Finish";
    public static final String MESSAGE_NEXT = "Next";

    private static final String FXML = "TourOverlay.fxml";
    private static final double CARD_WIDTH = 380;
    private static final double GAP = 12;
    private static final double MARGIN = 10;
    private static final double FRAME_PADDING = 4;

    private final List<TourStep> steps;
    private final Function<String, Optional<Node>> finder;
    private final Consumer<String> exampleTarget;
    private final Runnable onClosed;
    private final EventHandler<KeyEvent> escapeFilter = this::handleKey;

    private int stepIndex = -1;
    private Scene filteredScene;

    @FXML
    private Rectangle spotlight;
    @FXML
    private VBox card;
    @FXML
    private Label progress;
    @FXML
    private Label title;
    @FXML
    private Label text;
    @FXML
    private Button exampleButton;
    @FXML
    private Button skipButton;
    @FXML
    private Button backButton;
    @FXML
    private Button nextButton;

    /**
     * Creates a tour of {@code steps}. {@code finder} turns the selector of a step into the part of the window that
     * it names, {@code exampleTarget} receives the example of a step to put in the command box, and
     * {@code onClosed} runs once, when the tour is finished or left.
     */
    public TourOverlay(List<TourStep> steps, Function<String, Optional<Node>> finder,
            Consumer<String> exampleTarget, Runnable onClosed) {
        super(FXML);
        requireNonNull(steps);
        assert !steps.isEmpty() : "a tour has at least one step";
        this.steps = List.copyOf(steps);
        this.finder = requireNonNull(finder);
        this.exampleTarget = requireNonNull(exampleTarget);
        this.onClosed = requireNonNull(onClosed);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        getRoot().widthProperty().addListener((observable, oldValue, newValue) -> placeCard());
        getRoot().heightProperty().addListener((observable, oldValue, newValue) -> placeCard());
        getRoot().setVisible(false);
    }

    /** Shows the first step, and leaves the tour when Esc is pressed anywhere in the window. */
    public void start() {
        filteredScene = getRoot().getScene();
        if (filteredScene != null) {
            filteredScene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
        }
        getRoot().setVisible(true);
        show(0);
    }

    /** Shows the next step, or finishes the tour after the last one. */
    public void next() {
        if (!isShowing()) {
            return;
        }
        if (stepIndex == steps.size() - 1) {
            close();
        } else {
            show(stepIndex + 1);
        }
    }

    /** Shows the step before this one, if there is one. */
    public void back() {
        if (isShowing() && stepIndex > 0) {
            show(stepIndex - 1);
        }
    }

    /** Leaves the tour. */
    public void skip() {
        if (isShowing()) {
            close();
        }
    }

    /** Returns whether the tour is on screen. */
    public boolean isShowing() {
        return getRoot().isVisible();
    }

    /** Returns the number of the step that is shown, counted from 1, or 0 if the tour is not on screen. */
    public int getStepNumber() {
        return isShowing() ? stepIndex + 1 : 0;
    }

    /** Returns the number of steps of the tour. */
    public int getStepCount() {
        return steps.size();
    }

    private void show(int index) {
        stepIndex = index;
        TourStep step = steps.get(index);
        progress.setText("Step " + (index + 1) + " of " + steps.size());
        title.setText(step.title());
        text.setText(step.text());
        exampleButton.setText(step.exampleLabel());
        exampleButton.setVisible(step.hasExample());
        exampleButton.setManaged(step.hasExample());
        exampleButton.setAccessibleText(step.hasExample() ? step.exampleLabel() + ". Nothing runs until you press "
                + "Enter." : "");
        backButton.setDisable(index == 0);
        boolean isLast = index == steps.size() - 1;
        nextButton.setText(isLast ? MESSAGE_FINISH : MESSAGE_NEXT);
        skipButton.setVisible(!isLast);
        skipButton.setManaged(!isLast);
        placeCard();
        Platform.runLater(this::placeCard);
        nextButton.requestFocus();
    }

    /** Puts the card and the frame where the step belongs, which also follows a change of the window's size. */
    private void placeCard() {
        if (!isShowing() || stepIndex < 0) {
            return;
        }
        Pane overlay = getRoot();
        double width = overlay.getWidth();
        double height = overlay.getHeight();
        card.applyCss();
        double cardWidth = Math.min(CARD_WIDTH, Math.max(0, width - 2 * MARGIN));
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        double cardHeight = card.prefHeight(cardWidth);
        card.resize(cardWidth, cardHeight);
        TourStep step = steps.get(stepIndex);
        Optional<Bounds> target = step.hasTarget() ? boundsOf(step.target()) : Optional.empty();
        spotlight.setVisible(target.isPresent());
        if (target.isEmpty()) {
            card.relocate((width - cardWidth) / 2, (height - cardHeight) / 2);
            return;
        }
        Bounds frame = target.get();
        spotlight.setX(frame.getMinX() - FRAME_PADDING);
        spotlight.setY(frame.getMinY() - FRAME_PADDING);
        spotlight.setWidth(frame.getWidth() + 2 * FRAME_PADDING);
        spotlight.setHeight(frame.getHeight() + 2 * FRAME_PADDING);
        double x = clamp(frame.getCenterX() - cardWidth / 2, MARGIN, width - cardWidth - MARGIN);
        double below = frame.getMaxY() + FRAME_PADDING + GAP;
        double above = frame.getMinY() - FRAME_PADDING - GAP - cardHeight;
        if (below + cardHeight <= height - MARGIN) {
            card.relocate(x, below);
        } else if (above >= MARGIN) {
            card.relocate(x, above);
        } else {
            card.relocate(x, clamp((height - cardHeight) / 2, MARGIN, height - cardHeight - MARGIN));
        }
    }

    private Optional<Bounds> boundsOf(String selector) {
        return finder.apply(selector).filter(Node::isVisible).map(node -> {
            Bounds inScene = node.localToScene(node.getBoundsInLocal());
            return getRoot().sceneToLocal(inScene);
        });
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, Math.max(min, max)));
    }

    private void close() {
        getRoot().setVisible(false);
        stepIndex = -1;
        if (filteredScene != null) {
            filteredScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
            filteredScene = null;
        }
        onClosed.run();
    }

    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && isShowing()) {
            event.consume();
            skip();
        }
    }

    @FXML
    private void handleExample() {
        exampleTarget.accept(steps.get(stepIndex).example());
    }

    @FXML
    private void handleNext() {
        next();
    }

    @FXML
    private void handleBack() {
        back();
    }

    @FXML
    private void handleSkip() {
        skip();
    }
}
