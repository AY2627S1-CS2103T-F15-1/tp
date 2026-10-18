package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.finance.FinanceQueries;
import seedu.address.model.person.Person;

/** Shows the complete chronological account without changing payment numbering. */
public class BalanceCommand extends Command {
    public static final String MESSAGE_USAGE = "balance st/STUDENT_INDEX";
    private final Index studentIndex;

    /** Creates an account query for a displayed student, active or archived. */
    public BalanceCommand(Index studentIndex) {
        this.studentIndex = studentIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person student = FinanceCommandSupport.student(model, studentIndex);
        return new CommandResult(FinanceQueries.account(model.getAddressBook(), student.getId()));
    }
}
