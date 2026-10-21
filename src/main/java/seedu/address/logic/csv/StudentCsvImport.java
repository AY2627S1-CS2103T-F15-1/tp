package seedu.address.logic.csv;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.util.CsvUtil;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Rate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.Venue;

/**
 * Reads students from a CSV file and checks every field by exactly the rules of the {@code add} command, so that
 * an import cannot create a student that could not be typed. Reading changes nothing: the result is a report of
 * what an import would do.
 */
public final class StudentCsvImport {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final int MAX_STUDENTS = 5000;
    /** The number of rows of one kind that a report lists in full; the others are counted. */
    public static final int MAX_LISTED = 50;

    public static final String MESSAGE_CANNOT_IMPORT = "Cannot import %1$s: %2$s";
    public static final String MESSAGE_TOO_LARGE = "The file is larger than 5 MB.";
    public static final String MESSAGE_NOT_UTF8 = "Line %1$d: the file is not valid UTF-8 text.";
    public static final String MESSAGE_EMPTY_FILE = "The file is empty. The first line must name the columns: "
            + "name, phone, level, subjects, rate and, if you like, email and venue.";
    public static final String MESSAGE_TOO_MANY = "The file has more than 5000 students. "
            + "Split it and import the parts one at a time.";
    public static final String MESSAGE_UNKNOWN_COLUMN = "Line 1: unknown column '%1$s'. The columns are "
            + "name, phone, level, subjects, rate, email and venue.";
    public static final String MESSAGE_REPEATED_COLUMN = "Line 1: the column '%1$s' is given more than once.";
    public static final String MESSAGE_UNNAMED_COLUMN = "Line 1: a column has no name.";
    public static final String MESSAGE_MISSING_COLUMNS = "Line 1: the column(s) %1$s are missing.";
    public static final String MESSAGE_TOO_MANY_FIELDS = "Line %1$d: the row has more fields than the first line "
            + "has columns.";
    public static final String MESSAGE_REQUIRED = "Line %1$d: %2$s: A value is required.";
    public static final String MESSAGE_FIELD_INVALID = "Line %1$d: %2$s: %3$s";
    public static final String MESSAGE_DUPLICATE = "Line %1$d: A student named %2$s with this phone number already "
            + "exists in TutorFlow. The row is skipped.";
    public static final String MESSAGE_SAME_NAME = "Line %1$d: You already have a student named %2$s with a "
            + "different phone number. The row is imported.";

    private static final List<String> REQUIRED_COLUMNS = List.of("name", "phone", "level", "subjects", "rate");
    private static final List<String> OPTIONAL_COLUMNS = List.of("email", "venue");
    private static final String SUBJECT_SEPARATOR = ";";

    /**
     * What an import would do with a file.
     *
     * @param fileName the name of the file, without its folders.
     * @param rowsRead the rows that hold students, which is every row except the first line and blank rows.
     * @param students the students to add, in the order of the file.
     * @param duplicates one message for each row that is skipped because the student exists.
     * @param warnings one message for each row that is imported although a student has the same name.
     * @param errors one message for each row that is invalid; a row with several faults has one message of
     *               several lines.
     * @param blankLines the rows whose fields are all empty.
     */
    public record Report(String fileName, int rowsRead, List<Person> students, List<String> duplicates,
            List<String> warnings, List<String> errors, int blankLines) {

        /** Creates a report; the lists are copied. */
        public Report {
            students = List.copyOf(students);
            duplicates = List.copyOf(duplicates);
            warnings = List.copyOf(warnings);
            errors = List.copyOf(errors);
        }

        /** Returns whether any row is invalid, in which case nothing can be imported. */
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        /**
         * Returns the counts of the report on one line.
         *
         * @param isImported whether the students have been added, rather than only found ready to add.
         */
        public String summary(boolean isImported) {
            return String.format("Rows read: %d, %s: %d, duplicates skipped: %d, warnings: %d, errors: %d, "
                    + "blank lines ignored: %d", rowsRead, isImported ? "added" : "ready to add", students.size(),
                    duplicates.size(), warnings.size(), errors.size(), blankLines);
        }

        /**
         * Returns the rows to look at: the duplicates, then the warnings, then the errors, each on a line of its
         * own and each kind limited to {@link StudentCsvImport#MAX_LISTED} rows with a count of those left out. The
         * text is empty if there is nothing to look at.
         */
        public String issues() {
            List<String> lines = new ArrayList<>();
            addListed(lines, duplicates, "duplicate row", "duplicate rows");
            addListed(lines, warnings, "warning", "warnings");
            addListed(lines, errors, "row with errors", "rows with errors");
            return String.join("\n", lines);
        }

        private static void addListed(List<String> lines, List<String> messages, String one, String many) {
            lines.addAll(messages.subList(0, Math.min(MAX_LISTED, messages.size())));
            int left = messages.size() - MAX_LISTED;
            if (left > 0) {
                lines.add("... and " + left + " more " + (left == 1 ? one : many) + ".");
            }
        }
    }

