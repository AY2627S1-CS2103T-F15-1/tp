package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The commands the user has entered in this session, which the up and down keys step through as in a shell.
 * Blank commands and a command identical to the one before it are not kept, and only the latest
 * {@value #MAX_ENTRIES} commands are remembered.
 */
public class CommandHistory {

    public static final int MAX_ENTRIES = 100;

    private final List<String> entries = new ArrayList<>();
    /** The entry being shown, or {@code entries.size()} when the user is not stepping through the history. */
    private int position = 0;
    /** What the user had typed before stepping into the history, shown again when stepping back down. */
    private String draft = "";

    /**
     * Remembers {@code command} as the latest entry and stops stepping through the history.
     */
    public void record(String command) {
        requireNonNull(command);
        position = entries.size();
        if (command.isBlank() || !entries.isEmpty() && entries.get(entries.size() - 1).equals(command)) {
            return;
        }
        entries.add(command);
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
        position = entries.size();
    }

    /**
     * Steps to the previous (older) command. {@code currentText} is kept as a draft if this is the first step.
     * A command that was just run and is still shown, such as one that failed, is not offered again.
     *
     * @return the command to show, or empty if there is no older command.
     */
    public Optional<String> previous(String currentText) {
        requireNonNull(currentText);
        int newest = entries.size() - 1;
        int older = position;
        if (position == entries.size()) {
            older = !entries.isEmpty() && entries.get(newest).equals(currentText) ? newest : entries.size();
        }
        if (older == 0) {
            return Optional.empty();
        }
        if (position == entries.size()) {
            draft = currentText;
        }
        position = older - 1;
        return Optional.of(entries.get(position));
    }

    /**
     * Steps to the next (newer) command, ending with the draft that was typed before the first step back.
     *
     * @return the text to show, or empty if the user is not stepping through the history.
     */
    public Optional<String> next() {
        if (position >= entries.size()) {
            return Optional.empty();
        }
        position++;
        if (position == entries.size() - 1 && entries.get(position).equals(draft)) {
            position++; // the draft is the newest command, which stepping back skipped, so it is not shown twice
        }
        return Optional.of(position == entries.size() ? draft : entries.get(position));
    }
}
