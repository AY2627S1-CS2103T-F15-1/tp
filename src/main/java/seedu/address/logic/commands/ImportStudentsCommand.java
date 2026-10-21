package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.csv.StudentCsvImport;
import seedu.address.logic.parser.PathArgument;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds the students of a CSV file, all or none. Without {@code confirm} the command only reads and checks the file
 * and says what would happen.
 */
public class ImportStudentsCommand extends Command {

    public static final String COMMAND_WORD = "import";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " students file/PATH [confirm]\n"
            + COMMAND_WORD + ": Adds the students of a UTF-8 CSV file, all or none. The first line names the columns: "
            + "name, phone, level, subjects, rate and, if you like, email and venue, in any order. Separate several "
            + "subjects with ;. Every field is checked by the rules of add. Without confirm, only the file is read "
            + "and checked.\n"
            + "Examples: " + COMMAND_WORD + " students file/students.csv, " + COMMAND_WORD
            + " students file/students.csv confirm";

    public static final String MESSAGE_NOT_FOUND = "Cannot find a file at %1$s.";
    public static final String MESSAGE_FOLDER = "%1$s is a folder. Give the path of a CSV file.";
    public static final String MESSAGE_HEADER = "Student import from %1$s";
    public static final String MESSAGE_IMPORTED = "Imported %1$s.";
    public static final String MESSAGE_CONFIRM = "Type '%1$s' to add %2$s.";
    public static final String MESSAGE_NOTHING_NEW = "There are no new students to import.";
    public static final String MESSAGE_FIX_ROWS = "Nothing can be imported until the rows above are fixed.";
    public static final String MESSAGE_FIXED_ROWS = "Nothing was imported. Fix the rows above and try again.";

    private static final Logger logger = LogsCenter.getLogger(ImportStudentsCommand.class);

    private final Path file;
    private final String pathText;
    private final boolean isConfirmed;

    /**
     * Creates a command that imports the students of {@code file}, which was typed as {@code pathText}. If
     * {@code isConfirmed} is false, the file is only read and checked.
     */
    public ImportStudentsCommand(Path file, String pathText, boolean isConfirmed) {
        requireNonNull(file);
        requireNonNull(pathText);
        this.file = file;
        this.pathText = pathText;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (Files.isDirectory(file)) {
            throw new CommandException(String.format(MESSAGE_FOLDER, pathText));
        }
        if (!Files.exists(file)) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, pathText));
        }
        StudentCsvImport.Report report;
        try {
            report = StudentCsvImport.read(file, pathText, model.getAddressBook());
        } catch (StudentCsvImport.ImportException e) {
            throw new CommandException(e.getMessage(), e);
        }

        StringBuilder text = new StringBuilder(String.format(MESSAGE_HEADER, report.fileName())).append("\n")
                .append(report.summary(isConfirmed && !report.hasErrors()));
        if (!report.issues().isEmpty()) {
            text.append("\n").append(report.issues());
        }
        if (report.hasErrors()) {
            throw new CommandException(text.append("\n").append(isConfirmed ? MESSAGE_FIXED_ROWS : MESSAGE_FIX_ROWS)
                    .toString());
        }

        List<Person> students = report.students();
        if (!isConfirmed) {
            String confirmation = COMMAND_WORD + " students file/" + PathArgument.format(pathText) + " confirm";
            text.append("\n").append(students.isEmpty() ? MESSAGE_NOTHING_NEW
                    : String.format(MESSAGE_CONFIRM, confirmation, count(students.size())));
            CommandResult preview = new CommandResult(text.toString());
            return students.isEmpty() ? preview : preview.withConfirmationCommand(confirmation);
        }
        students.forEach(model::addPerson);
        logger.fine("Imported " + students.size() + " student(s)");
        return new CommandResult(String.format(MESSAGE_IMPORTED, count(students.size())) + "\n" + text)
                .withAddedStudents(students);
    }

    private static String count(int students) {
        return students + (students == 1 ? " student" : " students");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ImportStudentsCommand otherCommand)) {
            return false;
        }

        return file.equals(otherCommand.file) && pathText.equals(otherCommand.pathText)
                && isConfirmed == otherCommand.isConfirmed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, pathText, isConfirmed);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("file", file)
                .add("isConfirmed", isConfirmed)
                .toString();
    }
}
