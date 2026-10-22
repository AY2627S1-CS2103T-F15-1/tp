package seedu.address.logic.commands;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;

/** Records preparation without changing teaching notes or lesson status. */
public class LessonPrepCommand extends Command {
    public static final String MESSAGE_USAGE = "lesson prep INDEX n/NOTES";

    private final Index index;
    private final String notes;

    /** Creates a preparation update for a currently displayed lesson. */
    public LessonPrepCommand(Index index, String notes) {
        this.index = index;
        this.notes = notes;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Lesson lesson = LessonCommandUtil.getLesson(model, index);
        model.setLesson(lesson, lesson.prepare(notes));
        return new CommandResult("Preparation saved for " + lesson.getStudentName() + ": " + notes);
    }
}
