package seedu.address.logic.commands;

/** A class for commands which are destructive and are marked to be undone. */
public abstract class DestructiveCommand extends Command {
    /** Returns a short description of what this command destroys, used for the undo message. */
    public abstract String getDestructDescription();
}
