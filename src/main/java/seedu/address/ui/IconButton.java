package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * A button that shows an {@link Icon} instead of a word. The description is the tooltip and also what a screen
 * reader says, so the picture is never the only thing that tells the user what the button does.
 */
public class IconButton extends Button {
    public static final String STYLE_CLASS = "icon-button";

    /** Creates a button with {@code icon} that runs {@code action} and is described by {@code description}. */
    public IconButton(Icon icon, String description, Runnable action) {
        requireNonNull(icon);
        requireNonNull(description);
        requireNonNull(action);
        setGraphic(icon.shape());
        setTooltip(new Tooltip(description));
        setAccessibleText(description);
        setMnemonicParsing(false);
        getStyleClass().add(STYLE_CLASS);
        setOnAction(event -> action.run());
    }
}
