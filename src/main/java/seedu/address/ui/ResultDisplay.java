package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Shows a normal result in the default colour.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.getStyleClass().remove(ERROR_STYLE_CLASS);
        resultDisplay.setText(feedbackToUser);
    }

    /**
     * Shows an error message in red, so that it is not mistaken for a result.
     */
    public void setErrorToUser(String errorMessage) {
        requireNonNull(errorMessage);
        if (!resultDisplay.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        resultDisplay.setText(errorMessage);
    }

}
