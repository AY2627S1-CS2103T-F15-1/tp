package seedu.address.ui;

import static java.util.Objects.requireNonNull;

/**
 * One step of the guided tour.
 *
 * @param title what the step is about, in a few words.
 * @param text what the step tells the user, in a sentence or two.
 * @param target the selector of the part of the window that the step points at, or an empty text if it points at
 *               nothing and is shown in the middle of the window.
 * @param exampleLabel what the button for the example says, or an empty text if the step has no example.
 * @param example the command that the example puts in the command box, for the user to change and run.
 */
public record TourStep(String title, String text, String target, String exampleLabel, String example) {

    /** Creates a step. */
    public TourStep {
        requireNonNull(title);
        requireNonNull(text);
        requireNonNull(target);
        requireNonNull(exampleLabel);
        requireNonNull(example);
    }

    /** Returns a step that points at nothing and has no example. */
    public static TourStep of(String title, String text) {
        return new TourStep(title, text, "", "", "");
    }

    /** Returns this step pointing at the part of the window that {@code selector} names. */
    public TourStep pointingAt(String selector) {
        return new TourStep(title, text, selector, exampleLabel, example);
    }

    /** Returns this step with a button that puts {@code command} in the command box, called {@code label}. */
    public TourStep withExample(String label, String command) {
        return new TourStep(title, text, target, label, command);
    }

    /** Returns whether the step points at a part of the window. */
    public boolean hasTarget() {
        return !target.isEmpty();
    }

    /** Returns whether the step has an example to put in the command box. */
    public boolean hasExample() {
        return !example.isEmpty();
    }
}
