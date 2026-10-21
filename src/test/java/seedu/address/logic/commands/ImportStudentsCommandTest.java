package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class ImportStudentsCommandTest {
    private static final String HEADER = "name,phone,level,subjects,rate,email,venue\n";
    private static final String THREE = HEADER + "Amy Tan,91234567,S3,Math,50,,\n"
            + "Bob Lim,92345678,J1,Physics;Chemistry,62.50,bob@example.com,Blk 5\n"
            + "Cy Ng,93456789,P6,English,35,,\n";

    @TempDir
    public Path folder;

    private final Model model = new ModelManager(new AddressBook(), new UserPrefs());

    private ImportStudentsCommand command(String content, boolean isConfirmed) throws IOException {
        Path file = folder.resolve("students.csv");
        Files.writeString(file, content);
        return new ImportStudentsCommand(file, "students.csv", isConfirmed);
    }

    private String rejection(ImportStudentsCommand command) {
        return assertThrows(CommandException.class, () -> command.execute(model)).getMessage();
    }

    @Test
    public void execute_previewOfAGoodFile_reportsAndChangesNothing() throws Exception {
        String message = command(THREE, false).execute(model).getFeedbackToUser();

        assertEquals("Student import from students.csv\n"
                + "Rows read: 3, ready to add: 3, duplicates skipped: 0, warnings: 0, errors: 0, "
                + "blank lines ignored: 0\n"
                + "Type 'import students file/students.csv confirm' to add 3 students.", message);
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_previewOfAGoodFile_offersTheConfirmingCommand() throws Exception {
        CommandResult preview = command(THREE, false).execute(model);

        assertEquals(Optional.of("import students file/students.csv confirm"), preview.getConfirmationCommand());
        assertTrue(command(THREE, true).execute(model).getConfirmationCommand().isEmpty());
    }

    @Test
    public void execute_confirmedImportOfAGoodFile_addsEveryStudentAndHighlightsThem() throws Exception {
        CommandResult result = command(THREE, true).execute(model);

        assertEquals("Imported 3 students.\nStudent import from students.csv\n"
                + "Rows read: 3, added: 3, duplicates skipped: 0, warnings: 0, errors: 0, "
                + "blank lines ignored: 0", result.getFeedbackToUser());
        assertEquals(List.of("Amy Tan", "Bob Lim", "Cy Ng"), model.getAddressBook().getPersonList().stream()
                .map(student -> student.getName().fullName).toList());
        assertEquals(model.getAddressBook().getPersonList(), result.getAddedStudents());
    }

    @Test
    public void execute_oneStudent_isNotPluralised() throws Exception {
        String content = HEADER + "Amy Tan,91234567,S3,Math,50,,\n";

        assertTrue(command(content, false).execute(model).getFeedbackToUser().endsWith("to add 1 student."));
        assertTrue(command(content, true).execute(model).getFeedbackToUser().startsWith("Imported 1 student.\n"));
    }

    @Test
    public void execute_previewSuggestsTheCommandWithThePathAsItWasTyped() throws Exception {
        Path file = folder.resolve("students confirm");
        Files.writeString(file, THREE);

        String message = new ImportStudentsCommand(file, "students confirm", false).execute(model)
                .getFeedbackToUser();

        assertTrue(message.endsWith("Type 'import students file/\"students confirm\" confirm' to add 3 students."),
                message);
    }

    @Test
    public void execute_duplicatesAndWarnings_areListedAndTheOthersImported() throws Exception {
        model.addPerson(ALICE);
        String content = HEADER + "Alice Pauline,94351253,S3,Math,50,,\nAlice Pauline,81111111,S3,Math,50,,\n"
                + "Amy Tan,91234567,S3,Math,50,,\n,,,,,,\n";

        String preview = command(content, false).execute(model).getFeedbackToUser();
        String done = command(content, true).execute(model).getFeedbackToUser();

        assertEquals("Student import from students.csv\n"
                + "Rows read: 3, ready to add: 2, duplicates skipped: 1, warnings: 1, errors: 0, "
                + "blank lines ignored: 1\n"
                + "Line 2: A student named Alice Pauline with this phone number already exists in TutorFlow. "
                + "The row is skipped.\n"
                + "Line 3: You already have a student named Alice Pauline with a different phone number. "
                + "The row is imported.\n"
                + "Type 'import students file/students.csv confirm' to add 2 students.", preview);
        assertTrue(done.startsWith("Imported 2 students.\n"));
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_nothingNew_saysSoInsteadOfAskingForConfirmation() throws Exception {
        model.addPerson(ALICE);
        String content = HEADER + "Alice Pauline,94351253,S3,Math,50,,\n";

        CommandResult result = command(content, false).execute(model);
        String message = result.getFeedbackToUser();
        assertTrue(result.getConfirmationCommand().isEmpty());

        assertTrue(message.endsWith("\nThere are no new students to import."), message);
        assertTrue(command(content, true).execute(model).getFeedbackToUser().startsWith("Imported 0 students.\n"));
    }

    @Test
    public void execute_invalidRow_importsNothingAndNamesTheRowAndField() throws Exception {
        String content = HEADER + "Amy Tan,91234567,S3,Math,50,,\nBob Lim,123,J1,Physics,62.50,,\n";

        String preview = rejection(command(content, false));
        String confirmed = rejection(command(content, true));

        assertEquals("Student import from students.csv\n"
                + "Rows read: 2, ready to add: 1, duplicates skipped: 0, warnings: 0, errors: 1, "
                + "blank lines ignored: 0\n"
                + "Line 3: phone: Phone numbers must contain 8 to 15 digits, optionally starting with '+'. "
                + "Spaces and hyphens between the digits are allowed.\n"
                + "Nothing can be imported until the rows above are fixed.", preview);
        assertTrue(confirmed.endsWith("\nNothing was imported. Fix the rows above and try again."), confirmed);
        assertTrue(model.getAddressBook().getPersonList().isEmpty(), "all or nothing");
    }

    @Test
    public void execute_fileThatIsMissingOrAFolder_saysSo() {
        Path missing = folder.resolve("nope.csv");

        assertEquals("Cannot find a file at nope.csv.",
                rejection(new ImportStudentsCommand(missing, "nope.csv", false)));
        assertEquals("Cannot find a file at nope.csv.",
                rejection(new ImportStudentsCommand(missing, "nope.csv", true)));
        assertEquals(folder + " is a folder. Give the path of a CSV file.",
                rejection(new ImportStudentsCommand(folder, folder.toString(), false)));
    }

    @Test
    public void execute_fileThatCannotBeRead_isReportedWithItsName() throws Exception {
        assertEquals("Cannot import students.csv: The file is empty. The first line must name the columns: "
                + "name, phone, level, subjects, rate and, if you like, email and venue.",
                rejection(command("", true)));
        assertEquals("Cannot import students.csv: Line 1: the column(s) phone, level, subjects, rate are missing.",
                rejection(command("name\nAmy\n", false)));
    }

    @Test
    public void execute_confirmedImport_resetsTheFilterSoTheNewStudentsAreVisible() throws Exception {
        model.addPerson(ALICE);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("zzz")));
        assertTrue(model.getFilteredPersonList().isEmpty());

        command(THREE, true).execute(model);

        assertEquals(4, model.getFilteredPersonList().size());
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ImportStudentsCommand(null, "a", false));
        assertThrows(NullPointerException.class, () -> new ImportStudentsCommand(folder, null, false));
    }

    @Test
    public void equalsAndHashCode() {
        Path file = folder.resolve("a.csv");
        ImportStudentsCommand command = new ImportStudentsCommand(file, "a.csv", false);

        assertEquals(command, command);
        assertEquals(command, new ImportStudentsCommand(file, "a.csv", false));
        assertEquals(command.hashCode(), new ImportStudentsCommand(file, "a.csv", false).hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
        assertNotEquals(command, new ImportStudentsCommand(folder.resolve("b.csv"), "a.csv", false));
        assertNotEquals(command, new ImportStudentsCommand(file, "b.csv", false));
        assertNotEquals(command, new ImportStudentsCommand(file, "a.csv", true));
        assertFalse(command.toString().isEmpty());
    }
}
