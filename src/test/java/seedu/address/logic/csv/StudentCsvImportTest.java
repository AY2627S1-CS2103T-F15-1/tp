package seedu.address.logic.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.csv.StudentCsvImport.ImportException;
import seedu.address.logic.csv.StudentCsvImport.Report;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

public class StudentCsvImportTest {
    private static final String HEADER = "name,phone,level,subjects,rate,email,venue\n";
    private static final String PHONE_MESSAGE = Phone.MESSAGE_CONSTRAINTS;

    @TempDir
    public Path folder;

    private final AddressBook book = new AddressBook();

    private Path write(String content) throws IOException {
        Path file = folder.resolve("students.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private Report read(String content) throws Exception {
        return StudentCsvImport.read(write(content), "students.csv", book);
    }

    private String refusal(String content) throws IOException {
        Path file = write(content);
        return assertThrows(ImportException.class, () -> StudentCsvImport.read(file, "students.csv", book))
                .getMessage();
    }

    @Test
    public void read_validFile_buildsTheStudentsInOrder() throws Exception {
        Report report = read(HEADER
                + "Amy Tan,91234567,S3,Math;Physics,50,amy@example.com,Blk 5\n"
                + "Bob Lim,+6598765432,j1,Chemistry,$62.50,,\n");

        assertEquals("students.csv", report.fileName());
        assertEquals(2, report.rowsRead());
        assertFalse(report.hasErrors());
        List<Person> students = report.students();
        assertEquals("Amy Tan", students.get(0).getName().fullName);
        assertEquals("91234567", students.get(0).getPhone().value);
        assertEquals("S3", students.get(0).getLevel().value);
        assertEquals(2, students.get(0).getSubjects().size());
        assertEquals("amy@example.com", students.get(0).getEmail().orElseThrow().value);
        assertEquals("Blk 5", students.get(0).getVenue().orElseThrow().value);
        assertEquals("Bob Lim", students.get(1).getName().fullName);
        assertEquals("J1", students.get(1).getLevel().value);
        assertTrue(students.get(1).getEmail().isEmpty());
        assertTrue(students.get(1).getVenue().isEmpty());
        assertEquals("Rows read: 2, ready to add: 2, duplicates skipped: 0, warnings: 0, errors: 0, "
                + "blank lines ignored: 0", report.summary(false));
        assertEquals("Rows read: 2, added: 2, duplicates skipped: 0, warnings: 0, errors: 0, "
                + "blank lines ignored: 0", report.summary(true));
        assertEquals("", report.issues());
    }

    @Test
    public void read_columnsInAnyOrderAndCase_andTheOptionalOnesLeftOut() throws Exception {
        Report report = read("RATE, Subjects ,LEVEL,Phone,NAME\n45,Math,s2,81234567,Cy Ng\n");

        assertEquals("Cy Ng", report.students().getFirst().getName().fullName);
        assertEquals("81234567", report.students().getFirst().getPhone().value);
        assertEquals("S2", report.students().getFirst().getLevel().value);
        assertTrue(report.students().getFirst().getEmail().isEmpty());
    }

    @Test
    public void read_byteOrderMarkCrLfAndQuotedFields_areAccepted() throws Exception {
        Report report = read("\uFEFFname,phone,level,subjects,rate,venue\r\n"
                + "\"Tan, Amy\",91234567,S3,Math,50,\"Blk 5, #01-02\"\r\n");

        assertEquals("Tan, Amy", report.students().getFirst().getName().fullName);
        assertEquals("Blk 5, #01-02", report.students().getFirst().getVenue().orElseThrow().value);
    }

    @Test
    public void read_valuesAreNormalisedAsAddDoes() throws Exception {
        Report report = read(HEADER + "  Amy   Tan  ,9123 4567,S3, Math ; Physics ,50,,\n");

        Person student = report.students().getFirst();
        assertEquals("Amy Tan", student.getName().fullName);
        assertEquals("91234567", student.getPhone().value);
        assertEquals(Set.of(new Subject("Math"), new Subject("Physics")), student.getSubjects());
    }

    @Test
    public void read_blankRows_areIgnoredAndCountedApart() throws Exception {
        Report report = read(HEADER + "\nAmy Tan,91234567,S3,Math,50,,\n,,,,,,\n   ,  ,,,,,\n\n");

        assertEquals(1, report.rowsRead());
        assertEquals(1, report.students().size());
        assertEquals(4, report.blankLines());
        assertTrue(report.summary(false).endsWith("blank lines ignored: 4"));
    }

    @Test
    public void read_headerOnly_hasNothingToAdd() throws Exception {
        Report report = read(HEADER);

        assertEquals(0, report.rowsRead());
        assertTrue(report.students().isEmpty());
        assertFalse(report.hasErrors());
    }

    @Test
    public void read_invalidFields_areNamedByLineAndField() throws Exception {
        Report report = read(HEADER
                + "Amy Tan,123,S3,Math,50,,\n"
                + "Bob Lim,91234567,S9,Math,50,,\n"
                + "Cy Ng,91234567,S3,Math,abc,,\n"
                + "Di Ho,91234567,S3,Math,50,not-an-email,\n"
                + "Ed Ko,91234567,S3,Math;;Physics,50,,\n");

        assertEquals(5, report.errors().size());
        assertEquals("Line 2: phone: " + PHONE_MESSAGE, report.errors().get(0));
        assertTrue(report.errors().get(1).startsWith("Line 3: level: Level must be one of"));
        assertTrue(report.errors().get(2).startsWith("Line 4: rate: Rate must be between 0 and 9999.99"));
        assertTrue(report.errors().get(3).startsWith("Line 5: email: Emails should be of the format"));
        assertTrue(report.errors().get(4).startsWith("Line 6: subjects: Subjects must be 1-30 characters"));
        assertTrue(report.hasErrors());
        assertTrue(report.students().isEmpty());
    }

    @Test
    public void read_rowWithSeveralFaults_listsEachOnItsOwnLineButCountsOneRow() throws Exception {
        Report report = read(HEADER + "Amy Tan,123,S9,Math,50,,\n");

        assertEquals(1, report.errors().size());
        String[] lines = report.errors().getFirst().split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("Line 2: phone:"));
        assertTrue(lines[1].startsWith("Line 2: level:"));
        assertTrue(report.summary(false).contains("errors: 1"));
    }

