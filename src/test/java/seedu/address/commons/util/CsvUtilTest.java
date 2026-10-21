package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.CsvUtil.CsvFormatException;
import seedu.address.commons.util.CsvUtil.Record;

public class CsvUtilTest {

    private static List<List<String>> fields(String text) throws CsvFormatException {
        return CsvUtil.parse(text).stream().map(Record::fields).toList();
    }

    @Test
    public void parse_plainRows_splitsFieldsAndRecords() throws Exception {
        assertEquals(List.of(List.of("a", "b", "c"), List.of("1", "2", "3")), fields("a,b,c\n1,2,3\n"));
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), fields("a,b\nc,d"), "no final line break");
    }

    @Test
    public void parse_emptyText_hasNoRecords() throws Exception {
        assertTrue(CsvUtil.parse("").isEmpty());
        assertTrue(CsvUtil.parse("﻿").isEmpty());
    }

    @Test
    public void parse_everyKindOfLineBreak_endsARecord() throws Exception {
        List<List<String>> expected = List.of(List.of("a"), List.of("b"), List.of("c"), List.of("d"));

        assertEquals(expected, fields("a\nb\r\nc\rd"));
        assertEquals(expected, fields("a\r\nb\r\nc\r\nd\r\n"));
    }

    @Test
    public void parse_byteOrderMark_isDropped() throws Exception {
        assertEquals(List.of(List.of("name", "phone")), fields("﻿name,phone\n"));
    }

    @Test
    public void parse_quotedFields_keepCommasQuotesAndLineBreaks() throws Exception {
        assertEquals(List.of(List.of("Tan, Amy", "say \"hi\"", "two\nlines", "")),
                fields("\"Tan, Amy\",\"say \"\"hi\"\"\",\"two\nlines\",\"\"\n"));
        assertEquals(List.of(List.of("a\r\nb", "c")), fields("\"a\r\nb\",c"));
    }

    @Test
    public void parse_emptyFields_areKept() throws Exception {
        assertEquals(List.of(List.of("a", "", "c"), List.of("", "", ""), List.of("x", "")), fields("a,,c\n,,\nx,"));
    }

    @Test
    public void parse_emptyLine_isARecordOfOneEmptyField() throws Exception {
        List<Record> records = CsvUtil.parse("a\n\nb\n");

        assertEquals(3, records.size());
        assertEquals(List.of(""), records.get(1).fields());
        assertTrue(records.get(1).isBlank());
        assertFalse(records.get(0).isBlank());
    }

    @Test
    public void record_isBlankWhenEveryFieldIsEmptyOrSpaces() {
        assertTrue(new Record(1, List.of("", " ", "\t")).isBlank());
        assertFalse(new Record(1, List.of("", "x")).isBlank());
    }

    @Test
    public void parse_recordLines_countFromOneAndSkipLinesInsideQuotes() throws Exception {
        List<Record> records = CsvUtil.parse("h\n\"a\nb\",c\n\nlast\r\nend");

        assertEquals(List.of(1, 2, 4, 5, 6), records.stream().map(Record::line).toList());
    }

    @Test
    public void parse_unclosedQuote_namesTheLineWhereItOpened() {
        CsvFormatException e = assertThrows(CsvFormatException.class, () -> CsvUtil.parse("a,b\nc,\"open\nmore\n"));

        assertEquals(2, e.getLine());
        assertEquals("Line 2: a quoted field is not closed", e.getMessage());
    }

    @Test
    public void parse_quoteInTheMiddleOfAField_isAnError() {
        CsvFormatException e = assertThrows(CsvFormatException.class, () -> CsvUtil.parse("a\nab\"c,d"));

        assertEquals("Line 2: a quote must start a field, or be written twice inside quotes", e.getMessage());
    }

    @Test
    public void parse_textAfterAClosingQuote_isAnError() {
        assertEquals("Line 1: unexpected text after a closing quote",
                assertThrows(CsvFormatException.class, () -> CsvUtil.parse("\"a\"b,c")).getMessage());
        assertEquals("Line 1: unexpected text after a closing quote",
                assertThrows(CsvFormatException.class, () -> CsvUtil.parse("\"a\" ,c")).getMessage());
    }

    @Test
    public void parse_nullText_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CsvUtil.parse(null));
        assertThrows(NullPointerException.class, () -> CsvUtil.quote(null));
    }

    @Test
    public void quote_doublesTheQuotesInside() {
        assertEquals("\"\"", CsvUtil.quote(""));
        assertEquals("\"Tan, \"\"Amy\"\"\"", CsvUtil.quote("Tan, \"Amy\""));
    }

    @Test
    public void quoteAndParse_areInverse() throws Exception {
        String awkward = "a,\"b\"\nc\r\nd";

        assertEquals(List.of(List.of(awkward, "x")), fields(CsvUtil.quote(awkward) + ",x"));
    }
}
