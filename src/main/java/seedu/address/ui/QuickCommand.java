package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;

/**
 * A button on the toolbar of a panel that runs a command that the user could type. Its tooltip names the command,
 * so that using the button teaches the command.
 *
 * @param label what the button says.
 * @param command the command that the button runs.
 */
public record QuickCommand(String label, String command) {
    public static final String STYLE_CLASS = "toolbar-button";

    /** Creates a quick command. */
    public QuickCommand {
        requireNonNull(label);
        requireNonNull(command);
    }

    /** Returns a button that runs this command through {@code actions}. */
    public Button toButton(PanelActions actions) {
        requireNonNull(actions);
        Button button = new Button(label);
        button.setMnemonicParsing(false);
        button.getStyleClass().add(STYLE_CLASS);
        button.setTooltip(new Tooltip("Same as typing: " + command));
        button.setAccessibleText(label + ". Same as typing: " + command);
        button.setOnAction(event -> actions.run(command));
        return button;
    }

    /** Returns a toolbar of buttons for {@code commands}, which wraps onto more lines in a narrow panel. */
    public static FlowPane toolbarOf(PanelActions actions, QuickCommand... commands) {
        FlowPane toolbar = new FlowPane(6, 4);
        toolbar.getStyleClass().add("panel-toolbar");
        for (QuickCommand command : commands) {
            toolbar.getChildren().add(command.toButton(actions));
        }
        return toolbar;
    }
}