    @Test
    public void read_missingRequiredValue_isReportedForThatField() throws Exception {
        Report report = read(HEADER + ",91234567,S3,Math,50,,\nAmy Tan,91234567,S3,,50,,\nBob Lim,91234567,S3\n");

        assertEquals(List.of("Line 2: name: A value is required.", "Line 3: subjects: A value is required.",
                "Line 4: subjects: A value is required.\nLine 4: rate: A value is required."), report.errors());
    }

    @Test
    public void read_rowWithMoreFieldsThanColumns_isAnError() throws Exception {
        Report report = read("name,phone,level,subjects,rate\nAmy Tan,91234567,S3,Math,50,extra\n");

        assertEquals(List.of("Line 2: the row has more fields than the first line has columns."),
                report.errors());
    }

    @Test
    public void read_moreThanTenSubjects_followsTheRuleOfAdd() throws Exception {
        Report report = read(HEADER + "Amy Tan,91234567,S3,a;b;c;d;e;f;g;h;i;j;k,50,,\n");

        assertEquals("Line 2: subjects: A student can have at most 10 subjects.", report.errors().getFirst());
    }

    @Test
    public void read_errorLinesCountFromTheLineInTheFileEvenAfterQuotedLineBreaks() throws Exception {
        Report report = read(HEADER + "\"Amy\nTan\",91234567,S3,Math,50,,\nBob Lim,123,S3,Math,50,,\n");

        assertEquals(2, report.errors().size());
        assertTrue(report.errors().get(0).startsWith("Line 2: name:"), report.errors().get(0));
        assertTrue(report.errors().get(1).startsWith("Line 4: phone:"), report.errors().get(1));
    }

    @Test
    public void read_studentThatExists_isSkippedAsADuplicate() throws Exception {
        book.addPerson(ALICE);

        Report report = read(HEADER + "alice pauline,94351253,S3,Math,50,,\n");

        assertEquals(List.of("Line 2: A student named alice pauline with this phone number already exists in "
                + "TutorFlow. The row is skipped."), report.duplicates());
        assertTrue(report.students().isEmpty());
        assertFalse(report.hasErrors());
        assertTrue(report.summary(false).contains("ready to add: 0, duplicates skipped: 1"));
    }

