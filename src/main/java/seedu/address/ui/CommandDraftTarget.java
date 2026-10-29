package seedu.address.ui;

/** The command input operations a GUI shortcut needs, kept separate from JavaFX for testability. */
public interface CommandDraftTarget {
    /** Returns the current draft. */
    String getText();

    /** Replaces the draft and focuses the input. */
    void setTextAndFocus(String text);

    /** Clears the draft and focuses the input. */
    void clearAndFocus();
}
