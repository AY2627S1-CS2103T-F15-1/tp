package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.Level;
import seedu.address.model.person.Subject;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows all students, and numbers them again so "
            + "that the other commands can refer to them.\n"
            + "Parameters: [l/LEVEL] [s/SUBJECT] [archived/|all/]";

    public static final String MESSAGE_SUCCESS = "Listed active students.";

    private final Level level;
    private final Subject subject;
    private final boolean archived;
    private final boolean all;

    /** Lists active students without academic filters. */
    public ListCommand() {
        this(null, null, false, false);
    }

    /** Lists students matching all supplied filters and the chosen archive scope. */
    public ListCommand(Level level, Subject subject, boolean archived, boolean all) {
        this.level = level;
        this.subject = subject;
        this.archived = archived;
        this.all = all;
    }


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> (all || person.isArchived() == archived)
                && (level == null || level.equals(person.getLevel()))
                && (subject == null || person.getSubjects().contains(subject)));
        return new CommandResult(level == null && subject == null && !archived && !all ? MESSAGE_SUCCESS
                : "Listed " + model.getFilteredPersonList().size() + " matching student(s).");
    }
}