    @Test
    public void read_archivedStudentThatExists_isAlsoADuplicate() throws Exception {
        book.addPerson(ALICE.withArchived(true));

        assertEquals(1, read(HEADER + "Alice Pauline,94351253,S3,Math,50,,\n").duplicates().size());
    }

    @Test
    public void read_studentRepeatedInTheFile_isSkippedTheSecondTime() throws Exception {
        Report report = read(HEADER + "Amy Tan,91234567,S3,Math,50,,\nAMY TAN,91234567,S4,Physics,60,,\n");

        assertEquals(1, report.students().size());
        assertEquals(List.of("Line 3: A student named AMY TAN with this phone number already exists in TutorFlow. "
                + "The row is skipped."), report.duplicates());
    }

    @Test
    public void read_sameNameWithAnotherPhone_isImportedWithAWarning() throws Exception {
        book.addPerson(ALICE);

        Report report = read(HEADER + "Alice Pauline,81111111,S3,Math,50,,\nAmy Tan,91234567,S3,Math,50,,\n"
                + "amy tan,92222222,S3,Math,50,,\n");

        assertEquals(3, report.students().size());
        assertEquals(List.of("Line 2: You already have a student named Alice Pauline with a different phone "
                + "number. The row is imported.", "Line 4: You already have a student named amy tan with a "
                + "different phone number. The row is imported."), report.warnings());
        assertTrue(report.duplicates().isEmpty());
    }

    @Test
    public void read_rowThatIsInvalid_isNotCheckedForDuplicates() throws Exception {
        book.addPerson(ALICE);

        Report report = read(HEADER + "Alice Pauline,94351253,S9,Math,50,,\n");

        assertEquals(1, report.errors().size());
        assertTrue(report.duplicates().isEmpty());
    }

    @Test
    public void read_wrongHeader_refusesTheFile() throws Exception {
        assertEquals("Cannot import students.csv: Line 1: the column(s) phone, rate are missing.",
                refusal("name,level,subjects\nAmy,S3,Math\n"));
        assertEquals("Cannot import students.csv: Line 1: unknown column 'notes'. The columns are name, phone, "
                + "level, subjects, rate, email and venue.", refusal("name,phone,level,subjects,rate,notes\n"));
        assertEquals("Cannot import students.csv: Line 1: the column 'name' is given more than once.",
                refusal("name,phone,level,subjects,rate,NAME\n"));
        assertEquals("Cannot import students.csv: Line 1: a column has no name.",
                refusal("name,phone,level,subjects,rate,\n"));
    }

    @Test
    public void read_emptyFile_refusesTheFile() throws Exception {
        String expected = "Cannot import students.csv: The file is empty. The first line must name the columns: "
                + "name, phone, level, subjects, rate and, if you like, email and venue.";

        assertEquals(expected, refusal(""));
        assertEquals(expected, refusal("\uFEFF"));
    }

    @Test
    public void read_quotingError_namesTheLine() throws Exception {
        assertEquals("Cannot import students.csv: Line 3: a quoted field is not closed.",
                refusal(HEADER + "Amy Tan,91234567,S3,Math,50,,\n\"Bob,91234567,S3,Math,50,,\n"));
        assertEquals("Cannot import students.csv: Line 2: a quote must start a field, or be written twice "
                + "inside quotes.", refusal(HEADER + "Am\"y,91234567,S3,Math,50,,\n"));
    }

    @Test
    public void read_bytesThatAreNotUtf8_namesTheLine() throws Exception {
        Path file = folder.resolve("students.csv");
        byte[] first = (HEADER + "Amy Tan,91234567,S3,Math,50,,\nBob ").getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[first.length + 3];
        System.arraycopy(first, 0, bytes, 0, first.length);
        bytes[first.length] = (byte) 0xFF;
        bytes[first.length + 1] = (byte) 0xFE;
        bytes[first.length + 2] = '\n';
        Files.write(file, bytes);

        String message = assertThrows(ImportException.class, () -> StudentCsvImport.read(file, "students.csv", book))
                .getMessage();

        assertEquals("Cannot import students.csv: Line 3: the file is not valid UTF-8 text.", message);
    }

