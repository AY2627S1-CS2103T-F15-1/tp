package seedu.address.logic.commands;

import java.time.LocalDate;

import seedu.address.model.Model;
import seedu.address.model.finance.FinanceQueries;

/** Shows active and archived student debt ordered by oldest unpaid charge age. */
public class DuesCommand extends Command {
    public static final String MESSAGE_USAGE = "dues (no parameters)";
    private final LocalDate today;

    /** Creates a query with an explicit local date for debt ages. */
    public DuesCommand(LocalDate today) {
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(FinanceQueries.duesSummary(model.getAddressBook(), today));
    }
}
