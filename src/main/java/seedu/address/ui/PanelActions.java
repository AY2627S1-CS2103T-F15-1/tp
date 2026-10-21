package seedu.address.ui;

/**
 * What the buttons on the panels can ask the main window to do. A button never changes data itself: it runs a
 * command, or helps the user to type one, so that everything it does is also something the user can type.
 */
public interface PanelActions {

    /** Runs {@code command} as if the user had typed it, and shows its result as any command's. */
    void run(String command);

    /** Puts {@code text} in the command box, at the end of the line, for the user to complete and run. */
    void prefill(String text);

    /** Asks {@code question} and runs {@code command} only if the user confirms. */
    void ask(String question, String command);
}
