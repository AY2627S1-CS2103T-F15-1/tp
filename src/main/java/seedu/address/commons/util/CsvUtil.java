package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes comma-separated values in the usual spreadsheet form: a field that contains a comma, a double
 * quote or a line break is put in double quotes, with a quote inside it written twice.
 */
public final class CsvUtil {

    /** The byte order mark that a spreadsheet may write at the start of a UTF-8 file. */
    private static final char BYTE_ORDER_MARK = '﻿';

    /**
     * One record of a file.
     *
     * @param line the line of the file on which the record starts, counting from 1.
     * @param fields the values of the record, which are never null.
     */
    public record Record(int line, List<String> fields) {

        /** Creates a record; {@code fields} is copied. */
        public Record {
            fields = List.copyOf(fields);
        }

        /** Returns whether every field is empty or blank, as a spreadsheet writes a row that has been cleared. */
        public boolean isBlank() {
            return fields.stream().allMatch(String::isBlank);
        }
    }

    /** Thrown when text is not well formed CSV; the message names the line. */
    public static class CsvFormatException extends Exception {
        private final int line;

        /** Creates an exception about {@code line}, counting from 1. */
        public CsvFormatException(int line, String reason) {
            super("Line " + line + ": " + reason);
            this.line = line;
        }

        public int getLine() {
            return line;
        }
    }

    private CsvUtil() {
    }

    /**
     * Splits {@code text} into records. A line break of any kind (CR, LF or CRLF) ends a record unless it is inside
     * quotes, and one at the very end of the text does not start another. An empty line is a record with one empty
     * field.
     *
     * @throws CsvFormatException if a quote is not closed or is in the wrong place.
     */
    public static List<Record> parse(String text) throws CsvFormatException {
        requireNonNull(text);
        String input = !text.isEmpty() && text.charAt(0) == BYTE_ORDER_MARK ? text.substring(1) : text;
        List<Record> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        int line = 1;
        int recordLine = 1;
        boolean isQuoted = false;
        boolean isInQuotes = false;
        boolean hasContent = false; // whether anything of the current record has been read
        int quoteLine = 1;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (isInQuotes) {
                if (c == '"') {
                    if (i + 1 < input.length() && input.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        isInQuotes = false;
                    }
                } else {
                    field.append(c);
                    if (c == '\n' || c == '\r' && !(i + 1 < input.length() && input.charAt(i + 1) == '\n')) {
                        line++;
                    }
                }
                continue;
            }
            if (c == '"') {
                if (field.length() > 0 || isQuoted) {
                    throw new CsvFormatException(line, isQuoted ? "unexpected text after a closing quote"
                            : "a quote must start a field, or be written twice inside quotes");
                }
                isInQuotes = true;
                isQuoted = true;
                hasContent = true;
                quoteLine = line;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
                isQuoted = false;
                hasContent = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n') {
                    i++;
                }
                fields.add(field.toString());
                records.add(new Record(recordLine, fields));
                fields = new ArrayList<>();
                field.setLength(0);
                isQuoted = false;
                hasContent = false;
                line++;
                recordLine = line;
            } else if (isQuoted) {
                throw new CsvFormatException(line, "unexpected text after a closing quote");
            } else {
                field.append(c);
                hasContent = true;
            }
        }
        if (isInQuotes) {
            throw new CsvFormatException(quoteLine, "a quoted field is not closed");
        }
        if (hasContent || field.length() > 0) {
            fields.add(field.toString());
            records.add(new Record(recordLine, fields));
        }
        return List.copyOf(records);
    }

    /** Returns {@code text} in double quotes, with each quote inside it written twice. */
    public static String quote(String text) {
        requireNonNull(text);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