    /** Thrown when a file cannot be read as a list of students at all; the message says why. */
    public static class ImportException extends Exception {
        /** Creates an exception for the file named {@code fileName}. */
        public ImportException(String fileName, String reason) {
            super(String.format(MESSAGE_CANNOT_IMPORT, fileName, reason));
        }
    }

    private StudentCsvImport() {
    }

    /** Returns the name of {@code path} without its folders, whichever kind of separator they are written with. */
    public static String fileNameOf(String path) {
        requireNonNull(path);
        return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
    }

    /**
     * Reads {@code file} and reports what importing it into {@code book} would do. Nothing is changed.
     *
     * @throws ImportException if the file is too large, is not UTF-8 CSV with the columns of a student, or has more
     *     students than an import takes.
     */
    public static Report read(Path file, String displayPath, ReadOnlyAddressBook book) throws ImportException {
        requireNonNull(file);
        requireNonNull(book);
        String fileName = fileNameOf(displayPath);
        List<CsvUtil.Record> records = parseRecords(file, fileName);
        Map<String, Integer> columns = readHeader(records.getFirst(), fileName);

        List<Person> students = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int rowsRead = 0;
        int blankLines = 0;
        for (CsvUtil.Record record : records.subList(1, records.size())) {
            if (record.isBlank()) {
                blankLines++;
                continue;
            }
            rowsRead++;
            if (rowsRead > MAX_STUDENTS) {
                throw new ImportException(fileName, MESSAGE_TOO_MANY);
            }
            readRow(record, columns, book, students, duplicates, warnings, errors);
        }
        return new Report(fileName, rowsRead, students, duplicates, warnings, errors, blankLines);
    }

    private static List<CsvUtil.Record> parseRecords(Path file, String fileName) throws ImportException {
        byte[] bytes;
        try {
            if (Files.size(file) > MAX_BYTES) {
                throw new ImportException(fileName, MESSAGE_TOO_LARGE);
            }
            bytes = Files.readAllBytes(file);
        } catch (IOException e) {
            throw new ImportException(fileName, e.getMessage() == null ? e.getClass().getSimpleName()
                    : e.getMessage() + ".");
        }
        String text = decode(bytes, fileName);
        List<CsvUtil.Record> records;
        try {
            records = CsvUtil.parse(text);
        } catch (CsvUtil.CsvFormatException e) {
            throw new ImportException(fileName, e.getMessage() + ".");
        }
        if (records.isEmpty()) {
            throw new ImportException(fileName, MESSAGE_EMPTY_FILE);
        }
        return records;
    }

