package seedu.address.ui;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private final GuideLoader guideLoader;

    @FXML
    private WebView userGuide;

    @FXML
    private Label loadStatus;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, UserGuide::loadHtml);
    }

    /**
     * Creates a help window with a loader, allowing read failures to be handled independently of the viewer.
     */
    HelpWindow(Stage root, GuideLoader guideLoader) {
        super(FXML, root);
        this.guideLoader = guideLoader;
        userGuide.getEngine().getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            switch (newState) {
                case SCHEDULED, RUNNING -> loadStatus.setText("Loading User Guide...");
                case SUCCEEDED -> loadStatus.setText("");
                case FAILED, CANCELLED -> loadStatus.setText(
                        "Could not load the bundled User Guide. Click Reload guide to try again.");
                default -> loadStatus.setText("");
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        Worker.State state = userGuide.getEngine().getLoadWorker().getState();
        if (state == Worker.State.READY || state == Worker.State.FAILED || state == Worker.State.CANCELLED) {
            reloadGuide();
        }
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
    }

    /**
     * Loads the User Guide bundled from this project's documentation in the help window.
     */
    @FXML
    private void reloadGuide() {
        try {
            userGuide.getEngine().loadContent(guideLoader.load());
        } catch (IOException e) {
            logger.warning("Could not read the bundled User Guide: " + e);
            loadStatus.setText("Could not read the bundled User Guide. Click Reload guide to try again.");
        }
    }

    @FunctionalInterface
    interface GuideLoader {
        String load() throws IOException;
    }
}
