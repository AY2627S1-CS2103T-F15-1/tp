package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Sorts all persons in the address book by name.
 * The order is kept when the address book is saved, so that the next launch shows the same order.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String ORDER_ASCENDING = "asc";
    public static final String ORDER_DESCENDING = "desc";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts all students by name, ignoring case, "
            + "and displays them as a list with index numbers.\n"
            + "Parameters: [" + ORDER_ASCENDING + "|" + ORDER_DESCENDING + "] (default: " + ORDER_ASCENDING + ")\n"
            + "Examples: " + COMMAND_WORD + ", " + COMMAND_WORD + " " + ORDER_DESCENDING;

    public static final String MESSAGE_SUCCESS_ASCENDING = "Sorted all students by name (A to Z).";
    public static final String MESSAGE_SUCCESS_DESCENDING = "Sorted all students by name (Z to A).";

    private static final Comparator<Person> NAME_COMPARATOR =
            Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);

    private final boolean isDescending;

    /**
     * Creates a SortCommand that sorts in descending order if {@code isDescending} is true,
     * and in ascending order otherwise.
     */
    public SortCommand(boolean isDescending) {
        this.isDescending = isDescending;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.sortPersons(isDescending ? NAME_COMPARATOR.reversed() : NAME_COMPARATOR);
        // Sorting a filtered list would leave the user unsure which persons were sorted, so show everyone.
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(isDescending ? MESSAGE_SUCCESS_DESCENDING : MESSAGE_SUCCESS_ASCENDING);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }

        return isDescending == otherSortCommand.isDescending;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("isDescending", isDescending)
                .toString();
    }
}