    @Test
    public void read_fileOverFiveMegabytes_isRefusedBeforeItIsRead() throws Exception {
        Path file = folder.resolve("students.csv");
        Files.write(file, new byte[(int) StudentCsvImport.MAX_BYTES + 1]);

        String message = assertThrows(ImportException.class, () -> StudentCsvImport.read(file, "students.csv", book))
                .getMessage();

        assertEquals("Cannot import students.csv: The file is larger than 5 MB.", message);
    }

    @Test
    public void read_limitOfFiveThousandStudents_isEnforced() throws Exception {
        assertEquals(5000, read(rows(5000)).students().size());

        assertEquals("Cannot import students.csv: The file has more than 5000 students. "
                + "Split it and import the parts one at a time.", refusal(rows(5001)));
    }

    @Test
    public void read_blankRowsDoNotCountTowardsTheLimit() throws Exception {
        assertEquals(5000, read(rows(5000) + ",,,,,,\n".repeat(20)).students().size());
    }

    @Test
    public void issues_listsDuplicatesThenWarningsThenErrors_eachLimitedToFifty() throws Exception {
        StringBuilder rows = new StringBuilder(HEADER);
        for (int i = 0; i < 52; i++) {
            rows.append("Bad Row,12,S3,Math,50,,\n");
        }
        Report report = read(rows.toString());

        String[] lines = report.issues().split("\n");

        assertEquals(51, lines.length);
        assertEquals("... and 2 more rows with errors.", lines[50]);
        assertTrue(lines[0].startsWith("Line 2: phone:"));
        assertTrue(lines[49].startsWith("Line 51: phone:"));
        assertEquals(52, report.errors().size());
    }

    @Test
    public void issues_oneRowLeftOut_isCountedInTheSingular() throws Exception {
        book.addPerson(ALICE);
        StringBuilder rows = new StringBuilder(HEADER);
        for (int i = 0; i < 51; i++) {
            rows.append("Alice Pauline,94351253,S3,Math,50,,\n");
        }

        List<String> lines = List.of(read(rows.toString()).issues().split("\n"));

        assertEquals("... and 1 more duplicate row.", lines.getLast());
    }

    @Test
    public void issues_orderIsDuplicatesWarningsErrors() throws Exception {
        book.addPerson(ALICE);

        Report report = read(HEADER + "Alice Pauline,81111111,S3,Math,50,,\nBad,12,S3,Math,50,,\n"
                + "Alice Pauline,94351253,S3,Math,50,,\n");

        List<String> lines = List.of(report.issues().split("\n"));
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).startsWith("Line 4: A student named"));
        assertTrue(lines.get(1).startsWith("Line 2: You already have"));
        assertTrue(lines.get(2).startsWith("Line 3: phone"), lines.get(2));
    }

    @Test
    public void fileNameOf_dropsTheFoldersOfEitherKind() {
        assertEquals("students.csv", StudentCsvImport.fileNameOf("students.csv"));
        assertEquals("students.csv", StudentCsvImport.fileNameOf("data/students.csv"));
        assertEquals("old students.csv",
                StudentCsvImport.fileNameOf("C:\\Users\\Maya\\My Documents\\old students.csv"));
        assertEquals("a.csv", StudentCsvImport.fileNameOf("/home/maya/a.csv"));
    }

    @Test
    public void read_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> StudentCsvImport.read(null, "a.csv", book));
        assertThrows(NullPointerException.class, () -> StudentCsvImport.read(folder, "a.csv", null));
        assertThrows(NullPointerException.class, () -> StudentCsvImport.fileNameOf(null));
    }

    private static String rows(int count) {
        List<String> lines = new ArrayList<>();
        lines.add("name,phone,level,subjects,rate");
        lines.addAll(IntStream.range(0, count)
                .mapToObj(i -> "Student " + letters(i) + "," + (10000000 + i) + ",S3,Math,50")
                .collect(Collectors.toList()));
        return String.join("\n", lines) + "\n";
    }

    private static String letters(int number) {
        StringBuilder text = new StringBuilder();
        int rest = number;
        do {
            text.append((char) ('A' + rest % 26));
            rest /= 26;
        } while (rest > 0);
        return text.toString();
    }
}
