package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkQueries;

/** Shows a complete or filtered homework history and establishes a stable numbered selection. */
public class HomeworkListCommand extends Command {
    public static final String MESSAGE_USAGE = "hw list [st/STUDENT_INDEX] [overdue/|outstanding/]";

    private final Index studentIndex;
    private final boolean overdue;
    private final boolean outstanding;
    private final LocalDate today;

    /** Creates a list query using the supplied calendar date. */
    public HomeworkListCommand(Index studentIndex, boolean overdue, boolean outstanding, LocalDate today) {
        this.studentIndex = studentIndex;
        this.overdue = overdue;
        this.outstanding = outstanding;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        UUID studentId = null;
        if (studentIndex != null) {
            if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
                throw new CommandException(Messages.invalidPersonIndex(studentIndex,
                        model.getFilteredPersonList().size()));
            }
            studentId = model.getFilteredPersonList().get(studentIndex.getZeroBased()).getId();
        }
        List<Homework> rows = HomeworkQueries.select(model.getAddressBook(), studentId, overdue, outstanding, today);
        StringBuilder text = new StringBuilder(rows.size() + " homework item(s).");
        if (rows.isEmpty()) {
            text.append(model.getAddressBook().getHomeworkList().isEmpty()
                    ? "\nNo homework recorded yet." : "\nNo homework matches those filters.");
        }
        for (int i = 0; i < rows.size(); i++) {
            text.append("\n").append(i + 1).append(". ")
                    .append(HomeworkQueries.format(model.getAddressBook(), rows.get(i), today));
        }
        model.showHomework(rows);
        return new CommandResult(text.toString()).withDisplayedHomework(rows);
    }
}
