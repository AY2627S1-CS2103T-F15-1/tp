package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ALL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STUDENT;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.LessonCommandParser;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Lists the lessons of a student in the displayed student list. The numbers it shows are the lesson indices that
 * the other lesson commands use.
 */
public class LessonListCommand extends Command {

    public static final String COMMAND_WORD = LessonCommandParser.COMMAND_WORD + " list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists the upcoming lessons of the student "
            + "identified by the index number used in the displayed student list, or all of their lessons "
            + "if " + PREFIX_ALL + " is given.\n"
            + "Parameters: " + PREFIX_STUDENT + "STUDENT_INDEX [" + PREFIX_ALL + "]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STUDENT + "1 " + PREFIX_ALL;

    public static final String MESSAGE_NO_LESSONS = "%1$s has no lessons.";
    public static final String MESSAGE_NO_UPCOMING_LESSONS = "%1$s has no upcoming lessons. Use '"
            + PREFIX_ALL + "' to include past lessons.";
    public static final String MESSAGE_HEADER_UPCOMING = "%1$s — %2$d upcoming lesson(s)";
    public static final String MESSAGE_HEADER_ALL = "%1$s — %2$d lesson(s)";

    private final Index studentIndex;
    private final boolean isShowingAll;

    /**
     * Creates a {@code LessonListCommand} to list the lessons of the student at {@code studentIndex}.
     * Past and cancelled lessons are only listed if {@code isShowingAll} is true.
     */
    public LessonListCommand(Index studentIndex, boolean isShowingAll) {
        requireNonNull(studentIndex);
        this.studentIndex = studentIndex;
        this.isShowingAll = isShowingAll;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = LessonCommandUtil.getStudent(model, studentIndex);

        Predicate<Lesson> isStudentsLesson = lesson -> lesson.isWith(student);
        model.updateFilteredLessonList(isStudentsLesson);
        if (model.getFilteredLessonList().isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_LESSONS, student.getName()));
        }

        if (isShowingAll) {
            return new CommandResult(format(MESSAGE_HEADER_ALL, student, model.getFilteredLessonList()));
        }
        LocalDate today = LocalDate.now();
        model.updateFilteredLessonList(isStudentsLesson.and(lesson -> lesson.isUpcoming(today)));
        if (model.getFilteredLessonList().isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_UPCOMING_LESSONS, student.getName()));
        }
        return new CommandResult(format(MESSAGE_HEADER_UPCOMING, student, model.getFilteredLessonList()));
    }

    private static String format(String headerFormat, Person student, List<Lesson> lessons) {
        String header = String.format(headerFormat, student.getName(), lessons.size());
        String body = IntStream.range(0, lessons.size())
                .mapToObj(i -> String.format("%d. %s  %s  %s  %s", i + 1, lessons.get(i).getDate(),
                        lessons.get(i).getTimeRange(), lessons.get(i).getSubject(), describe(lessons.get(i))))
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

    private static String describe(Lesson lesson) {
        return Messages.formatVenue(lesson) + "  " + lesson.getStatus();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonListCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex) && isShowingAll == otherCommand.isShowingAll;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("isShowingAll", isShowingAll)
                .toString();
    }
}
