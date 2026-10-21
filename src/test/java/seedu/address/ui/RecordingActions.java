package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

/** Remembers what the buttons of a panel asked for, instead of doing it. */
final class RecordingActions implements PanelActions {
    private final List<String> calls = new ArrayList<>();

    @Override
    public void run(String command) {
        calls.add("run " + command);
    }

    @Override
    public void prefill(String text) {
        calls.add("prefill " + text);
    }

    @Override
    public void ask(String question, String command) {
        calls.add("ask " + question + " -> " + command);
    }

    /** Returns what was asked for, in order. */
    List<String> calls() {
        return List.copyOf(calls);
    }
}
