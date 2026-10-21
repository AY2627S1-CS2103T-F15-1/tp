package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class PathArgumentTest {
    private static final String USAGE = "usage";

    private static PathArgument parse(String value) throws ParseException {
        return PathArgument.parse(value, USAGE);
    }

    private static void assertPath(String expectedPath, boolean isConfirmed, String value) throws ParseException {
        PathArgument argument = parse(value);

        assertEquals(expectedPath, argument.getPath(), value);
        assertEquals(isConfirmed, argument.isConfirmed(), value);
    }

    @Test
    public void parse_plainPath_runsToTheEnd() throws Exception {
        assertPath("students.csv", false, "students.csv");
        assertPath("my students.csv", false, "my students.csv");
        assertPath("C:\\Users\\Maya\\this year.csv", false, "  C:\\Users\\Maya\\this year.csv  ");
        assertPath("a to/ b from/ c.csv", false, "a to/ b from/ c.csv");
    }

    @Test
    public void parse_finalWordConfirm_isTheConfirmation() throws Exception {
        assertPath("students.csv", true, "students.csv confirm");
        assertPath("my students.csv", true, "my students.csv   confirm  ");
        assertPath("a\tb.csv", true, "a\tb.csv\tconfirm");
    }

    @Test
    public void parse_confirmInsideAWordOrWithAnotherCase_isPartOfThePath() throws Exception {
        assertPath("unconfirm", false, "unconfirm");
        assertPath("confirmed.csv", false, "confirmed.csv");
        assertPath("a.csv CONFIRM", false, "a.csv CONFIRM");
        assertPath("a confirm.csv", false, "a confirm.csv");
    }

    @Test
    public void parse_quotedPath_mayEndInConfirmAndHoldSpacesAndPrefixes() throws Exception {
        assertPath("students confirm", false, "\"students confirm\"");
        assertPath("students confirm", true, "\"students confirm\" confirm");
        assertPath("C:\\Users\\Maya\\My Documents\\old students.csv", true,
                "\"C:\\Users\\Maya\\My Documents\\old students.csv\" confirm");
        assertPath("to/ from/ file/.csv", false, "\"to/ from/ file/.csv\"");
        assertPath(" padded ", false, "\" padded \"");
    }

    @Test
    public void parse_textAfterAQuotedPath_isInvalidFormat() {
        for (String value : new String[] {"\"a.csv\" now", "\"a.csv\"confirm please", "\"a.csv\" confirm confirm"}) {
            ParseException e = assertThrows(ParseException.class, () -> parse(value), value);

            assertEquals("Invalid command format!\n" + USAGE, e.getMessage());
        }
    }

    @Test
    public void parse_quoteThatIsNotClosed_isAnError() {
        assertEquals(PathArgument.MESSAGE_UNCLOSED_QUOTE,
                assertThrows(ParseException.class, () -> parse("\"a.csv")).getMessage());
        assertEquals(PathArgument.MESSAGE_UNCLOSED_QUOTE,
                assertThrows(ParseException.class, () -> parse("\"a.csv confirm")).getMessage());
    }

    @Test
    public void parse_noPath_isAnEmptyValue() {
        for (String value : new String[] {"", "   ", "confirm", "  confirm ", "\"\"", "\"  \" confirm"}) {
            assertEquals("Empty value for file/. Enter a value after this prefix.",
                    assertThrows(ParseException.class, () -> parse(value), value).getMessage());
        }
    }

    @Test
    public void format_quotesOnlyWhatCouldNotBeReadBack() throws Exception {
        assertEquals("students.csv", PathArgument.format("students.csv"));
        assertEquals("my students.csv", PathArgument.format("my students.csv"));
        assertEquals("\"students confirm\"", PathArgument.format("students confirm"));
        assertEquals("\"confirm\"", PathArgument.format("confirm"));
        assertEquals("\" padded\"", PathArgument.format(" padded"));
        for (String path : new String[] {"students.csv", "students confirm", "confirm", " padded ", "x y.csv"}) {
            assertPath(path, false, PathArgument.format(path));
        }
        assertTrue(PathArgument.format("a").equals("a"));
        assertFalse(PathArgument.format("a confirm").equals("a confirm"));
    }
}
