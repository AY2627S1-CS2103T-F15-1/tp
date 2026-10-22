package seedu.address.ui;

import java.io.IOException;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/** Controller for the offline user guide window. */
public class HelpWindow extends UiPart<Stage> {
    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private final GuideLoader guideLoader;
    private boolean loaded;

    @FXML
    private GuideView userGuide;
    @FXML
    private Label loadStatus;

    /** Creates a help window using the supplied stage. */
    public HelpWindow(Stage root) {
        this(root, UserGuide::loadHtml);
    }

    /** Creates a help window with a loader that can report resource failures. */
    HelpWindow(Stage root, GuideLoader guideLoader) {
        super(FXML, root);
        this.guideLoader = guideLoader;
        userGuide.setStatusHandler(loadStatus::setText);
    }

    public HelpWindow() {
        this(new Stage());
    }

    /** Shows the guide and retries loading if an earlier read failed. */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        if (!loaded) {
            reloadGuide();
        }
    }

    public boolean isShowing() {
        return getRoot().isShowing();
    }

    public void hide() {
        getRoot().hide();
    }

    public void focus() {
        getRoot().requestFocus();
    }

    /** Reloads the documentation bundled with this version of the application. */
    @FXML
    private void reloadGuide() {
        try {
            loadStatus.setText("Loading User Guide...");
            userGuide.loadHtml(guideLoader.load());
            loaded = true;
            loadStatus.setText("");
        } catch (IOException e) {
            loaded = false;
            logger.warning("Could not read the bundled User Guide: " + e);
            loadStatus.setText("Could not read the bundled User Guide. Click Reload guide to try again.");
        }
    }

    @FunctionalInterface
    interface GuideLoader {
        String load() throws IOException;
    }
}
