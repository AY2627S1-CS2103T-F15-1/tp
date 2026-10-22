package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Level;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * Lists the students that match an optional level, an optional subject and an archive scope, and numbers
 * them again so that the other commands can refer to them.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows the active students, or only the archived "
            + "ones with archived/, or both with all/, optionally limited to one level and subject, and numbers "
            + "them again so that the other commands can refer to them.\n"
            + "Parameters: [l/LEVEL] [s/SUBJECT] [archived/|all/]\n"
            + "Examples: " + COMMAND_WORD + ", " + COMMAND_WORD + " l/S3 s/Math, " + COMMAND_WORD + " archived/";

    public static final String MESSAGE_SUCCESS = "Listed active students.";
    public static final String MESSAGE_FILTERED = "Listed %1$d matching student(s).";

    private final Level level; // null if students of every level are listed
    private final Subject subject; // null if students of every subject are listed
    private final boolean isArchivedOnly;
    private final boolean isArchivedIncluded;

    /** Creates a command that lists the active students without academic filters. */
    public ListCommand() {
        this(null, null, false, false);
    }

    /**
     * Creates a command that lists the students that match {@code level} and {@code subject}, either of which may
     * be null. Only archived students are listed if {@code isArchivedOnly} is true, and active and archived
     * students are listed if {@code isArchivedIncluded} is true.
     */
    public ListCommand(Level level, Subject subject, boolean isArchivedOnly, boolean isArchivedIncluded) {
        this.level = level;
        this.subject = subject;
        this.isArchivedOnly = isArchivedOnly;
        this.isArchivedIncluded = isArchivedIncluded;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(this::matches);
        boolean isUnfiltered = level == null && subject == null && !isArchivedOnly && !isArchivedIncluded;
        return new CommandResult(isUnfiltered ? MESSAGE_SUCCESS
                : String.format(MESSAGE_FILTERED, model.getFilteredPersonList().size()));
    }

    /** Returns whether {@code person} is in the archive scope and matches the level and subject filters. */
    private boolean matches(Person person) {
        return (isArchivedIncluded || person.isArchived() == isArchivedOnly)
                && (level == null || level.equals(person.getLevel()))
                && (subject == null || person.getSubjects().contains(subject));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ListCommand otherCommand)) {
            return false;
        }

        return Objects.equals(level, otherCommand.level) && Objects.equals(subject, otherCommand.subject)
                && isArchivedOnly == otherCommand.isArchivedOnly
                && isArchivedIncluded == otherCommand.isArchivedIncluded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, subject, isArchivedOnly, isArchivedIncluded);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("level", level)
                .add("subject", subject)
                .add("isArchivedOnly", isArchivedOnly)
                .add("isArchivedIncluded", isArchivedIncluded)
                .toString();
    }
}
