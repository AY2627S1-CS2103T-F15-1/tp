package seedu.address.model;

/**
 * An extension to AddressBook, with additional undo capabilities.
 */
public class VersionedAddressBook extends AddressBook {
    private String lastDestructiveAction;
    private ReadOnlyAddressBook lastState;
    private ReadOnlyAddressBook transitionState;

    public VersionedAddressBook() {
        super();
    }

    public VersionedAddressBook(ReadOnlyAddressBook toBeCopied) {
        super(toBeCopied);
    }

    /** Hook to be run before commands execute, allowing us to take a snapshot if needed. */
    public void beforeCommand(boolean isDestructive) {
        if (isDestructive) {
            this.transitionState = new AddressBook(this);
        }
    }

    /**
     * Hook to be run after commands execute,
     * allowing us to update our snapshot states based on the outcome of the command.
     */
    public void afterCommand(String destructiveDescription, boolean isSuccessful) {
        if (isSuccessful) {
            if (destructiveDescription == null) {
                lastDestructiveAction = null;
                transitionState = null;
                lastState = null;
            } else {
                lastState = transitionState;
                lastDestructiveAction = destructiveDescription;
            }
        } else {
            transitionState = null;
        }
    }

    /**
     * Undoes the last destructive action performed on this AddressBook,
     * provided no attempts to modify it have been made.
     * @return description of the destructive action, or null if no destructive action
     */
    public String undoDestructiveAction() {
        if (lastDestructiveAction == null) {
            return null;
        }
        resetData(lastState);
        // will reset when command completes
        return lastDestructiveAction;
    }

}