    /** Decodes strictly, so that a byte that is not UTF-8 is reported with its line and never replaced. */
    private static String decode(byte[] bytes, String fileName) throws ImportException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer in = ByteBuffer.wrap(bytes);
        CharBuffer out = CharBuffer.allocate(bytes.length);
        CoderResult result = decoder.decode(in, out, true);
        if (result.isError()) {
            throw new ImportException(fileName, String.format(MESSAGE_NOT_UTF8, lineAt(bytes, in.position())));
        }
        out.flip();
        return out.toString();
    }

    private static int lineAt(byte[] bytes, int offset) {
        int line = 1;
        for (int i = 0; i < offset; i++) {
            boolean isLoneCarriageReturn = bytes[i] == '\r' && !(i + 1 < bytes.length && bytes[i + 1] == '\n');
            if (bytes[i] == '\n' || isLoneCarriageReturn) {
                line++;
            }
        }
        return line;
    }

    /** Returns the position of each column named in the first line. */
    private static Map<String, Integer> readHeader(CsvUtil.Record header, String fileName) throws ImportException {
        Set<String> known = new LinkedHashSet<>(REQUIRED_COLUMNS);
        known.addAll(OPTIONAL_COLUMNS);
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.fields().size(); i++) {
            String column = header.fields().get(i).strip().toLowerCase(Locale.ENGLISH);
            if (column.isEmpty()) {
                throw new ImportException(fileName, MESSAGE_UNNAMED_COLUMN);
            }
            if (!known.contains(column)) {
                throw new ImportException(fileName, String.format(MESSAGE_UNKNOWN_COLUMN, column));
            }
            if (columns.put(column, i) != null) {
                throw new ImportException(fileName, String.format(MESSAGE_REPEATED_COLUMN, column));
            }
        }
        String missing = REQUIRED_COLUMNS.stream().filter(column -> !columns.containsKey(column))
                .collect(Collectors.joining(", "));
        if (!missing.isEmpty()) {
            throw new ImportException(fileName, String.format(MESSAGE_MISSING_COLUMNS, missing));
        }
        return columns;
    }

    private static void readRow(CsvUtil.Record record, Map<String, Integer> columns, ReadOnlyAddressBook book,
            List<Person> students, List<String> duplicates, List<String> warnings, List<String> errors) {
        int line = record.line();
        if (record.fields().size() > columns.size()) {
            errors.add(String.format(MESSAGE_TOO_MANY_FIELDS, line));
            return;
        }
        List<String> faults = new ArrayList<>();
        Name name = parseField(record, columns, "name", true, faults, ParserUtil::parseName);
        Phone phone = parseField(record, columns, "phone", true, faults, ParserUtil::parsePhone);
        Level level = parseField(record, columns, "level", true, faults, ParserUtil::parseLevel);
        Set<Subject> subjects = parseField(record, columns, "subjects", true, faults,
                value -> ParserUtil.parseSubjects(List.of(value.split(SUBJECT_SEPARATOR, -1))));
        Rate rate = parseField(record, columns, "rate", true, faults, ParserUtil::parseRate);
        Email email = parseField(record, columns, "email", false, faults, ParserUtil::parseEmail);
        Venue venue = parseField(record, columns, "venue", false, faults, ParserUtil::parseVenue);
        if (!faults.isEmpty()) {
            errors.add(String.join("\n", faults));
            return;
        }

        Person student = new Person(name, phone, email, level, subjects, rate, venue);
        if (Stream.concat(book.getPersonList().stream(), students.stream()).anyMatch(student::isSamePerson)) {
            duplicates.add(String.format(MESSAGE_DUPLICATE, line, name));
            return;
        }
        if (Stream.concat(book.getPersonList().stream(), students.stream()).anyMatch(student::hasSameName)) {
            warnings.add(String.format(MESSAGE_SAME_NAME, line, name));
        }
        students.add(student);
    }

    /**
     * Returns the value of column {@code column} of {@code record}, parsed by {@code parser}, or null if the column
     * is optional and empty. A fault is added to {@code faults} and null is returned if the value is wrong or is
     * missing from a required column.
     */
    private static <T> T parseField(CsvUtil.Record record, Map<String, Integer> columns, String column,
            boolean isRequired, List<String> faults, FieldParser<T> parser) {
        Integer position = columns.get(column);
        String value = position == null || position >= record.fields().size() ? "" : record.fields().get(position);
        if (value.isBlank()) {
            if (isRequired) {
                faults.add(String.format(MESSAGE_REQUIRED, record.line(), column));
            }
            return null;
        }
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            faults.add(String.format(MESSAGE_FIELD_INVALID, record.line(), column, e.getMessage()));
            return null;
        }
    }

    /** Parses one field of a row. */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }
}
