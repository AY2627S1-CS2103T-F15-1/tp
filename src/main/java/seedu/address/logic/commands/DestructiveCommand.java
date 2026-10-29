package seedu.address.logic.commands;

/** A class for commands which are destructive and are marked to be undone. */
public abstract class DestructiveCommand extends Command {
    public abstract String getDestructDescription();
}
