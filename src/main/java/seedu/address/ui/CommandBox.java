package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> implements CommandDraftTarget {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory history = new CommandHistory();

    @FXML
    private TextField commandTextField;
    @FXML
    private Button clearCommandButton;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, newText) -> {
            setStyleToDefault();
            updateClearButton(newText);
        });
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleHistoryKey);
        clearCommandButton.setGraphic(UiIcon.cross());
        clearCommandButton.setFocusTraversable(true);
        updateClearButton(commandTextField.getText());
    }

    /** Returns the current command text. */
    @Override
    public String getText() {
        return commandTextField.getText();
    }

    /** Replaces the command text and focuses the field so the user can continue typing. */
    @Override
    public void setTextAndFocus(String text) {
        commandTextField.setText(text);
        commandTextField.requestFocus();
        commandTextField.positionCaret(text.length());
    }

    /** Clears the command text and returns focus to it. */
    @Override
    public void clearAndFocus() {
        setTextAndFocus("");
    }

    /**
     * Shows an earlier command when Up is pressed and a later one when Down is pressed, as a shell does.
     */
    private void handleHistoryKey(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            history.previous(commandTextField.getText()).ifPresent(this::showInTextField);
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            history.next().ifPresent(this::showInTextField);
            event.consume();
        }
    }

    /** Puts {@code text} in the command box with the caret at its end, ready to be run or edited. */
    private void showInTextField(String text) {
        commandTextField.setText(text);
        commandTextField.positionCaret(text.length());
    }

    /** Shows the clear control only when there is text to clear. */
    private void updateClearButton(String text) {
        boolean isPresent = text != null && !text.isEmpty();
        clearCommandButton.setVisible(isPresent);
        clearCommandButton.setManaged(isPresent);
    }

    /** Clears the current command text and returns focus to the input. */
    @FXML
    private void handleClearCommand() {
        clearAndFocus();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        history.record(commandText);
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            // The command stays in the box so that the user can correct it instead of typing it again
            commandTextField.positionCaret(commandText.length());
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
