package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * The strip above the command box that asks the user to confirm a command before it runs. It offers a button for
 * what the user would otherwise type, such as {@code delete 3 confirm}, and it goes away as soon as the user
 * cancels or runs anything else.
 */
public class ConfirmBar extends UiPart<Region> {
    public static final String MESSAGE_CANCELLED = "Cancelled. Nothing was changed.";

    private static final String FXML = "ConfirmBar.fxml";

    private final PanelActions actions;
    private final Runnable onCancel;
    private String command;

    @FXML
    private Label question;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    /**
     * Creates a bar, hidden until {@link #ask} shows it. Confirming runs the command through {@code actions}, and
     * cancelling calls {@code onCancel}.
     */
    public ConfirmBar(PanelActions actions, Runnable onCancel) {
        super(FXML);
        this.actions = requireNonNull(actions);
        this.onCancel = requireNonNull(onCancel);
        cancelButton.setAccessibleText("Cancel. Nothing is changed.");
        clear();
    }

    /** Shows {@code questionText} with a button that runs {@code commandText} when the user presses it. */
    public void ask(String questionText, String commandText) {
        requireNonNull(questionText);
        requireNonNull(commandText);
        command = commandText;
        question.setText(questionText);
        question.setAccessibleText(questionText);
        confirmButton.setAccessibleText("Confirm. Runs: " + commandText);
        getRoot().setVisible(true);
        getRoot().setManaged(true);
    }

    /** Hides the bar and forgets the command that it offered. */
    public void clear() {
        command = null;
        getRoot().setVisible(false);
        getRoot().setManaged(false);
    }

    /** Returns whether the bar is asking the user to confirm something. */
    public boolean isAsking() {
        return command != null;
    }

    /** Returns the command that the confirm button runs, if the bar is asking. */
    public Optional<String> getCommand() {
        return Optional.ofNullable(command);
    }

    /** Hides the bar and tells the user that nothing was changed, as the cancel button does. */
    public void cancel() {
        clear();
        onCancel.run();
    }

    @FXML
    private void handleConfirm() {
        String toRun = command;
        clear();
        if (toRun != null) {
            actions.run(toRun);
        }
    }

    @FXML
    private void handleCancel() {
        cancel();
    }
}
