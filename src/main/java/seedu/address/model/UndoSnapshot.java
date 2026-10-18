package seedu.address.model;

import static java.util.Objects.requireNonNull;

/**
 * A complete copy of the data taken just before a destructive command, kept for one undo.
 * The caller passes a private copy, so later changes to the live data cannot alter the snapshot.
 *
 * @param label a short description of the command that the snapshot guards, such as "student deletion".
 * @param data the data as it was before that command.
 */
public record UndoSnapshot(String label, ReadOnlyAddressBook data) {
    /** Rejects missing parts so that an undo can never restore nothing. */
    public UndoSnapshot {
        requireNonNull(label);
        requireNonNull(data);
    }
}
