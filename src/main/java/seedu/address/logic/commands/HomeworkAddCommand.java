package seedu.address.logic.commands;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.homework.Homework;
import seedu.address.model.person.Person;

/** Assigns homework to an active student without changing previously displayed homework indices. */
public class HomeworkAddCommand extends Command {
    public static final String MESSAGE_USAGE = "hw add st/STUDENT_INDEX desc/DESCRIPTION due/DATE";

    private final Index studentIndex;
    private final String description;
    private final LocalDate dueDate;
    private final LocalDate today;

    /** Creates an assignment request with an explicit calendar date for validation. */
    public HomeworkAddCommand(Index studentIndex, String description, LocalDate dueDate, LocalDate today) {
        this.studentIndex = studentIndex;
        this.description = description;
        this.dueDate = dueDate;
        this.today = today;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person student = HomeworkCommandSupport.student(model, studentIndex);
        if (student.isArchived()) {
            throw new CommandException("Restore this student before assigning new homework.");
        }
        HomeworkCommandSupport.validateDueDate(dueDate, today);
        Homework homework = Homework.assign(student.getId(), description, dueDate);
        try {
            model.addHomework(homework);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        String note = model.getAddressBook().getLessonList().stream()
                .anyMatch(lesson -> lesson.isWith(student) && lesson.isUpcoming(today))
                ? "" : "\nNote: " + student.getName() + " has no upcoming lessons.";
        return new CommandResult("Homework assigned to " + student.getName() + ": " + description
                + ", due " + dueDate + note + "\nRun 'hw list' to display the new assignment's index.");
    }
}
