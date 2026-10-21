package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * The strip that tells the user that the data on screen is sample data from the demo, which is never saved,
 * and offers a button to go back to their own data.
 */
public class DemoBanner extends UiPart<Region> {
    public static final String MESSAGE = "DEMO MODE: this is sample data and nothing here is saved. "
            + "Your own data is kept safe.";

    private static final String FXML = "DemoBanner.fxml";

    private final Runnable exitDemo;

    @FXML
    private Label message;
    @FXML
    private Button exitButton;

    /** Creates a banner, hidden until {@link #setDemoMode(boolean)} shows it, whose button runs {@code exitDemo}. */
    public DemoBanner(Runnable exitDemo) {
        super(FXML);
        this.exitDemo = requireNonNull(exitDemo);
        message.setText(MESSAGE);
        message.setAccessibleText(MESSAGE);
        exitButton.setAccessibleText("Exit the demo and bring back your own data");
        setDemoMode(false);
    }

    /** Shows the banner while {@code isDemoMode} is true, and takes no room on screen otherwise. */
    public void setDemoMode(boolean isDemoMode) {
        getRoot().setVisible(isDemoMode);
        getRoot().setManaged(isDemoMode);
    }

    @FXML
    private void handleExit() {
        exitDemo.run();
    }
}
